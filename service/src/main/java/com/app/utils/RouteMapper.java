package com.app.utils;

import com.app.dto.RouteDto;
import com.app.model.Route;
import com.app.model.RoutePoint;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RouteMapper {

    private final VehicleDtoToVehicle converter;

    public RouteMapper(VehicleDtoToVehicle converter) {
        this.converter = converter;
    }

    public void update(Route route, RouteDto routeDto) {
        if (routeDto.getDistance() != null) route.setDistance(routeDto.getDistance());
        if (routeDto.getEstimatedTime() != null) route.setEstimatedTime(routeDto.getEstimatedTime());
        if (routeDto.getStartTime() != null) route.setStartTime(routeDto.getStartTime());
        if (routeDto.getEndTime() != null) route.setEndTime(routeDto.getEndTime());
        if (routeDto.getStatus() != null) route.setStatus(routeDto.getStatus());
        if (routeDto.getVehicleDto() != null) route.setVehicle(converter.convert(routeDto.getVehicleDto()));
        if (routeDto.getWaypoints() != null) {
            List<RoutePoint> waypoints = routeDto.getWaypoints().stream()
                    .map(w -> {
                        RoutePoint routePoint = new RoutePoint();
                        routePoint.setName(w.getName());
                        routePoint.setLatitude(w.getLatitude());
                        routePoint.setLongitude(w.getLongitude());
                        routePoint.setSequence(w.getSequence());
                        routePoint.setRoute(route);
                        return routePoint;
                    }).toList();

            if (routeDto.getWaypoints() != null) route.setWaypoints(waypoints);
        }
    }
}
