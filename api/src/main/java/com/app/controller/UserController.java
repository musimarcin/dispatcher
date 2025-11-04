package com.app.controller;

import com.app.dto.UserDto;
import com.app.security.CustomUserDetailService;
import com.app.security.JWTGenerator;
import com.app.security.SecurityUtil;
import com.app.service.UserService;
import com.app.utils.converters.UserDtoToUser;
import jakarta.servlet.http.Cookie;
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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;
    private final JWTGenerator jwtGenerator;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailService customUserDetailService;
    private final UserDtoToUser userDtoConverter;

    public UserController(UserService userService, JWTGenerator jwtGenerator, AuthenticationManager authenticationManager, CustomUserDetailService customUserDetailService, UserDtoToUser userDtoConverter) {
        this.userService = userService;
        this.jwtGenerator = jwtGenerator;
        this.authenticationManager = authenticationManager;
        this.customUserDetailService = customUserDetailService;
        this.userDtoConverter = userDtoConverter;
    }

    @PostMapping("/register")
    public ResponseEntity<String> createUser(@RequestBody @Valid UserDto userDto, BindingResult bindingResult) {
        String username = SecurityUtil.getSessionUser();
        if (username != null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Already logged in");
        }
        if (bindingResult.hasErrors()) {
            StringBuilder errorMessage = new StringBuilder();
            bindingResult.getAllErrors().forEach(e -> {
                errorMessage.append(e.getDefaultMessage()).append(" ");
            });
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage.toString());
        }
        if (userService.checkUserAndEmail(userDto.getUsername(), userDto.getEmail()))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username or email taken");
        userService.createUser(userDtoConverter.convert(userDto));
        return ResponseEntity.status(HttpStatus.CREATED).body("User Registered");
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody UserDto userDto, HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userDto.getUsername(), userDto.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtGenerator.generateToken(authentication);
            response.addCookie(getNewCookie(token));
            return ResponseEntity.status(HttpStatus.OK).body("Logged in successfully");
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credentials incorrect");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logoutUser(HttpServletResponse response) {
        deleteCookie(response);
        SecurityContextHolder.clearContext();
        return ResponseEntity.status(HttpStatus.OK).body("Logged out successfully");
    }

    private static void deleteCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("token", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setSecure(false);
        response.addCookie(cookie);
    }

    private static Cookie getNewCookie (String token) {
        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(3500);
        cookie.setSecure(false); // only for development, in production: true
        return cookie;
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteUser(HttpServletResponse response) {
        String username = SecurityUtil.getSessionUser();
        if (username != null) {
            try {
                if (userService.deleteUser(username)) {
                    deleteCookie(response);
                    SecurityContextHolder.clearContext();
                    return ResponseEntity.status(HttpStatus.OK).body("User deleted successfully");
                }
                else
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");

            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete user");
            }

        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not logged in to delete user");
        }
    }

    @PutMapping("/change")
    public ResponseEntity<String> changeUser(@RequestBody UserDto userDto, HttpServletResponse response) {
        String username = SecurityUtil.getSessionUser();
        if (username != null) {
            try {
                StringBuilder message = new StringBuilder();
                if (!userDto.getUsername().isEmpty()) {
                    if (userService.changeUsername(username, userDto.getUsername())) {
                        UserDetails userDetails = customUserDetailService.loadUserByUsername(userDto.getUsername());
                        Authentication authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        String token = jwtGenerator.generateToken(authentication);
                        response.addCookie(getNewCookie(token));
                        username = userDto.getUsername();
                        message.append("username ");
                    }
                    else
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to change username or username already taken");
                }
                if (!userDto.getPassword().isEmpty()) {
                    if (userService.changePassword(username, userDto.getPassword()))
                        message.append("password ");
                    else
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to change password");
                }
                if (!userDto.getEmail().isEmpty()) {
                    if (userService.changeEmail(username, userDto.getEmail()))
                        message.append("email ");
                    else
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to change email");
                }
                return ResponseEntity.status(HttpStatus.OK).body("Successfully changed " + message);

            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to change user details");
            }

        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not logged in to change user details");
        }
    }

    @PostMapping("/change/role/add")
    public ResponseEntity<String> userAddRole(@RequestBody UserDto userDto, HttpServletResponse response) {
        String username = SecurityUtil.getSessionUser();
        if (username != null) {
            try {
                if (userDto.getRoles() != null) {
                    if (userService.addRoles(username, userDto.getRoles())) {
                        String role = userDto.getRoles().toString();
                        return ResponseEntity.status(HttpStatus.OK).body("Successfully added role " + role);
                    }
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to add role");
                }
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to change user details");
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not logged in to change user details");
    }

    @PostMapping("/change/role/remove")
    public ResponseEntity<String> userRemoveRole(@RequestBody UserDto userDto, HttpServletResponse response) {
        String username = SecurityUtil.getSessionUser();
        if (username != null) {
            try {
                if (userDto.getRoles() != null) {
                    if (userService.removeRoles(username, userDto.getRoles())) {
                        String role = userDto.getRoles().toString();
                        return ResponseEntity.status(HttpStatus.OK).body("Successfully removed role " + role);
                    }
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to remove role");
                }
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to change user details");
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not logged in to change user details");
    }


    @GetMapping("/roles")
    public List<String> getAllRoles() {
        return userService.getAllRoles();
    }

    @GetMapping("/role/user")
    public ResponseEntity<Set<String>> getUserRoles() {
        String username = SecurityUtil.getSessionUser();
        if (username != null) {
            return ResponseEntity.status(HttpStatus.OK).body(userService.getUserRoles(username));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.emptySet());
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> isLoggedIn() {
        if (SecurityUtil.getSessionUser() != null)
            return ResponseEntity.status(HttpStatus.OK).body(true);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
    }

}
