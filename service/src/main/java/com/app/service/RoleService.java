package com.app.service;

import com.app.model.Role;
import com.app.model.UserEntity;
import com.app.repository.UserRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleService {

    private final UserRepo userRepo;

    public RoleService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Transactional
    public boolean addRoles(String username, Set<String> newRoles) {
        Optional<UserEntity> user = userRepo.findByUsername(username);
        if (user.isEmpty()) return false;

        int changes = 0;
        Set<Role> roles = newRoles.stream()
                .map(role -> "ROLE_" + role)
                .map(Role::valueOf)
                .collect(Collectors.toSet());
        for (Role r : roles) {
            if (!user.get().getRoles().contains(r)) {
                user.get().getRoles().add(r);
                changes++;
            }
        }
        return changes > 0;
    }

    @Transactional
    public boolean removeRoles(String username, Set<String> newRoles) {
        Optional<UserEntity> user = userRepo.findByUsername(username);
        if (user.isEmpty()) return false;

        Set<Role> roles = newRoles.stream()
                .map(role -> "ROLE_" + role)
                .map(Role::valueOf)
                .collect(Collectors.toSet());
        int changes = 0;
        for (Role r : roles) {
            if (user.get().getRoles().contains(r)) {
                user.get().getRoles().remove(r);
                changes++;
            }
        }
        return changes > 0;
    }

    public Set<String> getAllRoles() {
        return Arrays.stream(Role.values()).map(String::valueOf)
                .map(role -> role.substring(5)).collect(Collectors.toSet());
    }

    public Set<String> getUserRoles(Long id) {
        Optional<UserEntity> user = userRepo.findById(id);
        if (user.isEmpty()) return Set.of();

        Set<Role> roles = user.get().getRoles();
        return roles.stream().map(String::valueOf)
                .map(role -> role.substring(5)).collect(Collectors.toSet());
    }
}
