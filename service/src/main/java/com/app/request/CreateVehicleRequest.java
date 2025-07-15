package com.app.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

@Data
public class CreateVehicleRequest {
    private String licensePlate;
    private String model;
    private String manufacturer;
    private BigDecimal fuelCapacity;
    private BigDecimal averageConsumption;
    private Integer mileage;
    private Date lastMaintenance;
    private Instant createdAt;
    private Long userId;
}
