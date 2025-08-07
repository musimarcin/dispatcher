package com.app.utils.converters;

import com.app.dto.RouteDto;
import com.app.model.Route;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class RouteToRouteDto implements Converter<Route, RouteDto> {

    @Override
    public RouteDto convert(Route source) {
        return new RouteDto(
                source.getId(),
                source.getStartLocation(),
                source.getEndLocation(),
                source.getDistance(),
                source.getEstimatedTime(),
                source.getStartTime(),
                source.getEndTime(),
                source.getStatus(),
                source.getCreatedAt(),
                source.getVehicle().getLicensePlate(),
                source.getUserId()
        );
    }
}
