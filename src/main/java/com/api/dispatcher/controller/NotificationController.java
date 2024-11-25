package com.api.dispatcher.controller;

import com.api.dispatcher.dto.NotificationDto;
import com.api.dispatcher.dto.NotificationsDto;
import com.api.dispatcher.model.Notification;
import com.api.dispatcher.service.NotificationService;
import com.api.dispatcher.utils.converters.NotificationToNotificationDto;
import org.springframework.data.domain.Page;
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
    public NotificationsDto getAllNotifications(@RequestParam(name = "page", defaultValue = "1" ) Integer page) {
        Page<Notification> notificationPage = notificationService.getAllNotifications(page);
        return new NotificationsDto(notificationPage.map(notificationConverter::convert));
    }

}
