package com.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class NotificationDto {
    private Long id;
    private String message;
    private Boolean isRead;
    private Instant createdAt;
    private Long userId;
}
