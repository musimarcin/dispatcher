package com.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class FuelHistoriesDto {

    private List<FuelHistoryDto> fuelHistoryDtoList;
    private int totalElements;
    private int totalPages;
    private int currentPage;
    @JsonProperty("isFirst")
    private boolean isFirst;
    @JsonProperty("isLast")
    private boolean isLast;
    private boolean hasNext;
    private boolean hasPrev;

    public FuelHistoriesDto(Page<FuelHistoryDto> fuelHistoryDtos) {
        this.setFuelHistoryDtoList(fuelHistoryDtos.getContent());
        this.setTotalElements(fuelHistoryDtos.getNumberOfElements());
        this.setTotalPages(fuelHistoryDtos.getTotalPages());
        this.setCurrentPage(fuelHistoryDtos.getNumber());
        this.setFirst(fuelHistoryDtos.isFirst());
        this.setLast(fuelHistoryDtos.isLast());
        this.setHasNext(fuelHistoryDtos.hasNext());
        this.setHasPrev(fuelHistoryDtos.hasPrevious());
    }
}
