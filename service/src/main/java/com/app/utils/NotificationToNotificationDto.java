package com.app.utils;

import com.app.dto.NotificationDto;
import com.app.model.Notification;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class NotificationToNotificationDto implements Converter<Notification, NotificationDto> {

    @Override
    public NotificationDto convert(Notification source) {
        return new NotificationDto(
                source.getId(),
                source.getMessage(),
                source.getIsRead(),
                source.getCreatedAt(),
                source.getUserId()
        );
    }
}
