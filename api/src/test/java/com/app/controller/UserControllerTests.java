package com.app.controller;

import com.app.dto.UserDto;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import jakarta.servlet.http.Cookie;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;
    @MockBean
    private JWTGenerator jwtGenerator;
    @MockBean
    private CustomUserDetailService customUserDetailService;
    @MockBean
    private SecurityUtil securityUtil;

    private UserDto userDtoJohn;
    private UserDto userDtoAdam;

    @BeforeEach
    void setUp() {
        userDtoJohn = UserDto.builder().username("John").password("johnsmith").email("john@johnsmith").roles(new HashSet<>(Set.of("DISPATCHER"))).build();
        userDtoAdam = UserDto.builder().username("Adam").password("adamadam").email("adam@adamadam").roles(new HashSet<>(Set.of("DRIVER"))).build();
    }


    @Test
    void givenLoggedInUser_whenDelete_thenReturnOk() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(userDtoJohn.getUsername());
        given(userService.deleteUser(anyString())).willReturn(true);

        mockMvc.perform(delete("/api/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deleted successfully"));
    }

    @Test
    void givenInvalidUser_whenDelete_thenReturnNotFound() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(userDtoJohn.getUsername());
        given(userService.deleteUser(anyString())).willReturn(false);

        mockMvc.perform(delete("/api/user"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void givenNonLoggedInUser_whenDelete_thenReturnUnauthorized() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(null);

        mockMvc.perform(delete("/api/user"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("You must be logged in to delete user"));
    }

    @Test
    void givenValidUserAndUsername_whenChangeUsername_thenReturnOk() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(userDtoJohn.getUsername(), userDtoAdam.getUsername());
        given(userService.changeUsername(anyString(), anyString())).willReturn(true);

        UserDetails userDetails = User
                .withUsername(userDtoAdam.getUsername())
                .password(userDtoJohn.getPassword())
                .authorities(String.valueOf(userDtoJohn.getRoles()))
                .build();
        given(customUserDetailService.loadUserByUsername(anyString())).willReturn(userDetails);
        given(jwtGenerator.generateToken(any(Authentication.class))).willReturn("mock-token");

        Cookie cookie = new Cookie("token", "mock-token");
        given(securityUtil.getNewCookie(anyString())).willReturn(cookie);

        mockMvc.perform(put("/api/user/username")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newUsername\":\"Adam\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully changed username"));
    }

    @Test
    void givenValidUserAndTakenUsername_whenChangeUsername_thenReturnBadRequest() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(userDtoJohn.getUsername(), userDtoAdam.getUsername());
        given(userService.changeUsername(anyString(), anyString())).willReturn(false);

        mockMvc.perform(put("/api/user/username")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newUsername\":\"Adam\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username already taken"));
    }

    @Test
    void givenInvalidUserAndValidUsername_whenChangeUsername_thenReturnUnauthorized() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(null);

        mockMvc.perform(put("/api/user/username")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newUsername\":\"Adam\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Not logged in"));
    }

    @Test
    void givenValidUserAndShortUsername_whenChangeUsername_thenReturnBadRequest() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(userDtoJohn.getUsername());

        mockMvc.perform(put("/api/user/username")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"newUsername\":\"Ad\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username must be between 3 and 20 characters"));
    }


    @Test
    void givenValidUserAndPassword_whenChangePassword_thenReturnOk() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(userDtoJohn.getUsername());
        given(userService.changePassword(anyString(), anyString())).willReturn(true);

        mockMvc.perform(put("/api/user/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPassword\":\"adamadam\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully changed password"));
    }

    @Test
    void givenValidUserAndInvalidPassword_whenChangePassword_thenReturnBadRequest() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(userDtoJohn.getUsername());
        given(userService.changePassword(anyString(), anyString())).willReturn(false);

        mockMvc.perform(put("/api/user/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPassword\":\"adamadam\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Failed to change password"));
    }

    @Test
    void givenValidUserAndShortPassword_whenChangePassword_thenReturnBadRequest() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(userDtoJohn.getUsername());
        given(userService.changePassword(anyString(), anyString())).willReturn(false);

        mockMvc.perform(put("/api/user/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPassword\":\"adam\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Password must be at least 8 characters"));
    }

    @Test
    void givenValidUserAndEmail_whenChangeEmail_thenReturnOk() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(userDtoJohn.getUsername());
        given(userService.changeEmail(anyString(), anyString())).willReturn(true);

        mockMvc.perform(put("/api/user/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newEmail\":\"adam@adamadam\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully changed email"));
    }

    @Test
    void givenValidUserAndTakenEmail_whenChangeEmail_thenReturnBadRequest() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(userDtoJohn.getUsername());
        given(userService.changeEmail(anyString(), anyString())).willReturn(false);

        mockMvc.perform(put("/api/user/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newEmail\":\"adam@adamadam\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Failed to change email"));
    }

    @Test
    void givenValidUserAndInvalidEmail_whenChangeEmail_thenReturnBadRequest() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(userDtoJohn.getUsername());
        given(userService.changeEmail(anyString(), anyString())).willReturn(false);

        mockMvc.perform(put("/api/user/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newEmail\":\"adam\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email should be valid"));
    }

    @Test
    void givenValidUserAndBlankEmail_whenChangeEmail_thenReturnBadRequest() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(userDtoJohn.getUsername());
        given(userService.changeEmail(anyString(), anyString())).willReturn(false);

        mockMvc.perform(put("/api/user/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newEmail\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email cannot be empty"));
    }

    @Test
    void givenValidUser_whenRemoveRoles_thenReturnOk() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(userDtoJohn.getUsername());
        given(userService.removeRoles(anyString(), anySet())).willReturn(true);

        mockMvc.perform(patch("/api/user/roles/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roles\": [\"DISPATCHER\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully removed role [DISPATCHER]"));
    }

    @Test
    void givenInvalidUser_whenRemoveRoles_thenReturnOk() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(null);

        mockMvc.perform(patch("/api/user/roles/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roles\": [\"DISPATCHER\"]}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("You are not logged in to change user details"));
    }

    @Test
    void givenInvalidUser_whenAddRoles_thenReturnOk() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(null);

        mockMvc.perform(patch("/api/user/roles/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roles\": [\"ROLE_DRIVER\"]}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("You are not logged in to change user details"));
    }

    @Test
    void givenValidUser_whenAddRoles_thenReturnOk() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(userDtoJohn.getUsername());
        given(userService.addRoles(anyString(), anySet())).willReturn(true);

        mockMvc.perform(patch("/api/user/roles/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roles\": [\"DRIVER\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully added role [DRIVER]"));
    }

    @Test
    void givenValidUser_whenAddRoles_thenReturnBadRequest() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(userDtoJohn.getUsername());
        given(userService.addRoles(anyString(), anySet())).willReturn(false);

        mockMvc.perform(patch("/api/user/roles/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roles\": [\"DRIVER\"]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Failed to add role"));
    }

}
