package com.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @NotNull(message = "Fuel capacity is required")
    private BigDecimal fuelCapacity;
    private BigDecimal averageConsumption;
    @NotNull(message = "Mileage is required")
    private Integer mileage;
    private Date lastMaintenance;
    private Instant createdAt;
    private Long userId;

}
