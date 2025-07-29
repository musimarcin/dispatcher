package com.app.service;

import com.app.model.Role;
import com.app.model.UserEntity;
import com.app.repository.UserRepo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepo userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean checkUserAndEmail(String username, String email) {
        return userRepo.existsByUsername(username) && userRepo.existsByEmail(email);
    }

    public void createUser(UserEntity user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
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
        UserEntity user = userRepo.findByUsername(oldUsername);
        if (user != null
                && userRepo.findByUsername(newUsername) == null) {
            user.setUsername(newUsername);
            return true;
        } else return false;
    }

    @Transactional
    public boolean changePassword(String username, String password) {
        UserEntity user = userRepo.findByUsername(username);
        if (user != null) {
            user.setPassword(passwordEncoder.encode(password));
            return true;
        } else return false;
    }

    @Transactional
    public boolean changeEmail(String username, String email) {
        UserEntity user = userRepo.findByUsername(username);
        if (user != null) {
            user.setEmail(email);
            return true;
        } else return false;
    }

    @Transactional
    public boolean addRoles(String username, Set<String> newRoles) {
        UserEntity user = userRepo.findByUsername(username);
        int changes = 0;
        if (user != null) {
            Set<Role> roles = newRoles.stream()
                    .map(Role::valueOf)
                    .collect(Collectors.toSet());
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
    public boolean removeRoles(String username, Set<String> newRoles) {
        UserEntity user = userRepo.findByUsername(username);
        Set<Role> roles = newRoles.stream()
                .map(Role::valueOf)
                .collect(Collectors.toSet());
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
        for (Role r : Role.values())
            res.add(r.name().substring(5));
        res.removeFirst(); //removing role admin
        return res;
    }

    public Set<String> getUserRoles(String username) {
        UserEntity user = userRepo.findByUsername(username);
        Set<Role> roles = user.getRoles();
        Set<String> res = new HashSet<>();
        for (Role r : roles) {
            res.add(r.name().substring(5));
        }
        return res;
    }
}
