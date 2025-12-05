package com.app.controller;

import com.app.dto.requests.EmailChangeRequest;
import com.app.dto.requests.PasswordChangeRequest;
import com.app.dto.UserInfo;
import com.app.dto.requests.UsernameChangeRequest;
import com.app.security.CustomUserDetailService;
import com.app.security.JWTGenerator;
import com.app.security.SecurityUtil;
import com.app.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final JWTGenerator jwtGenerator;
    private final CustomUserDetailService customUserDetailService;
    private final SecurityUtil securityUtil;

    public UserController(UserService userService, JWTGenerator jwtGenerator,
                          CustomUserDetailService customUserDetailService, SecurityUtil securityUtil) {
        this.userService = userService;
        this.jwtGenerator = jwtGenerator;
        this.customUserDetailService = customUserDetailService;
        this.securityUtil = securityUtil;
    }

    @DeleteMapping
    public ResponseEntity<?> deleteUser(HttpServletResponse response) {
        String username = securityUtil.getSessionUser();
        if (username == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "You must be logged in to delete user"));
        if (!userService.deleteUser(username))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User not found"));

        securityUtil.deleteCookie(response);
        securityUtil.clear();
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "User deleted successfully"));
    }

    @PutMapping("/username")
    public ResponseEntity<?> changeUsername(@RequestBody @Valid UsernameChangeRequest request,
                                            HttpServletResponse response) {
        String username = securityUtil.getSessionUser();
        if (username == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not logged in"));

        boolean changed = userService.changeUsername(username, request.newUsername());

        if (!changed)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Username already taken"));

        UserDetails userDetails = customUserDetailService.loadUserByUsername(request.newUsername());
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String newToken = jwtGenerator.generateToken(authentication);
        response.addCookie(securityUtil.getNewCookie(newToken));

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Successfully changed username"));
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody @Valid PasswordChangeRequest request) {
        String username = securityUtil.getSessionUser();
        if (username == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not logged in"));

        boolean changed = userService.changePassword(username, request.newPassword());
        if (!changed)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Failed to change password"));

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Successfully changed password"));
    }

    @PutMapping("/email")
    public ResponseEntity<?> changeEmail(@RequestBody @Valid EmailChangeRequest request) {
        String username = securityUtil.getSessionUser();
        if (username == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not logged in"));

        boolean changed = userService.changeEmail(username, request.newEmail());
        if (!changed)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Failed to change email"));

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Successfully changed email"));
    }

    @GetMapping("/drivers")
    public ResponseEntity<?> getAllDrivers() {
        String username = securityUtil.getSessionUser();
        if (username == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not logged in"));
        Set<UserInfo> drivers = userService.getAllDrivers(username);
        if (drivers.isEmpty())
            return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Drivers not found"));
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("body" , drivers));
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        String username = securityUtil.getSessionUser();
        if (username == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not logged in"));
        List<UserInfo> users = userService.getAllUsers(username);
        if (users == null)
            return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Users not found"));
        if (users.isEmpty())
            return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Users not found"));
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("body", users));
    }

}
