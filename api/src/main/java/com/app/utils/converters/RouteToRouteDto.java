package com.app.utils.converters;

import com.app.dto.RouteDto;
import com.app.dto.RoutePointDto;
import com.app.model.Route;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RouteToRouteDto implements Converter<Route, RouteDto> {

    @Override
    public RouteDto convert(Route source) {
        List<RoutePointDto> waypoints = source.getWaypoints().stream()
                .map(w -> {
                    RoutePointDto routePointDto = new RoutePointDto();
                    routePointDto.setId(w.getId());
                    routePointDto.setName(w.getName());
                    routePointDto.setLatitude(w.getLatitude());
                    routePointDto.setLongitude(w.getLongitude());
                    routePointDto.setRoute(w.getRoute());
                    return routePointDto;
                }).collect(Collectors.toList());
        return new RouteDto(
                source.getId(),
                source.getDistance(),
                source.getEstimatedTime(),
                source.getStartTime(),
                source.getEndTime(),
                source.getStatus(),
                source.getCreatedAt(),
                source.getVehicle().getLicensePlate(),
                waypoints,
                source.getUserId()
        );
    }
}
