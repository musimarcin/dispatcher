package com.app.service;

import com.app.converters.UserToUserDto;
import com.app.dto.UserDto;
import com.app.model.Role;
import com.app.model.UserEntity;
import com.app.repository.UserRepo;
import com.app.converters.UserDtoToUser;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final UserDtoToUser userDtoConverter;
    private final UserToUserDto userConverter;

    public UserService(UserRepo userRepo, PasswordEncoder passwordEncoder, UserDtoToUser userDtoConverter, UserToUserDto userConverter) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.userDtoConverter = userDtoConverter;
        this.userConverter = userConverter;
    }

    private boolean checkUserAndEmail(String username, String email) {
        return userRepo.existsByUsername(username) && userRepo.existsByEmail(email);
    }

    public UserDto createUser(UserDto userDto) {
        if (checkUserAndEmail(userDto.getUsername(), userDto.getEmail())) return null;

        UserEntity user = userDtoConverter.convert(userDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepo.save(user);
        return userConverter.convert(user);
    }

    @Transactional
    public boolean deleteUser(String username) {
        Optional<UserEntity> user = userRepo.findByUsername(username);
        if (user.isEmpty()) return false;

        userRepo.delete(user.get());
        return true;
    }

    @Transactional
    public boolean changeUsername(String oldUsername, String newUsername) {
        Optional<UserEntity> user = userRepo.findByUsername(oldUsername);
        if (user.isEmpty()) return false;

        if (userRepo.findByUsername(newUsername).isPresent())
            return false;

        user.get().setUsername(newUsername);
        return true;
    }

    @Transactional
    public boolean changePassword(String username, String password) {
        Optional<UserEntity> user = userRepo.findByUsername(username);
        if (user.isEmpty()) return false;

        user.get().setPassword(passwordEncoder.encode(password));
        return true;
    }

    @Transactional
    public boolean changeEmail(String username, String email) {
        Optional<UserEntity> user = userRepo.findByUsername(username);
        if (user.isEmpty()) return false;

        user.get().setEmail(email);
        return true;
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

    public List<String> getAllRoles() {
        List<String> res = new ArrayList<>();
        for (Role r : Role.values())
            res.add(r.name().substring(5));
        res.remove(0); //removing role admin
        return res;
    }

    public Set<String> getUserRoles(String username) {
        Optional<UserEntity> user = userRepo.findByUsername(username);
        if (user.isEmpty()) return Set.of();

        Set<Role> roles = user.get().getRoles();
        Set<String> res = new HashSet<>();
        for (Role r : roles) {
            res.add(r.name().substring(5));
        }
        return res;
    }
}
