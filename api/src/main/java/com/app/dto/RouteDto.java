package com.app.dto;

import com.app.model.RouteStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

@Data
@AllArgsConstructor
public class RouteDto {
    private Long id;
    @NotBlank(message = "Start location is required")
    private String startLocation;
    @NotBlank(message = "End location is required")
    private String endLocation;
    private BigDecimal distance;
    private Integer estimatedTime;
    @NotBlank(message = "Starting time is required")
    private Date startTime;
    private Date endTime;
    private RouteStatus status;
    private Instant createdAt;
    @NotBlank(message = "Vehicle plate is required")
    private String licensePlate;
    private Long userId;
}
