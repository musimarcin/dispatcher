package com.app.service;

import com.app.dto.UserInfo;
import com.app.utils.UserToUserDto;
import com.app.dto.UserDto;
import com.app.model.Role;
import com.app.model.UserEntity;
import com.app.repository.UserRepo;
import com.app.utils.UserDtoToUser;
import com.app.utils.UserToUserInfo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final UserDtoToUser userDtoConverter;
    private final UserToUserDto userConverter;
    private final UserToUserInfo userInfo;

    public UserService(UserRepo userRepo, PasswordEncoder passwordEncoder, UserDtoToUser userDtoConverter, UserToUserDto userConverter, UserToUserInfo userInfo) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.userDtoConverter = userDtoConverter;
        this.userConverter = userConverter;
        this.userInfo = userInfo;
    }

    private boolean checkUserAndEmail(String username, String email) {
        return userRepo.existsByUsername(username) && userRepo.existsByEmail(email);
    }

    public UserDto createUser(UserDto userDto) {
        if (checkUserAndEmail(userDto.getUsername(), userDto.getEmail())) return null;
        userDto.setRegistered(Instant.now());
        UserEntity user = userDtoConverter.convert(userDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepo.save(user);
        return userConverter.convert(user);
    }

    public UserInfo getUserInfo(String username) {
        Optional<UserEntity> user = userRepo.findByUsername(username);
        return user.map(userInfo::convert).orElse(null);
    }

    public Set<UserInfo> getAllDrivers(String username) {
        Optional<UserEntity> user = userRepo.findByUsername(username);
        if (user.isEmpty()) return Set.of();
        Set<UserEntity> drivers = userRepo.getAllDrivers(Role.ROLE_DRIVER);
        return drivers.stream().filter(u -> !u.getRoles().contains(Role.ROLE_ADMIN))
                .map(userInfo::convert).collect(Collectors.toSet());
    }

    public List<UserInfo> getAllUsers(String username) {
        Optional<UserEntity> user = userRepo.findByUsername(username);
        if (user.isEmpty()) return List.of();
        if (!user.get().getRoles().contains(Role.ROLE_ADMIN)) return List.of();
        List<UserEntity> users = userRepo.findAll();
        return users.stream().map(userInfo::convert).collect(Collectors.toList());
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


}
