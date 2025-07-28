package com.app.service;

import com.app.model.Role;
import com.app.repository.RoleRepo;
import com.app.request.CreateUserRequest;
import com.app.model.UserEntity;
import com.app.repository.UserRepo;
import com.app.security.CustomUserDetailService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final PasswordEncoder passwordEncoder;
    private final List<Role> roles;

    public UserService(UserRepo userRepo, RoleRepo roleRepo, PasswordEncoder passwordEncoder, List<Role> roles) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
        this.roles = roleRepo.findAll();
    }

    public boolean checkUserAndEmail(String username, String email) {
        return userRepo.existsByUsername(username) && userRepo.existsByEmail(email);
    }

    public void createUser(CreateUserRequest request) {
        UserEntity user = new UserEntity();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRoles(roleRepo.findByNameIn(request.getRoles()));
        user.setRegistered(Instant.now());
        userRepo.save(user);
    }

    @Transactional
    public boolean deleteUser(String username) {
        UserEntity user = userRepo.findByUsername(username);
        if (user != null) {
            userRepo.delete(user);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean changeUsername(String oldUsername, String newUsername) {
        if (userRepo.findByUsername(oldUsername) != null
                && userRepo.findByUsername(newUsername) == null) {
            int changes = userRepo.updateUsername(newUsername, oldUsername);
            return changes > 0;
        } else return false;
    }

    @Transactional
    public boolean changePassword(String username, String password) {
        if (userRepo.findByUsername(username) != null) {
            int changes = userRepo.updatePassword(passwordEncoder.encode(password), username);
            return changes > 0;
        } else return false;
    }

    @Transactional
    public boolean changeEmail(String username, String email) {
        if (userRepo.findByUsername(username) != null) {
            int changes = userRepo.updateEmail(email, username);
            return changes > 0;
        } else return false;
    }

    @Transactional
    public boolean addRoles(String username, List<String> newRoles) {
        UserEntity user = userRepo.findByUsername(username);
        List<Role> roles = newRoles.stream()
                .map(name -> roleRepo.findByName(name).orElseThrow(() -> new RuntimeException("Role not found")))
                .toList();
        int changes = 0;
        if (user != null) {
            for (Role r : roles) {
                if (!user.getRoles().contains(r)) {
                    user.getRoles().add(r);
                    changes++;
                }
            }
        } else {
            return false;
        }
        return changes > 0;
    }

    @Transactional
    public boolean removeRoles(String username, List<String> newRoles) {
        UserEntity user = userRepo.findByUsername(username);
        List<Role> roles = newRoles.stream()
                .map(name -> roleRepo.findByName(name).orElseThrow(() -> new RuntimeException("Role not found")))
                .toList();
        int changes = 0;
        if (user != null) {
            for (Role r : roles) {
                if (user.getRoles().contains(r)) {
                    user.getRoles().remove(r);
                    changes++;
                }
            }
        } else return false;
        return changes > 0;
    }

    public List<String> getAllRoles() {
        List<String> res = new ArrayList<>();
        for (Role r : roles)
            res.add(r.getName().substring(5));
        res.removeFirst(); //removing role admin
        return res;
    }

    public List<String> getUserRoles(String username) {
        UserEntity user = userRepo.findByUsername(username);
        List<Role> roles = user.getRoles();
        List<String> res = new ArrayList<>();
        for (Role r : roles) {
            res.add(r.getName().substring(5));
        }
        return res;
    }
}
