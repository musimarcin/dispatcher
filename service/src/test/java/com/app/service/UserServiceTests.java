package com.app.service;

import com.app.dto.UserInfo;
import com.app.utils.UserDtoToUser;
import com.app.utils.UserToUserDto;
import com.app.dto.UserDto;
import com.app.model.Role;
import com.app.model.UserEntity;
import com.app.repository.UserRepo;
import com.app.utils.UserToUserInfo;
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
    @Mock
    private UserToUserInfo userInfoConverter;

    @InjectMocks
    private UserService userService;

    private UserDto userDtoJohn;
    private UserEntity userJohn;
    private UserDto userDtoAdam;
    private UserEntity userAdam;
    private UserInfo userInfoJohn;
    private UserInfo userInfoAdam;
    private UserEntity userAdmin;

    @BeforeEach
    void setUp() {
        userDtoJohn = UserDto.builder().id(1L).username("John").password("smith").email("john@smith").roles(new HashSet<>(Set.of("DISPATCHER"))).build();
        userDtoAdam = UserDto.builder().id(2L).username("Adam").password("adam").email("adam@adam").roles(new HashSet<>(Set.of("DRIVER"))).build();
        userAdam = UserEntity.builder().id(2L).username("Adam").password("adam").email("adam@adam").roles(new HashSet<>(Set.of(Role.DRIVER))).build();
        userJohn = UserEntity.builder().id(1L).username("John").password("smith").email("john@smith").roles(new HashSet<>(Set.of(Role.DISPATCHER))).build();
        userInfoJohn = new UserInfo(1L, "John", Set.of(Role.DISPATCHER));
        userInfoAdam = new UserInfo(1L, "Adam", Set.of(Role.DRIVER));
        userAdmin = UserEntity.builder().id(3L).username("Admin").password("adminadmin").email("admin@adminadmin").roles((new HashSet<>(Set.of(Role.ADMIN)))).build();
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
    void givenInvalidUserName_whenCreateUser_thenReturnNull() {
        given(userRepo.existsByUsername(anyString())).willReturn(true);

        UserDto result = userService.createUser(userDtoJohn);

        assertNull(result);
    }

    @Test
    void givenInvalidUserEmail_whenCreateUser_thenReturnNull() {
        given(userRepo.existsByUsername(anyString())).willReturn(false);
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
    void givenValidUser_whenGetAllDrivers_thenReturnDriversSet() {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(userJohn));
        given(userRepo.getAllDrivers(any(Role.class))).willReturn(Set.of(userAdam));
        given(userInfoConverter.convert(userAdam)).willReturn(userInfoAdam);

        Set<UserInfo> result = userService.getAllDrivers(userJohn.getUsername());

        assertEquals(Set.of(userInfoAdam), result);
    }

    @Test
    void givenInvalidUser_whenGetAllDrivers_thenReturnEmptySet() {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.empty());

        Set<UserInfo> result = userService.getAllDrivers(userJohn.getUsername());

        assertEquals(Set.of(), result);
    }

    @Test
    void givenValidUser_whenGetAllUsers_thenReturnUsersList() {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(userAdmin));
        given(userRepo.findAll()).willReturn(List.of(userJohn));
        given(userInfoConverter.convert(userJohn)).willReturn(userInfoJohn);

        List<UserInfo> result = userService.getAllUsers(userAdmin.getUsername());

        assertEquals(List.of(userInfoJohn), result);
    }

    @Test
    void givenInvalidUser_whenGetAllUsers_thenReturnEmptyList() {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.empty());

        List<UserInfo> result = userService.getAllUsers(userJohn.getUsername());

        assertEquals(List.of(), result);
    }

    @Test
    void givenUnauthorizedUser_whenGetAllUsers_thenReturnEmptyList() {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(userJohn));

        List<UserInfo> result = userService.getAllUsers(userJohn.getUsername());

        assertEquals(List.of(), result);
    }
}
