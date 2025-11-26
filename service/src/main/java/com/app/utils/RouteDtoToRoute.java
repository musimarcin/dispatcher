package com.app.utils;

import com.app.dto.RouteDto;
import com.app.model.Route;
import com.app.model.RoutePoint;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RouteDtoToRoute implements Converter<RouteDto, Route> {

    private final VehicleDtoToVehicle vehicleDtoConverter;

    public RouteDtoToRoute(VehicleDtoToVehicle vehicleDtoConverter) {
        this.vehicleDtoConverter = vehicleDtoConverter;
    }

    @Override
    public Route convert(RouteDto source) {
        Route route = new Route();
        route.setId(source.getId());
        route.setDistance(source.getDistance());
        route.setEstimatedTime(source.getEstimatedTime());
        route.setStartTime(source.getStartTime());
        route.setEndTime(source.getEndTime());
        route.setStatus(source.getStatus());
        route.setCreatedAt(source.getCreatedAt());
        route.setUserId(source.getUserId());

        List<RoutePoint> waypoints = source.getWaypoints().stream()
                .map(w -> {
                    RoutePoint routePoint = new RoutePoint();
                    routePoint.setName(w.getName());
                    routePoint.setLatitude(w.getLatitude());
                    routePoint.setLongitude(w.getLongitude());
                    routePoint.setSequence(w.getSequence());
                    routePoint.setRoute(route);
                    return routePoint;
                }).toList();

        route.setVehicle(vehicleDtoConverter.convert(source.getVehicleDto()));
        route.setWaypoints(waypoints);
        return route;
    }
}
