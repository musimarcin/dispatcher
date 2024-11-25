package com.api.dispatcher.utils.converters;

import com.api.dispatcher.dto.NotificationDto;
import com.api.dispatcher.model.Notification;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class NotificationToNotificationDto implements Converter<Notification, NotificationDto> {

    @Override
    public NotificationDto convert(Notification source) {
        return new NotificationDto(
                source.getUserId(),
                source.getMessage(),
                source.getIsRead(),
                source.getCreatedAt(),
                source.getUserId()
        );
    }
}
