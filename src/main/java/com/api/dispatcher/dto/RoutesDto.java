package com.api.dispatcher.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class RoutesDto {
    private List<RouteDto> vehicleDtoList;
    private int totalElements;
    private int totalPages;
    private int currentPage;
    @JsonProperty("isFirst")
    private boolean isFirst;
    @JsonProperty("isLast")
    private boolean isLast;
    private boolean hasNext;
    private boolean hasPrev;

    public RoutesDto(Page<RouteDto> routeDtoPage) {
        this.setVehicleDtoList(routeDtoPage.getContent());
        this.setTotalElements(routeDtoPage.getNumberOfElements());
        this.setTotalPages(routeDtoPage.getTotalPages());
        this.setCurrentPage(routeDtoPage.getNumber());
        this.setFirst(routeDtoPage.isFirst());
        this.setLast(routeDtoPage.isLast());
        this.setHasNext(routeDtoPage.hasNext());
        this.setHasPrev(routeDtoPage.hasPrevious());
    }
}
