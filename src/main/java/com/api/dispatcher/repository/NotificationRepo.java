package com.api.dispatcher.repository;

import com.api.dispatcher.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepo extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserId(Long userId, Pageable page);
}
