package com.app.controller;

import com.app.dto.UserDto;
import com.app.security.CustomUserDetailService;
import com.app.security.JWTGenerator;
import com.app.security.SecurityUtil;
import com.app.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

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


    private UserDto userDtoJohn;
    private UserDto userDtoAdam;

    @BeforeEach
    void setUp() {
        userDtoJohn = UserDto.builder().username("John").password("smith").email("john@smith").roles(new HashSet<>(Set.of("DISPATCHER"))).build();
        userDtoAdam = UserDto.builder().username("Adam").password("adam").email("adam@adam").roles(new HashSet<>(Set.of("DRIVER"))).build();
    }

    @Test
    void givenNewUser_whenCreate_thenReturnCreated() throws Exception {
        // given
        given(userService.createUser(any(UserDto.class))).willReturn(userDtoJohn);

        // when
        ResultActions response = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDtoJohn)));

        // then
        response.andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.body.username").value("John"))
                .andExpect(jsonPath("$.body.email").value("john@smith"));
    }

    @Test
    void givenExistingUser_whenCreate_thenReturnAlreadyExisting() throws Exception {
        given(userService.createUser(any(UserDto.class))).willReturn(null);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDtoJohn)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username or email taken"));
    }

    @Test
    void givenValidUser_whenLogin_thenReturnOk() throws Exception {
        given(authenticationManager.authenticate(any())).willReturn(mock(Authentication.class));
        given(jwtGenerator.generateToken(any())).willReturn("mock-token");

        Cookie cookie = new Cookie("token", "mock-token");
        given(securityUtil.getNewCookie(anyString())).willReturn(cookie);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"John\",\"password\":\"smith\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged in successfully"))
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
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    void whenLogout_thenReturnOk() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(userDtoJohn.getUsername());
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully logged out"));
    }

    @Test
    void whenLogout_thenReturnUnauthorized() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(null);

        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Not logged in"));
    }
}
