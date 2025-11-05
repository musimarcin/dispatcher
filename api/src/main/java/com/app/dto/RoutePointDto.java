package com.app.dto;

import com.app.model.Route;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoutePointDto {
    private Long id;
    private String name;
    private Double latitude;
    private Double longitude;
    private Integer sequence;
    private Route route;
}