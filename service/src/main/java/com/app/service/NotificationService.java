package com.app.service;

import com.app.dto.NotificationDto;
import com.app.model.Notification;
import com.app.repository.NotificationRepo;
import com.app.repository.UserRepo;
import com.app.security.SecurityUtil;
import com.app.converters.NotificationToNotificationDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


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

    private Long getUser() {
        String username = SecurityUtil.getSessionUser();
        return userRepo.findByUsername(username).get().getId();
    }

    public Page<NotificationDto> getAllNotifications(Integer page) {
        Page<Notification> notificationPage = notificationRepo.findByUserId(getUser(), getPage(page));
        return notificationPage.map(notificationConverter::convert);
    }

    public boolean readNotification(Long id) {
        if (notificationRepo.findById(id).isPresent()) {
            Notification notification = notificationRepo.findById(id).get();
            notification.setIsRead(true);
            notificationRepo.save(notification);
            return true;
        }
        return false;
    }
}
