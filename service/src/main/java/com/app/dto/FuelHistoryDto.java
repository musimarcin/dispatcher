package com.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
public class FuelHistoryDto {

    private String id;
    private Double fuelConsumed;
    private Instant createdAt;
    private Long vehicleId;
    private Long routeId;
}
