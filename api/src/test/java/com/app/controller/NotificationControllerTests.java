package com.app.controller;

import com.app.dto.NotificationDto;
import com.app.security.CustomUserDetailService;
import com.app.security.JWTGenerator;
import com.app.security.SecurityUtil;
import com.app.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
public class NotificationControllerTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private JWTGenerator jwtGenerator;
    @MockBean
    private AuthenticationManager authenticationManager;
    @MockBean
    private CustomUserDetailService customUserDetailService;
    @MockBean
    private SecurityUtil securityUtil;

    private NotificationDto notificationDto;
    private Page<NotificationDto> notificationDtoPage;

    @BeforeEach
    void setUp() {
        notificationDto = NotificationDto.builder().id(1L).message("test").isRead(false).createdAt(Instant.now()).userId(2L).build();
        notificationDtoPage = new PageImpl<>(List.of(notificationDto));
    }

    @Test
    void givenLoggedInUser_whenGetAllNotifications_thenReturnOk() throws Exception {
        given(securityUtil.getSessionUser()).willReturn("John");
        given(notificationService.getAllNotifications(anyString(), anyInt())).willReturn(notificationDtoPage);

        mockMvc.perform(get("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.notificationDtoList[0].message").value("test"))
                .andExpect(jsonPath("$.body.notificationDtoList[0].isRead").value(false))
                .andExpect(jsonPath("$.body.notificationDtoList[0].userId").value(2));
    }

    @Test
    void givenLoggedInUser_whenGetAllNotifications_thenReturnNotFound() throws Exception {
        given(securityUtil.getSessionUser()).willReturn("John");
        given(notificationService.getAllNotifications(anyString(), anyInt())).willReturn(Page.empty());

        mockMvc.perform(get("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Notification not found"));
    }

    @Test
    void givenNonLoggedInUser_whenGetAllNotifications_thenReturnUnauthorized() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(null);

        mockMvc.perform(get("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Not logged in"));
    }

    @Test
    void givenLoggedInUser_whenReadNotifications_thenReturnOk() throws Exception {
        given(securityUtil.getSessionUser()).willReturn("John");
        given(notificationService.readNotification(anyLong())).willReturn(true);

        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notificationDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Message marked as read"));
    }

    @Test
    void givenLoggedInUserAndInvalidNotification_whenReadNotifications_thenReturnNotFound() throws Exception {
        notificationDto.setMessage("bad_test");
        given(securityUtil.getSessionUser()).willReturn("John");
        given(notificationService.readNotification(anyLong())).willReturn(false);

        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notificationDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Notification not found"));
    }

    @Test
    void givenNonLoggedInUser_whenReadNotifications_thenReturnNotFound() throws Exception {
        given(securityUtil.getSessionUser()).willReturn(null);

        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notificationDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Not logged in"));
    }
}
