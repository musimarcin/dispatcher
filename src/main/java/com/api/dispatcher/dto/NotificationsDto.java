package com.api.dispatcher.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class NotificationsDto {
    private List<NotificationDto> vehicleDtoList;
    private int totalElements;
    private int totalPages;
    private int currentPage;
    @JsonProperty("isFirst")
    private boolean isFirst;
    @JsonProperty("isLast")
    private boolean isLast;
    private boolean hasNext;
    private boolean hasPrev;

    public NotificationsDto(Page<NotificationDto> notificationDtoPage) {
        this.setVehicleDtoList(notificationDtoPage.getContent());
        this.setTotalElements(notificationDtoPage.getNumberOfElements());
        this.setTotalPages(notificationDtoPage.getTotalPages());
        this.setCurrentPage(notificationDtoPage.getNumber());
        this.setFirst(notificationDtoPage.isFirst());
        this.setLast(notificationDtoPage.isLast());
        this.setHasNext(notificationDtoPage.hasNext());
        this.setHasPrev(notificationDtoPage.hasPrevious());
    }
}
