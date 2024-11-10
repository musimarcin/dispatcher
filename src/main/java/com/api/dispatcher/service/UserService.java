package com.api.dispatcher.service;

import com.api.dispatcher.dto.CreateUserRequest;
import com.api.dispatcher.model.UserEntity;
import com.api.dispatcher.repository.UserRepo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

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

    public void createUser(CreateUserRequest request) {
        UserEntity user = new UserEntity();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRoles(Collections.singletonList(request.getRole()));
        userRepo.save(user);
    }

    @Transactional
    public boolean deleteUser(String username) {
        UserEntity user = userRepo.findByUsername(username);
        if (user != null) {
            userRepo.delete(user);
            return true;
        } else return false;
    }

    @Transactional
    public boolean changeUsername(String oldUsername, String newUsername) {
        if (userRepo.findByUsername(oldUsername) != null) {
            int changes = userRepo.updateUsername(oldUsername, newUsername);
            return changes > 0;
        } else return false;
    }

    @Transactional
    public boolean changePassword(String username, String password) {
        if (userRepo.findByUsername(username) != null) {
            int changes = userRepo.updatePassword(username, passwordEncoder.encode(password));
            return changes > 0;
        } else return false;
    }

    @Transactional
    public boolean changeEmail(String username, String email) {
        if (userRepo.findByUsername(username) != null) {
            int changes = userRepo.updateEmail(username, email);
            return changes > 0;
        } else return false;
    }
}
