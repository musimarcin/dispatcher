package com.app.service;

import com.app.model.Notification;
import com.app.repository.NotificationRepo;
import com.app.repository.UserRepo;
import com.app.security.SecurityUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


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

    private Long getUser() {
        String username = SecurityUtil.getSessionUser();
        return userRepo.findByUsername(username).getId();
    }

    public Page<Notification> getAllNotifications(Integer page) {
        return notificationRepo.findByUserId(getUser(), getPage(page));
    }
}
