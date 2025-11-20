package com.app.controller;

import com.app.dto.NotificationDto;
import com.app.dto.NotificationsDto;
import com.app.security.SecurityUtil;
import com.app.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
    public ResponseEntity<?> getAllNotifications(@RequestParam(name = "page", defaultValue = "1") Integer page) {
        if (securityUtil.getSessionUser() == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not logged in"));
        Page<NotificationDto> notificationDtoPage = notificationService.getAllNotifications(securityUtil.getSessionUser(), page);
        if (notificationDtoPage.isEmpty())
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(Map.of("message", "Notification not found"));
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("body", new NotificationsDto(notificationDtoPage)));
    }

    @PostMapping
    public ResponseEntity<?> readNotification(@RequestBody NotificationDto notificationDto) {
        if (securityUtil.getSessionUser() == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not logged in"));
        if (!notificationService.readNotification(notificationDto.getId()))
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(Map.of("message", "Notification not found"));
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Message marked as read"));
    }

}
