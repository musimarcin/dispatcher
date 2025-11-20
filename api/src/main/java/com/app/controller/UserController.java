package com.app.controller;

import com.app.dto.*;
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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You must be logged in to delete user");
        if (!userService.deleteUser(username))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");

        securityUtil.deleteCookie(response);
        securityUtil.clear();
        return ResponseEntity.status(HttpStatus.OK).body("User deleted successfully");
    }

    @PutMapping("/username")
    public ResponseEntity<?> changeUsername(@RequestBody @Valid UsernameChangeRequest request,
                                            HttpServletResponse response) {
        String username = securityUtil.getSessionUser();
        if (username == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in");

        boolean changed = userService.changeUsername(username, request.newUsername());

        if (!changed)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username already taken");

        UserDetails userDetails = customUserDetailService.loadUserByUsername(request.newUsername());
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String newToken = jwtGenerator.generateToken(authentication);
        response.addCookie(securityUtil.getNewCookie(newToken));

        return ResponseEntity.status(HttpStatus.OK).body("Successfully changed username");
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody @Valid PasswordChangeRequest request) {
        String username = securityUtil.getSessionUser();
        if (username == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in");

        boolean changed = userService.changePassword(username, request.newPassword());
        if (!changed)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to change password");

        return ResponseEntity.status(HttpStatus.OK).body("Successfully changed password");
    }

    @PutMapping("/email")
    public ResponseEntity<?> changeEmail(@RequestBody @Valid EmailChangeRequest request) {
        String username = securityUtil.getSessionUser();
        if (username == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in");

        boolean changed = userService.changeEmail(username, request.newEmail());
        if (!changed)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to change email");

        return ResponseEntity.status(HttpStatus.OK).body("Successfully changed email");
    }

    @PostMapping("/roles")
    public ResponseEntity<?> addRole(@RequestBody RoleChangeRequest request) {
        String username = securityUtil.getSessionUser();
        if (username == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not logged in to change user details");

        if (request.roles().isEmpty())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to get roles");

        if (!userService.addRoles(username, request.roles()))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to add role");

        String role = request.roles().toString();
        return ResponseEntity.status(HttpStatus.OK).body("Successfully added role " + role);
    }

    @DeleteMapping("/roles")
    public ResponseEntity<?> removeRole(@RequestBody RoleChangeRequest request) {
        String username = securityUtil.getSessionUser();
        if (username == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not logged in to change user details");

        if (request.roles().isEmpty())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to get roles");

        if (!userService.removeRoles(username, request.roles()))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to remove role");

        String role = request.roles().toString();
        return ResponseEntity.status(HttpStatus.OK).body("Successfully removed role " + role);
    }

    @GetMapping("/roles")
    public List<String> getAllRoles() {
        return userService.getAllRoles();
    }

    @GetMapping("/roles/user")
    public ResponseEntity<Set<String>> getUserRoles() {
        String username = securityUtil.getSessionUser();
        if (username == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.emptySet());
        if (userService.getUserRoles(username).isEmpty())
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new HashSet<>(Collections.emptySet()));

        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserRoles(username));
    }



}
