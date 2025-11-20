package com.app.service;

import com.app.converters.UserDtoToUser;
import com.app.converters.UserToUserDto;
import com.app.dto.UserDto;
import com.app.model.Role;
import com.app.model.UserEntity;
import com.app.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests {

    @Mock
    private UserRepo userRepo;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserDtoToUser userDtoConverter;
    @Mock
    private UserToUserDto userConverter;

    @InjectMocks
    private UserService userService;

    private UserDto userDtoJohn;
    private UserEntity userJohn;
    private UserDto userDtoAdam;
    private UserEntity userAdam;

    @BeforeEach
    void setUp() {
        userDtoJohn = UserDto.builder().username("John").password("smith").email("john@smith").roles(new HashSet<>(Set.of("ROLE_DISPATCHER"))).build();
        userDtoAdam = UserDto.builder().username("Adam").password("adam").email("adam@adam").roles(new HashSet<>(Set.of("ROLE_DRIVER"))).build();
        userAdam = UserEntity.builder().username("Adam").password("adam").email("adam@adam").roles(new HashSet<>(Set.of(Role.ROLE_DRIVER))).build();
        userJohn = UserEntity.builder().username("John").password("smith").email("john@smith").roles(new HashSet<>(Set.of(Role.ROLE_DISPATCHER))).build();
    }

    @Test
    void givenValidUser_whenCreateUser_thenReturnUserDto() {
        given(userDtoConverter.convert(any(UserDto.class))).willReturn(userJohn);
        given(userConverter.convert(any(UserEntity.class))).willReturn(userDtoJohn);
        given(passwordEncoder.encode(anyString())).willReturn("encoded");

        UserDto result = userService.createUser(userDtoJohn);

        assertEquals(userDtoJohn.getUsername(), result.getUsername());
        assertEquals(userDtoJohn.getEmail(), result.getEmail());
        assertEquals(userDtoJohn.getRoles(), result.getRoles());
    }

    @Test
    void givenInvalidUser_whenCreateUser_thenReturnNull() {
        given(userRepo.existsByUsername(anyString())).willReturn(true);
        given(userRepo.existsByEmail(anyString())).willReturn(true);

        UserDto result = userService.createUser(userDtoJohn);

        assertNull(result);
    }

    @Test
    void givenValidUser_whenDeleteUser_thenReturnTrue() {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(userJohn));

        boolean result = userService.deleteUser(userDtoJohn.getUsername());

        assertTrue(result);
    }

    @Test
    void givenInvalidUser_whenDeleteUser_thenReturnFalse() {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.empty());

        boolean result = userService.deleteUser(userDtoAdam.getUsername());

        assertFalse(result);
    }

    @Test
    void givenValidUser_whenChangeUsername_thenReturnTrue() {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(userJohn));
        given(userRepo.findByUsername(userDtoAdam.getUsername())).willReturn(Optional.empty());

        boolean result = userService.changeUsername(userDtoJohn.getUsername(), userDtoAdam.getUsername());

        assertTrue(result);
        assertEquals(userDtoAdam.getUsername(), userJohn.getUsername());
    }

    @Test
    void givenInvalidUser_whenChangeUsername_thenReturnFalse() {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.empty());

        boolean result = userService.changeUsername(userDtoAdam.getUsername(), userDtoAdam.getUsername());

        assertFalse(result);
    }

    @Test
    void givenValidUser_whenChangePassword_thenReturnTrue() {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(userJohn));
        given(passwordEncoder.encode(anyString())).willReturn("encoded");

        boolean result = userService.changePassword(userDtoJohn.getUsername(), userDtoJohn.getPassword());

        assertTrue(result);
        assertEquals("encoded", userJohn.getPassword());
    }

    @Test
    void givenInvalidUser_whenChangePassword_thenReturnFalse() {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.empty());

        boolean result = userService.changePassword(userDtoJohn.getPassword(), userDtoJohn.getPassword());

        assertFalse(result);
    }

    @Test
    void givenValidUser_whenChangeEmail_thenReturnTrue() {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(userJohn));

        boolean result = userService.changeEmail(userDtoJohn.getUsername(), userDtoAdam.getEmail());

        assertTrue(result);
        assertEquals(userDtoAdam.getEmail(), userJohn.getEmail());
    }

    @Test
    void givenInvalidUser_whenChangeEmail_thenReturnFalse() {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.empty());

        boolean result = userService.changeEmail(userDtoJohn.getUsername(), userDtoAdam.getEmail());

        assertFalse(result);
    }

    @Test
    void givenValidUser_whenAddRoles_thenReturnTrue() {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(userJohn));

        boolean result = userService.addRoles(userDtoJohn.getUsername(), userDtoAdam.getRoles());

        assertTrue(result);
        assertEquals(userJohn.getRoles(), Set.of(Role.ROLE_DRIVER, Role.ROLE_DISPATCHER));
    }

    @Test
    void givenValidUserAndInvalidRoles_whenAddRoles_thenReturnFalse() {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(userJohn));

        boolean result = userService.addRoles(userDtoJohn.getUsername(), userDtoJohn.getRoles());

        assertFalse(result);
    }

    @Test
    void givenInvalidUser_whenAddRoles_ThenReturnFalse() {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.empty());

        boolean result = userService.addRoles(userDtoJohn.getUsername(), userDtoAdam.getRoles());

        assertFalse(result);
    }

    @Test
    void givenValidUser_whenRemoveRoles_thenReturnTrue() {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(userJohn));

        boolean result = userService.removeRoles(userDtoJohn.getUsername(), userDtoJohn.getRoles());

        assertTrue(result);
        assertEquals(Set.of(), userJohn.getRoles());
    }

    @Test
    void givenValidUser_whenRemoveRoles_thenReturnFalse() {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(userJohn));

        boolean result = userService.removeRoles(userDtoJohn.getUsername(), userDtoAdam.getRoles());

        assertFalse(result);
    }

    @Test
    void givenInvalidUser_whenRemoveRoles_thenReturnFalse() {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.empty());

        boolean result = userService.removeRoles(userDtoJohn.getUsername(), userDtoAdam.getRoles());

        assertFalse(result);
    }

    @Test
    void givenValidUser_whenGetUserRoles_thenReturnSet() {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(userJohn));

        Set<String> result = userService.getUserRoles(userDtoJohn.getUsername());

        assertEquals(Set.of("DISPATCHER"), result);
    }

    @Test
    void givenInvalidUser_whenGetUserRoles_thenReturnNull() {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.empty());

        Set<String> result = userService.getUserRoles(userDtoJohn.getUsername());

        assertEquals(Set.of(), result);
    }
}
