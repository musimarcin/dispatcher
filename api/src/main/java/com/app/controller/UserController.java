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
            return new ResponseEntity<>("Already logged in", HttpStatus.FORBIDDEN);
        }
        if (bindingResult.hasErrors()) {
            StringBuilder errorMessage = new StringBuilder();
            bindingResult.getAllErrors().forEach(e -> {
                errorMessage.append(e.getDefaultMessage()).append(" ");
            });
            return new ResponseEntity<>(errorMessage.toString(), HttpStatus.BAD_REQUEST);
        }
        if (userService.checkUserAndEmail(userDto.getUsername(), userDto.getEmail()))
            return new ResponseEntity<>("Username or email taken", HttpStatus.BAD_REQUEST);
        userService.createUser(userDtoConverter.convert(userDto));
        return new ResponseEntity<>("User Registered", HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody UserDto userDto, HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userDto.getUsername(), userDto.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtGenerator.generateToken(authentication);
            response.addCookie(getNewCookie(token));
            return new ResponseEntity<>("Logged in successfully", HttpStatus.OK);
        } catch (AuthenticationException e) {
            return new ResponseEntity<>("Credentials incorrect", HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logoutUser(HttpServletResponse response) {
        deleteCookie(response);
        SecurityContextHolder.clearContext();
        return new ResponseEntity<>("Logged out successfully", HttpStatus.OK);
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
                    return new ResponseEntity<>("User deleted successfully", HttpStatus.OK);
                }
                else
                    return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);

            } catch (Exception e) {
                return new ResponseEntity<>("Failed to delete user", HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } else {
            return new ResponseEntity<>("You are not logged in to delete user", HttpStatus.UNAUTHORIZED);
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
                        return new ResponseEntity<>("Failed to change username or username already taken", HttpStatus.INTERNAL_SERVER_ERROR);
                }
                if (!userDto.getPassword().isEmpty()) {
                    if (userService.changePassword(username, userDto.getPassword()))
                        message.append("password ");
                    else
                        return new ResponseEntity<>("Failed to change password", HttpStatus.INTERNAL_SERVER_ERROR);
                }
                if (!userDto.getEmail().isEmpty()) {
                    if (userService.changeEmail(username, userDto.getEmail()))
                        message.append("email ");
                    else
                        return new ResponseEntity<>("Failed to change email", HttpStatus.INTERNAL_SERVER_ERROR);
                }
                return new ResponseEntity<>("Successfully changed " + message, HttpStatus.OK);

            } catch (Exception e) {
                return new ResponseEntity<>("Failed to change user details", HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } else {
            return new ResponseEntity<>("You are not logged in to change user details", HttpStatus.UNAUTHORIZED);
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
                        return new ResponseEntity<>("Successfully added role " + role, HttpStatus.OK);
                    }
                    return new ResponseEntity<>("Failed to add role", HttpStatus.BAD_REQUEST);
                }
            } catch (Exception e) {
                return new ResponseEntity<>("Failed to change user details", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<>("You are not logged in to change user details", HttpStatus.UNAUTHORIZED);
    }

    @PostMapping("/change/role/remove")
    public ResponseEntity<String> userRemoveRole(@RequestBody UserDto userDto, HttpServletResponse response) {
        String username = SecurityUtil.getSessionUser();
        if (username != null) {
            try {
                if (userDto.getRoles() != null) {
                    if (userService.removeRoles(username, userDto.getRoles())) {
                        String role = userDto.getRoles().toString();
                        return new ResponseEntity<>("Successfully removed role " + role, HttpStatus.OK);
                    }
                    return new ResponseEntity<>("Failed to remove role", HttpStatus.BAD_REQUEST);
                }
            } catch (Exception e) {
                return new ResponseEntity<>("Failed to change user details", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<>("You are not logged in to change user details", HttpStatus.UNAUTHORIZED);
    }


    @GetMapping("/roles")
    public List<String> getAllRoles() {
        return userService.getAllRoles();
    }

    @GetMapping("/role/user")
    public ResponseEntity<Set<String>> getUserRoles() {
        String username = SecurityUtil.getSessionUser();
        if (username != null) {
            return new ResponseEntity<>(userService.getUserRoles(username), HttpStatus.OK);
        }
        return new ResponseEntity<>(Collections.emptySet(), HttpStatus.UNAUTHORIZED);
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> isLoggedIn() {
        if (SecurityUtil.getSessionUser() != null)
            return new ResponseEntity<>(true, HttpStatus.OK);
        return new ResponseEntity<>(false, HttpStatus.UNAUTHORIZED);
    }

}
