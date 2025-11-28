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

import java.util.HashSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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

    @BeforeEach
    void setUp() {
        userDtoJohn = UserDto.builder().username("John").password("johnsmith").email("john@johnsmith").roles(new HashSet<>(Set.of("DISPATCHER"))).build();
    }

    @Test
    void givenValidUser_whenRemoveRoles_thenReturnOk() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(userDtoJohn.getUsername());
        given(roleService.removeRoles(anyString(), anySet())).willReturn(true);

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
        given(roleService.addRoles(anyString(), anySet())).willReturn(true);

        mockMvc.perform(patch("/api/user/roles/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roles\": [\"DRIVER\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully added role [DRIVER]"));
    }

    @Test
    void givenValidUser_whenAddRoles_thenReturnBadRequest() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(userDtoJohn.getUsername());
        given(roleService.addRoles(anyString(), anySet())).willReturn(false);

        mockMvc.perform(patch("/api/user/roles/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roles\": [\"DRIVER\"]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Failed to add role"));
    }
}
