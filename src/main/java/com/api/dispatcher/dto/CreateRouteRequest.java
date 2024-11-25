package com.api.dispatcher.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateRouteRequest {
    @NotBlank(message = "Starting location is required")
    private String startLocation;
    @NotBlank(message = "Finishing location is required")
    private String endLocation;
    @NotNull(message = "License plate of vehicle is required")
    private String vehicleLicensePlate;
}
