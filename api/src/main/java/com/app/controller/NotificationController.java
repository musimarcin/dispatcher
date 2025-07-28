package com.app.controller;

import com.app.dto.NotificationsDto;
import com.app.model.Notification;
import com.app.service.NotificationService;
import com.app.utils.converters.NotificationToNotificationDto;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
