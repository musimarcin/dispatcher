package com.api.dispatcher.service;

import com.api.dispatcher.model.Notification;
import com.api.dispatcher.model.UserEntity;
import com.api.dispatcher.repository.NotificationRepo;
import com.api.dispatcher.repository.UserRepo;
import com.api.dispatcher.security.SecurityUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class NotificationService {

    private final NotificationRepo notificationRepo;
    private final UserRepo userRepo;

    public NotificationService(NotificationRepo notificationRepo, UserRepo userRepo) {
        this.notificationRepo = notificationRepo;
        this.userRepo = userRepo;
    }

    public Pageable getPage(Integer page) {
        int pageNo = page < 1 ? 0 : page - 1;
        return PageRequest.of(pageNo, 10);
    }

    private UserEntity getUser() {
        String username = SecurityUtil.getSessionUser();
        return userRepo.findByUsername(username);
    }

    public Page<Notification> getAllNotifications(Integer page) {
        Long userId = getUser().getId();
        return notificationRepo.findByUserId(userId, getPage(page));
    }

    public void sendNewRouteNotification(Long userId, String vehicleName, String routeDetails) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setMessage("New route has been set to vehicle " + vehicleName + " : " + routeDetails);
        notification.setCreatedAt(Instant.now());
        notification.setIsRead(false);

        notificationRepo.save(notification);
    }
}
