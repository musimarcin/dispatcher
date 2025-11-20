package com.app.controller;

import com.app.dto.UserDto;
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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JWTGenerator jwtGenerator;
    private final AuthenticationManager authenticationManager;
    private final SecurityUtil securityUtil;

    public AuthController(UserService userService, JWTGenerator jwtGenerator, AuthenticationManager authenticationManager, SecurityUtil securityUtil) {
        this.userService = userService;
        this.jwtGenerator = jwtGenerator;
        this.authenticationManager = authenticationManager;
        this.securityUtil = securityUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> createUser(@RequestBody @Valid UserDto userDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder errorMessage = new StringBuilder();
            bindingResult.getAllErrors().forEach(e ->
                    errorMessage.append(e.getDefaultMessage()).append(" "));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", errorMessage.toString()));
        }
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
        if (securityUtil.getSessionUser() == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in");
        securityUtil.deleteCookie(response);
        securityUtil.clear();
        return ResponseEntity.status(HttpStatus.OK).body("Successfully logged out");
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> isLoggedIn() {
        if (securityUtil.getSessionUser() == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
        return ResponseEntity.status(HttpStatus.OK).body(true);
    }
}
