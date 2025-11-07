package com.app.controller;

import com.app.dto.UserDto;
import com.app.model.Role;
import com.app.model.UserEntity;
import com.app.security.CustomUserDetailService;
import com.app.security.JWTGenerator;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import javax.security.sasl.AuthenticationException;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
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
                .andExpect(content().string(containsString("User Registered:")))
                .andExpect(content().string(containsString("username=John")))
                .andExpect(content().string(containsString("email=john@smith")));
    }

    @Test
    public void givenExistingUser_whenCreate_thenReturnAlreadyExisting() throws Exception {
        given(userService.createUser(any(UserDto.class))).willReturn(null);

        ResultActions response = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDtoJohn)));

        response.andExpect(status().isBadRequest())
                .andExpect(content().string("Username or email taken"));
    }

    @Test
    public void givenValidUser_whenLogin_ThenReturnToken() throws Exception {
        given(authenticationManager.authenticate(any())).willReturn(null);
        given(jwtGenerator.generateToken(any())).willReturn("mock-token");

        ResultActions response = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"John\",\"password\":\"smith\"}"));

        response.andExpect(status().isOk())
                .andExpect(content().string("Logged in successfully"))
                .andExpect(header().string("Set-Cookie", containsString("token=mock-token")));

    }

    @Test
    public void givenInvalidUser_whenLogin_ThenReturnUnauthorized() throws Exception {
        given(authenticationManager.authenticate(any())).willThrow(new BadCredentialsException("Bad credentials"));

        ResultActions response = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"invalid\",\"password\":\"invalid\"}"));

        response.andExpect(status().isUnauthorized())
                .andExpect(content().string("Credentials incorrect"));
    }

    @Test
    void whenLogout_thenReturnOk() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer mocked-token"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "John", password = "smith")
    void givenExistingUser_whenDelete_thenReturnOk() throws Exception {
        given(userService.deleteUser("John")).willReturn(true);

        mockMvc.perform(delete("/api/auth"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "John", password = "smith")
    void givenNonExistingUser_whenDelete_thenReturnNotFound() throws Exception {
        given(userService.deleteUser("Abc")).willReturn(false);

        mockMvc.perform(delete("/api/auth"))
                .andExpect(status().isNotFound());
    }

    @Test
    void givenNonLoggedInUser_whenDelete_thenReturnUnauthorized() throws Exception {
        SecurityContextHolder.clearContext();

        ResultActions response = mockMvc.perform(delete("/api/auth/delete"));

        response.andExpect(status().isUnauthorized())
                .andExpect(content().string("You are not logged in to delete user"));
    }
}
