package com.app.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

@Data
@AllArgsConstructor
public class VehicleDto {
    private Long id;
    @NotBlank(message = "License plate is required")
    private String licensePlate;
    @NotBlank(message = "Model is required")
    private String model;
    @NotBlank(message = "Manufacturer is required")
    private String manufacturer;
    @NotNull(message = "Production year is required")
    @Min(value = 1900, message = "Production year incorrect")
    @Max(value = 2200, message = "Production year incorrect")
    private Integer productionYear;
    @NotNull(message = "Fuel capacity is required")
    @Min(value = 1, message = "Fuel capacity incorrect")
    private BigDecimal fuelCapacity;
    @Min(value = 1, message = "Average Consumption incorrect")
    private BigDecimal averageConsumption;
    @NotNull(message = "Mileage is required")
    @Min(value = 0, message = "Mileage incorrect")
    private Integer mileage;
    private Date lastMaintenance;
    private Instant createdAt;
    private Long userId;

}
