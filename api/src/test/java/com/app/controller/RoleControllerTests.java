package com.app.controller;

import com.app.dto.UserDto;
import com.app.security.CustomUserDetailService;
import com.app.security.JWTGenerator;
import com.app.security.SecurityUtil;
import com.app.service.RoleService;
import com.app.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RoleController.class)
@AutoConfigureMockMvc(addFilters = false)
public class RoleControllerTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;
    @MockBean
    private RoleService roleService;
    @MockBean
    private JWTGenerator jwtGenerator;
    @MockBean
    private CustomUserDetailService customUserDetailService;
    @MockBean
    private SecurityUtil securityUtil;

    private UserDto userDtoJohn;
    private UserDto userDtoAdmin;

    @BeforeEach
    void setUp() {
        userDtoJohn = UserDto.builder().id(1L).username("John").password("johnsmith").email("john@johnsmith").roles(Set.of("DISPATCHER")).build();
        userDtoAdmin = UserDto.builder().id(2L).username("Admin").password("adminadmin").email("admin@adminadmin").roles(Set.of("ADMIN")).build();
    }

    @Test
    void givenInvalidUser_whenAddRoles_thenReturnOk() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(null);

        mockMvc.perform(patch("/api/roles/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roles\": [\"DRIVER\"], \"username\": \"John\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("You are not logged in to change user details"));
    }

    @Test
    void givenValidUser_whenAddRoles_thenReturnOk() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(userDtoAdmin.getUsername());
        given(roleService.addRoles(anyString(), anySet())).willReturn(true);

        mockMvc.perform(patch("/api/roles/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roles\": [\"DRIVER\"], \"username\": \"John\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully added role [DRIVER]"));
    }

    @Test
    void givenValidUser_whenAddRoles_thenReturnBadRequest() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(userDtoAdmin.getUsername());
        given(roleService.addRoles(anyString(), anySet())).willReturn(false);

        mockMvc.perform(patch("/api/roles/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roles\": [\"DRIVER\"], \"username\": \"John\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Failed to add role"));
    }

    @Test
    void givenInvalidUser_whenAddRoles_thenReturnBadRequest() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(userDtoAdmin.getUsername());

        mockMvc.perform(patch("/api/roles/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"John\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Failed to get roles"));
    }

    @Test
    void givenLoggedInNotAdmin_whenAddRole_thenReturnUnauthorized() throws Exception {
        mockMvc.perform(patch("/api/roles/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roles\": [\"DISPATCHER\"], \"username\": \"Test\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void givenValidUser_whenRemoveRoles_thenReturnOk() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(userDtoAdmin.getUsername());
        given(roleService.removeRoles(anyString(), anySet())).willReturn(true);

        mockMvc.perform(patch("/api/roles/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roles\": [\"DISPATCHER\"], \"username\": \"John\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully removed role [DISPATCHER]"));
    }

    @Test
    void givenInvalidUser_whenRemoveRoles_thenReturnOk() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(null);

        mockMvc.perform(patch("/api/roles/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roles\": [\"DISPATCHER\"], \"username\": \"John\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("You are not logged in to change user details"));
    }

    @Test
    void givenInvalidRoles_whenRemoveRoles_thenReturnBadRequest() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(userDtoAdmin.getUsername());

        mockMvc.perform(patch("/api/roles/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"John\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Failed to get roles"));
    }

    @Test
    void givenInvalidUser_whenRemoveRoles_thenReturnBadRequest() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(userDtoAdmin.getUsername());
        given(roleService.removeRoles(anyString(), anySet())).willReturn(false);

        mockMvc.perform(patch("/api/roles/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roles\": [\"DISPATCHER\"], \"username\": \"Test\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Failed to remove role"));
    }

    @Test
    void givenLoggedInNotAdmin_whenRemoveRole_thenReturnUnauthorized() throws Exception {
        mockMvc.perform(patch("/api/roles/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roles\": [\"DISPATCHER\"], \"username\": \"Test\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void givenLoggedInNotAdmin_whenGetUserRoles_thenReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/roles/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void givenNotLoggedIn_whenGetUserRoles_thenReturnUnauthorized() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(null);

        mockMvc.perform(get("/api/roles/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Not logged in"));
    }

    @Test
    void givenLoggedInAdmin_whenGetUserRoles_thenReturnOk() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(userDtoAdmin.getUsername());
        given(roleService.getUserRoles(anyLong())).willReturn(userDtoJohn.getRoles());

        mockMvc.perform(get("/api/roles/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body").value("DISPATCHER"));
    }

    @Test
    void givenLoggedInAdminAndUserWithoutRoles_whenGetUserRoles_thenReturnOk() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(userDtoAdmin.getUsername());
        given(roleService.getUserRoles(anyLong())).willReturn(Set.of());

        mockMvc.perform(get("/api/roles/3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("No roles found"));
    }


}
