package com.app.dto.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class VehicleUpdateRequest {

    @NotNull(message = "Insert selected vehicle id")
    private Long id;
    @NotNull(message = "Insert average consumption")
    private BigDecimal averageConsumption;
    @NotNull(message = "Insert new mileage")
    private Integer mileage;

    public VehicleUpdateRequest(Long id, BigDecimal averageConsumption, Integer mileage) {
        this.id = id;
        this.averageConsumption = averageConsumption;
        this.mileage = mileage;
    }
}
