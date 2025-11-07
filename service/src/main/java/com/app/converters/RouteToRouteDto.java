package com.app.converters;

import com.app.dto.RouteDto;
import com.app.dto.RoutePointDto;
import com.app.model.Route;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RouteToRouteDto implements Converter<Route, RouteDto> {

    private final VehicleToVehicleDto vehicleConverter;

    public RouteToRouteDto(VehicleToVehicleDto vehicleConverter) {
        this.vehicleConverter = vehicleConverter;
    }

    @Override
    public RouteDto convert(Route source) {
        RouteDto routeDto = new RouteDto();
        routeDto.setId(source.getId());
        routeDto.setDistance(source.getDistance());
        routeDto.setEstimatedTime(source.getEstimatedTime());
        routeDto.setStartTime(source.getStartTime());
        routeDto.setEndTime(source.getEndTime());
        routeDto.setStatus(source.getStatus());
        routeDto.setCreatedAt(source.getCreatedAt());
        routeDto.setVehicleDto(vehicleConverter.convert(source.getVehicle()));
        routeDto.setUserId(source.getUserId());

        List<RoutePointDto> waypoints = source.getWaypoints().stream()
                .map(w -> {
                    RoutePointDto routePointDto = new RoutePointDto();
                    routePointDto.setId(w.getId());
                    routePointDto.setName(w.getName());
                    routePointDto.setLatitude(w.getLatitude());
                    routePointDto.setLongitude(w.getLongitude());
                    routePointDto.setSequence(w.getSequence());
                    return routePointDto;
                }).toList();

        routeDto.setWaypoints(waypoints);
        return routeDto;
    }
}
