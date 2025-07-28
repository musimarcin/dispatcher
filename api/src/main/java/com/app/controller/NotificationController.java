package com.app.controller;

import com.app.dto.NotificationDto;
import com.app.dto.NotificationsDto;
import com.app.model.Notification;
import com.app.security.SecurityUtil;
import com.app.service.NotificationService;
import com.app.utils.converters.NotificationToNotificationDto;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationToNotificationDto notificationConverter;

    public NotificationController(NotificationService notificationService, NotificationToNotificationDto notificationConverter) {
        this.notificationService = notificationService;
        this.notificationConverter = notificationConverter;
    }

    @GetMapping
    public NotificationsDto getAllNotifications(@RequestParam(name = "page", defaultValue = "1") Integer page) {
        Page<Notification> notificationPage = notificationService.getAllNotifications(page);
        return new NotificationsDto(notificationPage.map(notificationConverter::convert));
    }

    @PostMapping("/read")
    public ResponseEntity<String> readNotification(@RequestBody NotificationDto notificationDto) {
        String username = SecurityUtil.getSessionUser();
        if (username != null) {
            if (notificationService.readNotification(notificationDto.getId()))
                return new ResponseEntity<>("Message marked as read.", HttpStatus.OK);
        }
        return new ResponseEntity<>("You are not logged in.", HttpStatus.UNAUTHORIZED);
    }
}
