package com.app.controller;

import com.app.dto.NotificationDto;
import com.app.dto.NotificationsDto;
import com.app.security.SecurityUtil;
import com.app.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final SecurityUtil securityUtil;

    public NotificationController(NotificationService notificationService, SecurityUtil securityUtil) {
        this.notificationService = notificationService;
        this.securityUtil = securityUtil;
    }

    @GetMapping
    public NotificationsDto getAllNotifications(@RequestParam(name = "page", defaultValue = "1") Integer page) {
        Page<NotificationDto> notificationDtoPage = notificationService.getAllNotifications(securityUtil.getSessionUser(), page);
        return new NotificationsDto(notificationDtoPage);
    }

    @PostMapping("/read")
    public ResponseEntity<String> readNotification(@RequestBody NotificationDto notificationDto) {
        String username = securityUtil.getSessionUser();
        if (username != null) {
            if (notificationService.readNotification(notificationDto.getId()))
                return ResponseEntity.status(HttpStatus.OK).body("Message marked as read.");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not logged in.");
    }
}
