package com.app.service;

import com.app.dto.UserDto;
import com.app.model.Role;
import com.app.model.UserEntity;
import com.app.repository.UserRepo;
import com.app.utils.UserDtoToUser;
import com.app.utils.UserToUserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class RoleServiceTests {

    @Mock
    private UserRepo userRepo;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserDtoToUser userDtoConverter;
    @Mock
    private UserToUserDto userConverter;

    @InjectMocks
    private RoleService roleService;

    private UserDto userDtoJohn;
    private UserEntity userJohn;
    private UserDto userDtoAdam;

    @BeforeEach
    void setUp() {
        userDtoJohn = UserDto.builder().id(1L).username("John").password("smithsmith").email("john@smithsmith").roles(new HashSet<>(Set.of("DISPATCHER"))).build();
        userDtoAdam = UserDto.builder().id(2L).username("Adam").password("adamadam").email("adam@adamadam").roles(new HashSet<>(Set.of("DRIVER"))).build();
        userJohn = UserEntity.builder().id(1L).username("John").password("smithsmith").email("john@smithsmith").roles(new HashSet<>(Set.of(Role.DISPATCHER))).build();
    }

    @Test
    void givenValidUser_whenAddRoles_thenReturnTrue() {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(userJohn));

        boolean result = roleService.addRoles(userDtoJohn.getUsername(), userDtoAdam.getRoles());

        assertTrue(result);
        assertEquals(userJohn.getRoles(), Set.of(Role.DRIVER, Role.DISPATCHER));
    }

    @Test
    void givenValidUserAndInvalidRoles_whenAddRoles_thenReturnFalse() {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(userJohn));

        boolean result = roleService.addRoles(userDtoJohn.getUsername(), userDtoJohn.getRoles());

        assertFalse(result);
    }

    @Test
    void givenInvalidUser_whenAddRoles_ThenReturnFalse() {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.empty());

        boolean result = roleService.addRoles(userDtoJohn.getUsername(), userDtoAdam.getRoles());

        assertFalse(result);
    }

    @Test
    void givenValidUser_whenRemoveRoles_thenReturnTrue() {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(userJohn));

        boolean result = roleService.removeRoles(userDtoJohn.getUsername(), userDtoJohn.getRoles());

        assertTrue(result);
        assertEquals(Set.of(), userJohn.getRoles());
    }

    @Test
    void givenValidUser_whenRemoveRoles_thenReturnFalse() {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(userJohn));

        boolean result = roleService.removeRoles(userDtoJohn.getUsername(), userDtoAdam.getRoles());

        assertFalse(result);
    }

    @Test
    void givenInvalidUser_whenRemoveRoles_thenReturnFalse() {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.empty());

        boolean result = roleService.removeRoles(userDtoJohn.getUsername(), userDtoAdam.getRoles());

        assertFalse(result);
    }

    @Test
    void givenValidUser_whenGetUserRoles_thenReturnSet() {
        given(userRepo.findById(anyLong())).willReturn(Optional.of(userJohn));

        Set<String> result = roleService.getUserRoles(userDtoJohn.getId());

        assertEquals(Set.of("DISPATCHER"), result);
    }

    @Test
    void givenInvalidUser_whenGetUserRoles_thenReturnEmpty() {
        Set<String> result = roleService.getUserRoles(userDtoJohn.getId());

        assertEquals(Set.of(), result);
    }
}
