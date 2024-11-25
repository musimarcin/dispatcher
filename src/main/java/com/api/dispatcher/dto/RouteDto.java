package com.api.dispatcher.dto;

import com.api.dispatcher.model.Vehicle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

@Data
@AllArgsConstructor
public class RouteDto {

    private Long id;
    @NotBlank(message = "Starting location is required")
    private String startLocation;
    @NotBlank(message = "Starting location is required")
    private String endLocation;
    private BigDecimal distance;
    private Integer estimatedTime;
    private Date startTime;
    private Date endTime;
    private String status;
    private Instant createdAt;
    @NotNull(message = "Vehicle is required")
    private Vehicle vehicle;
    private Long userId;
}
