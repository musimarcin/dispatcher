package com.app.service;

import com.app.utils.NotificationToNotificationDto;
import com.app.dto.NotificationDto;
import com.app.model.Notification;
import com.app.model.Role;
import com.app.model.UserEntity;
import com.app.repository.NotificationRepo;
import com.app.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTests {

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private UserRepo userRepo;
    @Mock
    private NotificationRepo notificationRepo;
    @Mock
    private NotificationToNotificationDto notificationConverter;

    private UserEntity userJohn;
    private Notification notification;
    private NotificationDto notificationDto;
    private Page<Notification> notifications;

    @BeforeEach
    void setUp() {
        userJohn = UserEntity.builder().id(2L).username("John").password("smith").email("john@smith")
                .roles(new HashSet<>(Set.of(Role.ROLE_DISPATCHER))).build();
        notification = Notification.builder().id(1L).message("test").isRead(false).createdAt(Instant.now()).userId(2L).build();
        notificationDto = NotificationDto.builder().id(1L).message("test").isRead(false).createdAt(Instant.now()).userId(2L).build();
        notifications = new PageImpl<>(List.of(notification));
    }

    @Test
    void givenValidUser_whenGetAllNotifications_thenReturnPage() {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(userJohn));
        given(notificationRepo.findByUserId(anyLong(), any(Pageable.class))).willReturn(notifications);
        given(notificationConverter.convert(any(Notification.class))).willReturn(notificationDto);

        Page<NotificationDto> result = notificationService.getAllNotifications(userJohn.getUsername(), 0);

        assertEquals(notifications.getContent().get(0).getMessage(),
                result.getContent().get(0).getMessage());
        assertEquals(notifications.getContent().get(0).getUserId(),
                result.getContent().get(0).getUserId());
        assertEquals(notifications.getContent().get(0).getIsRead(),
                result.getContent().get(0).getIsRead());
    }

    @Test
    void givenInvalidUser_whenGetAllNotifications_thenReturnEmptyPage() {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.empty());

        Page<NotificationDto> result = notificationService.getAllNotifications(userJohn.getUsername(), 0);

        assertEquals(Page.empty(), result);
    }

    @Test
    void givenValidUserAndInvalidNotification_whenGetAllNotifications_thenReturnEmptyPage() {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(userJohn));
        given(notificationRepo.findByUserId(anyLong(), any(Pageable.class))).willReturn(Page.empty());

        Page<NotificationDto> result = notificationService.getAllNotifications(userJohn.getUsername(), 0);

        assertEquals(Page.empty(), result);
    }

    @Test
    void givenValidNotification_whenReadNotification_thenReturnTrue() {
        given(notificationRepo.findById(anyLong())).willReturn(Optional.of(notification));

        boolean result = notificationService.readNotification(1L);

        assertTrue(result);
    }

    @Test
    void givenInvalidNotification_whenReadNotification_thenReturnTrue() {
        given(notificationRepo.findById(anyLong())).willReturn(Optional.empty());

        boolean result = notificationService.readNotification(1L);

        assertFalse(result);
    }

}
