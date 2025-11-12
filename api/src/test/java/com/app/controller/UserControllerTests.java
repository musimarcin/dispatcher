package com.app.controller;

import com.app.dto.UserDto;
import com.app.model.Role;
import com.app.model.UserEntity;
import com.app.security.CustomUserDetailService;
import com.app.security.JWTGenerator;
import com.app.security.SecurityUtil;
import com.app.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import jakarta.servlet.http.Cookie;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;
    @MockBean
    private JWTGenerator jwtGenerator;
    @MockBean
    private AuthenticationManager authenticationManager;
    @MockBean
    private CustomUserDetailService customUserDetailService;
    @MockBean
    private SecurityUtil securityUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private UserEntity userJohn;
    private UserEntity userAdam;
    private UserEntity userAsdf;
    private UserDto userDtoJohn;
    private UserDto userDtoAdam;
    private UserDto userDtoAsdf;

    @BeforeEach
    void setUp() {
        userJohn = UserEntity.builder().id(1L).username("John").password("smith").email("john@smith").roles(new HashSet<>(Set.of(Role.ROLE_DISPATCHER))).build();
        userAdam = UserEntity.builder().id(2L).username("Adam").password("adam").email("adam@adam").roles(new HashSet<>(Set.of(Role.ROLE_DRIVER))).build();
        userAsdf = UserEntity.builder().id(3L).username("Asdf").password("qwerty").email("asdf@qwerty").roles(new HashSet<>(Set.of(Role.ROLE_DISPATCHER, Role.ROLE_DRIVER))).build();

        userDtoJohn = UserDto.builder().username("John").password("smith").email("john@smith").roles(new HashSet<>(Set.of("ROLE_DISPATCHER"))).build();
        userDtoAdam = UserDto.builder().username("Adam").password("adam").email("adam@adam").roles(new HashSet<>(Set.of("ROLE_DRIVER"))).build();
        userDtoAsdf = UserDto.builder().username("Asdf").password("qwerty").email("asdf@qwerty").roles(new HashSet<>(Set.of("ROLE_DISPATCHER", "ROLE_DRIVER"))).build();
    }



    @Test
    public void givenNewUser_whenCreate_thenReturnCreated() throws Exception {
        // given
        given(userService.createUser(any(UserDto.class))).willReturn(userDtoJohn);

        // when
        ResultActions response = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDtoJohn)));

        // then
        response.andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.user.username").value("John"))
                .andExpect(jsonPath("$.user.email").value("john@smith"));
    }

    @Test
    public void givenExistingUser_whenCreate_thenReturnAlreadyExisting() throws Exception {
        given(userService.createUser(any(UserDto.class))).willReturn(null);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDtoJohn)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username or email taken"));
    }

    @Test
    void givenValidUser_whenLogin_thenReturnOk() throws Exception {
        Authentication authentication = mock(Authentication.class);
        given(authenticationManager.authenticate(any())).willReturn(authentication);
        given(jwtGenerator.generateToken(any())).willReturn("mock-token");

        Cookie cookie = new Cookie("token", "mock-token");
        given(securityUtil.getNewCookie(anyString())).willReturn(cookie);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"John\",\"password\":\"smith\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Logged in successfully"))
                .andExpect(cookie().value("token", "mock-token"));
    }

    @Test
    void givenInvalidUser_whenLogin_thenReturnUnauthorized() throws Exception {
        given(authenticationManager.authenticate(any(Authentication.class)))
                .willThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"John\",\"password\":\"smith\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid credentials"));
    }

    @Test
    void whenLogout_thenReturnOk() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer mocked-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("Logged out successfully"));
    }

    @Test
    void givenLoggedInUser_whenDelete_thenReturnOk() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(userDtoJohn.getUsername());
        given(userService.deleteUser(userDtoJohn.getUsername())).willReturn(true);

        mockMvc.perform(delete("/api/auth"))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted successfully"));
    }

    @Test
    void givenInvalidUser_whenDelete_thenReturnNotFound() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(userDtoJohn.getUsername());
        given(userService.deleteUser(userDtoAdam.getUsername())).willReturn(false);

        mockMvc.perform(delete("/api/auth"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found"));
    }

    @Test
    void givenNonLoggedInUser_whenDelete_thenReturnUnauthorized() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(null);

        mockMvc.perform(delete("/api/auth"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("You must be logged in to delete user"));
    }

    @Test
    void givenValidUserAndUsername_whenChange_thenReturnOk() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(userDtoJohn.getUsername(), userDtoAdam.getUsername());
        given(userService.changeUsername(userDtoJohn.getUsername(), userDtoAdam.getUsername())).willReturn(true);

        UserDetails userDetails = User
                .withUsername(userDtoAdam.getUsername())
                .password(userDtoJohn.getPassword())
                .authorities(String.valueOf(userDtoJohn.getRoles()))
                .build();
        given(customUserDetailService.loadUserByUsername(userDtoAdam.getUsername())).willReturn(userDetails);
        given(jwtGenerator.generateToken(any())).willReturn("mock-token");

        Cookie cookie = new Cookie("token", "mock-token");
        given(securityUtil.getNewCookie(anyString())).willReturn(cookie);

        mockMvc.perform(put("/api/auth/change")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"Adam\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully changed username"));
    }

    @Test
    void givenValidUserAndPassword_whenChange_thenReturnOk() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(userDtoJohn.getUsername());
        given(userService.changePassword(userDtoJohn.getUsername(), userDtoAdam.getPassword())).willReturn(true);

        mockMvc.perform(put("/api/auth/change")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"adam\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully changed password"));
    }

    @Test
    void givenValidUserAndEmail_whenChange_thenReturnOk() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(userDtoJohn.getUsername());
        given(userService.changeEmail(userDtoJohn.getUsername(), userDtoAdam.getEmail())).willReturn(true);

        mockMvc.perform(put("/api/auth/change")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"adam@adam\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully changed email"));
    }

    @Test
    void givenValidUserAndUsernameAndPasswordAndEmail_whenChange_thenReturnOk() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(userDtoJohn.getUsername(), userDtoAdam.getUsername());
        given(userService.changeUsername(userDtoJohn.getUsername(), userDtoAdam.getUsername())).willReturn(true);
        //changed username so using userdtoadam now
        given(userService.changePassword(userDtoAdam.getUsername(), userDtoAdam.getPassword())).willReturn(true);
        given(userService.changeEmail(userDtoAdam.getUsername(), userDtoAdam.getEmail())).willReturn(true);

        UserDetails userDetails = User
                .withUsername(userDtoAdam.getUsername())
                .password(userDtoAdam.getPassword())
                .authorities(String.valueOf(userDtoJohn.getRoles()))
                .build();
        given(customUserDetailService.loadUserByUsername(userDtoAdam.getUsername())).willReturn(userDetails);
        given(jwtGenerator.generateToken(any())).willReturn("mock-token");

        Cookie cookie = new Cookie("token", "mock-token");
        given(securityUtil.getNewCookie(anyString())).willReturn(cookie);

        mockMvc.perform(put("/api/auth/change")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"Adam\", \"password\":\"adam\", \"email\":\"adam@adam\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully changed username password email"));
    }

    @Test
    void givenValidUserAndInvalidUsername_whenChange_thenReturnBadRequest() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(userDtoJohn.getUsername(), userDtoAdam.getUsername());
        given(userService.changeUsername(userDtoJohn.getUsername(), userDtoAdam.getUsername())).willReturn(false);

        mockMvc.perform(put("/api/auth/change")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"Adam\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Failed to change username or username already taken"));
    }

    @Test
    void givenValidUserAndInvalidPassword_whenChange_thenReturnBadRequest() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(userDtoJohn.getUsername());
        given(userService.changePassword(userDtoJohn.getUsername(), userDtoAdam.getPassword())).willReturn(false);

        mockMvc.perform(put("/api/auth/change")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"adam\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Failed to change password"));
    }

    @Test
    void givenValidUserAndInvalidEmail_whenChange_thenReturnBadRequest() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(userDtoJohn.getUsername());
        given(userService.changeEmail(userDtoJohn.getUsername(), userDtoAdam.getEmail())).willReturn(false);

        mockMvc.perform(put("/api/auth/change")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"adam@adam\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Failed to change email"));
    }

    @Test
    void givenInvalidUserAndValidUser_whenChange_thenReturnUnauthorized() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(null);

        mockMvc.perform(put("/api/auth/change")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"adam\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Not logged in"));
    }

    @Test
    void givenValidUser_whenRemoveRoles_thenReturnOk() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(userDtoJohn.getUsername());
        given(userService.removeRoles(userDtoJohn.getUsername(), Set.of("ROLE_DISPATCHER"))).willReturn(true);

        mockMvc.perform(patch("/api/auth/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roles\": [\"ROLE_DISPATCHER\"]}")
                        .header("action", "remove"))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully edited role [ROLE_DISPATCHER]"));
    }

    @Test
    void givenInvalidUser_whenRemoveRoles_thenReturnOk() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(null);

        mockMvc.perform(patch("/api/auth/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roles\": [\"ROLE_DISPATCHER\"]}")
                        .header("action", "remove"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("You are not logged in to change user details"));
    }

    @Test
    void givenInvalidUser_whenAddRoles_thenReturnOk() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(null);

        mockMvc.perform(patch("/api/auth/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roles\": [\"ROLE_DRIVER\"]}")
                        .header("action", "add"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("You are not logged in to change user details"));
    }

    @Test
    void givenValidUser_whenAddRoles_thenReturnOk() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(userDtoJohn.getUsername());
        given(userService.addRoles(userDtoJohn.getUsername(), Set.of("ROLE_DRIVER"))).willReturn(true);

        mockMvc.perform(patch("/api/auth/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roles\": [\"ROLE_DRIVER\"]}")
                        .header("action", "add"))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully edited role [ROLE_DRIVER]"));
    }

    @Test
    void givenValidUser_whenAddRoles_thenReturnBadRequest() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(userDtoJohn.getUsername());
        given(userService.addRoles(userDtoJohn.getUsername(), Set.of("ROLE_DRIVER"))).willReturn(false);

        mockMvc.perform(patch("/api/auth/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roles\": [\"ROLE_DRIVER\"]}")
                        .header("action", "add"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Failed to update roles"));
    }



}
