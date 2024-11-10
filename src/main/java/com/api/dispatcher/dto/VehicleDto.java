package com.api.dispatcher.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

@Data
@AllArgsConstructor
public class VehicleDto {
    private Long id;
    private String licensePlate;
    private String model;
    private String manufacturer;
    private BigDecimal fuelCapacity;
    private BigDecimal averageConsumption;
    private Integer mileage;
    private Date lastMaintenance;
    private Instant createdAt;
    private UserDto user;

}
