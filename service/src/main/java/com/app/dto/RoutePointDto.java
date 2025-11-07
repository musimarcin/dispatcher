package com.app.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoutePointDto {
    private Long id;
    @NotNull(message = "Name cannot be null")
    private String name;
    @NotNull(message = "Latitude cannot be null")
    private Double latitude;
    @NotNull(message = "Longitude cannot be null")
    private Double longitude;
    @NotNull(message = "Sequence cannot be null")
    private Integer sequence;
    private RouteDto routeDto;
}