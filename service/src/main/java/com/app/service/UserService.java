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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
        if (!checkUserAndEmail(userDto.getUsername(), userDto.getEmail())) {
            UserEntity user = userDtoConverter.convert(userDto);
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepo.save(user);
            return userConverter.convert(user);
        }
        return null;
    }

    @Transactional
    public boolean deleteUser(String username) {
        if (userRepo.findByUsername(username).isPresent()) {
            userRepo.delete(userRepo.findByUsername(username).get());
            return true;
        }
        return false;
    }

    @Transactional
    public boolean changeUsername(String oldUsername, String newUsername) {
        if (userRepo.findByUsername(oldUsername).isPresent()
                && userRepo.findByUsername(newUsername) == null) {
            UserEntity user = userRepo.findByUsername(oldUsername).get();
            user.setUsername(newUsername);
            return true;
        } else return false;
    }

    @Transactional
    public boolean changePassword(String username, String password) {
        if (userRepo.findByUsername(username).isPresent()) {
            UserEntity user = userRepo.findByUsername(username).get();
            user.setPassword(passwordEncoder.encode(password));
            return true;
        } else return false;
    }

    @Transactional
    public boolean changeEmail(String username, String email) {
        if (userRepo.findByUsername(username).isPresent()) {
            UserEntity user = userRepo.findByUsername(username).get();
            user.setEmail(email);
            return true;
        } else return false;
    }

    @Transactional
    public boolean addRoles(String username, Set<String> newRoles) {
        int changes = 0;
        if (userRepo.findByUsername(username).isPresent()) {
            UserEntity user = userRepo.findByUsername(username).get();
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
        Set<Role> roles = newRoles.stream()
                .map(Role::valueOf)
                .collect(Collectors.toSet());
        int changes = 0;
        if (userRepo.findByUsername(username).isPresent()) {
            UserEntity user = userRepo.findByUsername(username).get();
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
        res.remove(0); //removing role admin
        return res;
    }

    public Set<String> getUserRoles(String username) {
        if (userRepo.findByUsername(username).isPresent()) {
            UserEntity user = userRepo.findByUsername(username).get();
            Set<Role> roles = user.getRoles();
            Set<String> res = new HashSet<>();
            for (Role r : roles) {
                res.add(r.name().substring(5));
            }
            return res;
        } else return null;
    }
}
