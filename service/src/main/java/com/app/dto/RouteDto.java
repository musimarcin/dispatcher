package com.app.dto;

import com.app.model.RouteStatus;
import com.app.model.Vehicle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteDto {
    private Long id;
    @NotNull(message = "Distance cannot be null")
    private BigDecimal distance;
    private Integer estimatedTime;
    @NotNull(message = "Starting time is required")
    private Date startTime;
    private Date endTime;
    private RouteStatus status;
    private Instant createdAt;
    @NotBlank(message = "Vehicle plate is required")
    private VehicleDto vehicleDto;
    private List<RoutePointDto> waypoints;
    private Long userId;
}
