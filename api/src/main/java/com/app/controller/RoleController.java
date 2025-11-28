package com.app.controller;


import com.app.dto.RoleChangeRequest;
import com.app.security.SecurityUtil;
import com.app.service.RoleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final SecurityUtil securityUtil;
    private final RoleService roleService;

    public RoleController(SecurityUtil securityUtil, RoleService roleService) {
        this.securityUtil = securityUtil;
        this.roleService = roleService;
    }

    @PatchMapping("/add")
    public ResponseEntity<?> addRole(@RequestBody RoleChangeRequest request) {
        String username = securityUtil.getSessionUser();
        if (username == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "You are not logged in to change user details"));

        if (request.roles().isEmpty())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Failed to get roles"));

        if (!roleService.addRoles(username, request.roles()))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Failed to add role"));

        String role = request.roles().toString();
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Successfully added role " + role));
    }

    @PatchMapping("/remove")
    public ResponseEntity<?> removeRole(@RequestBody RoleChangeRequest request) {
        String username = securityUtil.getSessionUser();
        if (username == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "You are not logged in to change user details"));

        if (request.roles().isEmpty())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Failed to get roles"));

        if (!roleService.removeRoles(username, request.roles()))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Failed to remove role"));

        String role = request.roles().toString();
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Successfully removed role " + role));
    }

    @GetMapping
    public ResponseEntity<?> getAllRoles() {
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("body", roleService.getAllRoles()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserRoles(@PathVariable Long id) {
        String username = securityUtil.getSessionUser();
        if (username == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not logged in"));
        if (roleService.getUserRoles(id).isEmpty())
            return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "No roles found"));

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("body", roleService.getUserRoles(id)));
    }
}
