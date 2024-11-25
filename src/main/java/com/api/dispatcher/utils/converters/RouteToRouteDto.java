package com.api.dispatcher.utils.converters;

import com.api.dispatcher.dto.RouteDto;
import com.api.dispatcher.model.Route;
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
                source.getVehicle(),
                source.getUserId()
        );
    }
}
