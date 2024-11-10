package com.api.dispatcher.controller;

import com.api.dispatcher.dto.CreateUserRequest;
import com.api.dispatcher.security.JWTGenerator;
import com.api.dispatcher.security.SecurityUtil;
import com.api.dispatcher.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/")
public class UserController {

    private final UserService userService;
    private final JWTGenerator jwtGenerator;
    private final AuthenticationManager authenticationManager;

    public UserController(UserService userService, JWTGenerator jwtGenerator, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.jwtGenerator = jwtGenerator;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("register")
    public ResponseEntity<String> createUser(@RequestBody CreateUserRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            return new ResponseEntity<>("Already logged in", HttpStatus.FORBIDDEN);
        }
        if (userService.checkUserAndEmail(request.getUsername(), request.getEmail()))
            return new ResponseEntity<>("Username taken", HttpStatus.BAD_REQUEST);
        if (request.getUsername() == null ||
                request.getPassword() == null ||
                request.getEmail() == null) {
            return new ResponseEntity<>("Fields missing", HttpStatus.BAD_REQUEST);
        }
        if (request.getUsername().isEmpty() ||
                request.getPassword().isEmpty() ||
                request.getEmail().isEmpty()) {
            return new ResponseEntity<>("Fields cannot be empty", HttpStatus.BAD_REQUEST);
        }
        userService.createUser(request);
        return new ResponseEntity<>("User Registered", HttpStatus.CREATED);
    }

    @PostMapping("login")
    public ResponseEntity<String> loginUser(@RequestBody CreateUserRequest request, HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtGenerator.generateToken(authentication);
            Cookie cookie = getCookie(token);
            response.addCookie(cookie);
            return new ResponseEntity<>("Logged in successfully", HttpStatus.OK);
        } catch (AuthenticationException e) {
            return new ResponseEntity<>("Credentials incorrect", HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("logout")
    public ResponseEntity<String> logoutUser(HttpServletResponse response) {
        SecurityContextHolder.clearContext();
        Cookie cookie = new Cookie("token", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setSecure(true);
        response.addCookie(cookie);
        return new ResponseEntity<>("Logged out successfully", HttpStatus.OK);
    }

    private static Cookie getCookie(String token) {
        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(3500);
        cookie.setSecure(true);
        return cookie;
    }

    @DeleteMapping("delete")
    public ResponseEntity<String> deleteUser(HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            try {
                SecurityContextHolder.clearContext();
                Cookie cookie = new Cookie("token", null);
                cookie.setHttpOnly(true);
                cookie.setSecure(true);
                cookie.setPath("/");
                cookie.setMaxAge(0);
                response.addCookie(cookie);

                if (userService.deleteUser(authentication.getName()))
                    return new ResponseEntity<>("User deleted successfully", HttpStatus.OK);
                else
                    return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);

            } catch (Exception e) {
                return new ResponseEntity<>("Failed to delete user", HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } else {
            return new ResponseEntity<>("You are not logged in to delete user", HttpStatus.UNAUTHORIZED);
        }
    }

    @PutMapping("change")
    public ResponseEntity<String> changeUser(@RequestBody CreateUserRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            String username = SecurityUtil.getSessionUser();

            try {
                String message = "";
                if (request.getUsername() != null) {
                    if (userService.changeUsername(username, request.getUsername()))
                        message = "username";
                    else
                        return new ResponseEntity<>("Failed to change user details", HttpStatus.INTERNAL_SERVER_ERROR);
                }
                if (request.getPassword() != null) {
                    if (userService.changePassword(username, request.getPassword()))
                        message = "password";
                    else
                        return new ResponseEntity<>("Failed to change user details", HttpStatus.INTERNAL_SERVER_ERROR);
                }
                if (request.getEmail() != null) {
                    if (userService.changeEmail(username, request.getEmail()))
                        message = "email";
                    else
                        return new ResponseEntity<>("Failed to change user details", HttpStatus.INTERNAL_SERVER_ERROR);
                }

                return new ResponseEntity<>("Successfully changed " + message, HttpStatus.OK);

            } catch (Exception e) {
                return new ResponseEntity<>("Failed to change user details", HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } else {
            return new ResponseEntity<>("You are not logged in to change user details", HttpStatus.UNAUTHORIZED);
        }
    }
}
