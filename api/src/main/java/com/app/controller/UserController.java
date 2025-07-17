package com.app.controller;

import com.app.request.CreateUserRequest;
import com.app.security.JWTGenerator;
import com.app.security.SecurityUtil;
import com.app.service.UserService;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;
    private final JWTGenerator jwtGenerator;
    private final AuthenticationManager authenticationManager;

    public UserController(UserService userService, JWTGenerator jwtGenerator, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.jwtGenerator = jwtGenerator;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public ResponseEntity<String> createUser(@RequestBody CreateUserRequest request) {
        String username = SecurityUtil.getSessionUser();
        if (username != null) {
            return new ResponseEntity<>("Already logged in", HttpStatus.FORBIDDEN);
        }
        if (userService.checkUserAndEmail(request.getUsername(), request.getEmail()))
            return new ResponseEntity<>("Username taken", HttpStatus.BAD_REQUEST);
        if (request.getUsername() == null ||
                request.getPassword() == null ||
                request.getEmail() == null ||
                request.getRoles() == null) {
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

    @PostMapping("/login")
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

    @PostMapping("/logout")
    public ResponseEntity<String> logoutUser(HttpServletResponse response) {
        SecurityContextHolder.clearContext();
        Cookie cookie = new Cookie("token", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setSecure(false);
        response.addCookie(cookie);
        return new ResponseEntity<>("Logged out successfully", HttpStatus.OK);
    }

    private static Cookie getCookie(String token) {
        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(3500);
        cookie.setSecure(false);
        return cookie;
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteUser(HttpServletResponse response) {
        String username = SecurityUtil.getSessionUser();
        if (username != null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            try {
                SecurityContextHolder.clearContext();
                Cookie cookie = new Cookie("token", null);
                cookie.setHttpOnly(true);
                cookie.setSecure(false);
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

    @PutMapping("/change")
    public ResponseEntity<String> changeUser(@RequestBody CreateUserRequest request, HttpServletResponse response) {
        String username = SecurityUtil.getSessionUser();
        if (username != null) {
            try {
                StringBuilder message = new StringBuilder();
                if (request.getUsername() != null) {
                    if (userService.changeUsername(username, request.getUsername()))
                        message.append("username ");
                    else
                        return new ResponseEntity<>("Failed to change user details", HttpStatus.INTERNAL_SERVER_ERROR);
                }
                if (request.getPassword() != null) {
                    if (userService.changePassword(username, request.getPassword()))
                        message.append("password ");
                    else
                        return new ResponseEntity<>("Failed to change user details", HttpStatus.INTERNAL_SERVER_ERROR);
                }
                if (request.getEmail() != null) {
                    if (userService.changeEmail(username, request.getEmail()))
                        message.append("email ");
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

    @PostMapping("/change/role/add")
    public ResponseEntity<String> userAddRole(@RequestBody CreateUserRequest request, HttpServletResponse response) {
        String username = SecurityUtil.getSessionUser();
        if (username != null) {
            try {
                if (request.getRoles() != null) {
                    if (userService.addRoles(username, request.getRoles())) {
                        String role = request.getRoles().toString();
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
    public ResponseEntity<String> userRemoveRole(@RequestBody CreateUserRequest request, HttpServletResponse response) {
        String username = SecurityUtil.getSessionUser();
        if (username != null) {
            try {
                if (request.getRoles() != null) {
                    if (userService.removeRoles(username, request.getRoles())) {
                        String role = request.getRoles().toString();
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
    public List<String> getRoles() {
        return userService.getRoles();
    }

    @GetMapping("/role/user")
    public ResponseEntity<List<String>> getUserRoles() {
        String username = SecurityUtil.getSessionUser();
        System.out.println("USERNAME " + username);
        if (username != null) {
            System.out.println("GOING INTO SERVICE");
            return new ResponseEntity<>(userService.getUserRoles(username), HttpStatus.OK);
        }
        return new ResponseEntity<>(Collections.emptyList(), HttpStatus.UNAUTHORIZED);
    }

}
