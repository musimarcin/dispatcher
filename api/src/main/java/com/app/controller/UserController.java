package com.app.controller;

import com.app.dto.UserDto;
import com.app.security.CustomUserDetailService;
import com.app.security.JWTGenerator;
import com.app.security.SecurityUtil;
import com.app.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;
    private final JWTGenerator jwtGenerator;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailService customUserDetailService;
    private final SecurityUtil securityUtil;

    public UserController(UserService userService, JWTGenerator jwtGenerator,
                          AuthenticationManager authenticationManager, CustomUserDetailService customUserDetailService, SecurityUtil securityUtil) {
        this.userService = userService;
        this.jwtGenerator = jwtGenerator;
        this.authenticationManager = authenticationManager;
        this.customUserDetailService = customUserDetailService;
        this.securityUtil = securityUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> createUser(@RequestBody @Valid UserDto userDto) {
        if (securityUtil.getSessionUser() != null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Already logged in"));
        }
        UserDto created = userService.createUser(userDto);
        if (created == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Username or email taken"));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "User registered successfully", "user", created));
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UserDto userDto, HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userDto.getUsername(), userDto.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtGenerator.generateToken(authentication);
            response.addCookie(securityUtil.getNewCookie(token));
            return ResponseEntity.status(HttpStatus.OK).body("Logged in successfully");
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletResponse response) {
        securityUtil.deleteCookie(response);
        securityUtil.clear();
        return ResponseEntity.status(HttpStatus.OK).body("Logged out successfully");
    }

    @DeleteMapping
    public ResponseEntity<?> deleteUser(HttpServletResponse response) {
        String username = securityUtil.getSessionUser();
        if (username == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You must be logged in to delete user");
        if (userService.deleteUser(username)) {
            securityUtil.deleteCookie(response);
            securityUtil.clear();
            return ResponseEntity.status(HttpStatus.OK).body("User deleted successfully");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");

    }

    @PutMapping("/change")
    public ResponseEntity<?> changeUser(@RequestBody UserDto userDto, HttpServletResponse response) {
        String username = securityUtil.getSessionUser();
        if (username == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in");
        StringBuilder message = new StringBuilder();
        if (userDto.getUsername() != null && !userDto.getUsername().isEmpty()) {
            if (!userService.changeUsername(username, userDto.getUsername()))
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to change username or username already taken");

            UserDetails userDetails = customUserDetailService.loadUserByUsername(userDto.getUsername());
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtGenerator.generateToken(authentication);
            response.addCookie(securityUtil.getNewCookie(token));
            username = userDto.getUsername();

            message.append("username ");
        }
        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            if (!userService.changePassword(username, userDto.getPassword()))
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to change password");

            message.append("password ");
        }
        if (userDto.getEmail() != null && !userDto.getEmail().isEmpty()) {
            if (!userService.changeEmail(username, userDto.getEmail()))
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to change email");

            message.append("email ");
        }
        message.deleteCharAt(message.length() - 1);
        return ResponseEntity.status(HttpStatus.OK).body("Successfully changed " + message);
    }

    @PatchMapping("/roles")
    public ResponseEntity<?> editRoles(@RequestBody UserDto userDto,
                                       @RequestHeader(value = "action", required = false) String action) {
        String username = securityUtil.getSessionUser();
        if (username == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not logged in to change user details");

        if (userDto.getRoles() == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to get roles");

        boolean edit = switch (action) {
            case "add" -> userService.addRoles(username, userDto.getRoles());
            case "remove" -> userService.removeRoles(username, userDto.getRoles());
            default -> false;
        };

        if (!edit)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to update roles");

        String role = userDto.getRoles().toString();
        return ResponseEntity.status(HttpStatus.OK).body("Successfully edited role " + role);
    }

    @GetMapping("/roles")
    public List<String> getAllRoles() {
        return userService.getAllRoles();
    }

    @GetMapping("/role/user")
    public ResponseEntity<Set<String>> getUserRoles() {
        String username = securityUtil.getSessionUser();
        if (username == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.emptySet());
        if (userService.getUserRoles(username) != null)
            return ResponseEntity.status(HttpStatus.OK).body(userService.getUserRoles(username));
        else return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new HashSet<>(Collections.emptySet()));
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> isLoggedIn() {
        if (securityUtil.getSessionUser() == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
        return ResponseEntity.status(HttpStatus.OK).body(true);
    }

}
