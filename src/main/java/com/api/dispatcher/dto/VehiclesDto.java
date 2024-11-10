package com.api.dispatcher.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class VehiclesDto {
    private List<VehicleDto> vehicleDtoList;
    private int totalElements;
    private int totalPages;
    private int currentPage;
    @JsonProperty("isFirst")
    private boolean isFirst;
    @JsonProperty("isLast")
    private boolean isLast;
    private boolean hasNext;
    private boolean hasPrev;

    public VehiclesDto(Page<VehicleDto> vehicleDtoPage) {
        this.setVehicleDtoList(vehicleDtoPage.getContent());
        this.setTotalElements(vehicleDtoPage.getNumberOfElements());
        this.setTotalPages(vehicleDtoPage.getTotalPages());
        this.setCurrentPage(vehicleDtoPage.getNumber());
        this.setFirst(vehicleDtoPage.isFirst());
        this.setLast(vehicleDtoPage.isLast());
        this.setHasNext(vehicleDtoPage.hasNext());
        this.setHasPrev(vehicleDtoPage.hasPrevious());
    }
}