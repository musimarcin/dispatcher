package com.app.service;

import com.app.dto.NotificationDto;
import com.app.model.Notification;
import com.app.model.UserEntity;
import com.app.repository.NotificationRepo;
import com.app.repository.UserRepo;
import com.app.utils.NotificationToNotificationDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class NotificationService {

    private final NotificationRepo notificationRepo;
    private final UserRepo userRepo;
    private final NotificationToNotificationDto notificationConverter;

    public NotificationService(NotificationRepo notificationRepo, UserRepo userRepo, NotificationToNotificationDto notificationConverter) {
        this.notificationRepo = notificationRepo;
        this.userRepo = userRepo;
        this.notificationConverter = notificationConverter;
    }

    public Pageable getPage(Integer page) {
        int pageNo = page < 1 ? 0 : page - 1;
        return PageRequest.of(pageNo, 10);
    }

    public Page<NotificationDto> getAllNotifications(String username, Integer page) {
        Optional<UserEntity> user = userRepo.findByUsername(username);
        if (user.isEmpty()) return Page.empty();
        Page<Notification> notificationPage = notificationRepo.findByUserId(user.get().getId(), getPage(page));
        if (notificationPage.isEmpty()) return Page.empty();
        return notificationPage.map(notificationConverter::convert);
    }

    public boolean readNotification(Long id) {
        Optional<Notification> notification = notificationRepo.findById(id);
        if (notification.isEmpty()) return false;
        notification.get().setIsRead(true);
        notificationRepo.save(notification.get());
        return true;
    }
}
