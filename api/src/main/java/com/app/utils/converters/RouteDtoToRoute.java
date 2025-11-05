package com.app.utils.converters;

import com.app.dto.RouteDto;
import com.app.model.Route;
import com.app.model.RoutePoints;
import com.app.model.Vehicle;
import com.app.service.VehicleService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RouteDtoToRoute implements Converter<RouteDto, Route> {

    private final VehicleService vehicleService;

    public RouteDtoToRoute(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @Override
    public Route convert(RouteDto source) {
        HashMap<String, String> vehiclePlate = new HashMap<>();
        vehiclePlate.put("licensePlate", source.getLicensePlate());
        Vehicle vehicle;
        if (vehicleService.searchVehicles(1, vehiclePlate).stream().findFirst().isPresent()) {
            vehicle = vehicleService.searchVehicles(1, vehiclePlate).stream().findFirst().get();
        } else return null;
        List<RoutePoints> waypoints = source.getWaypoints().stream()
                .map(w -> {
                    RoutePoints routePoint = new RoutePoints();
                    routePoint.setId(w.getId());
                    routePoint.setName(w.getName());
                    routePoint.setLatitude(w.getLatitude());
                    routePoint.setLongitude(w.getLongitude());
                    routePoint.setRoute(w.getRoute());
                    return routePoint;
                }).collect(Collectors.toList());
        return new Route(
                source.getId(),
                source.getDistance(),
                source.getEstimatedTime(),
                source.getStartTime(),
                source.getEndTime(),
                source.getStatus(),
                source.getCreatedAt(),
                vehicle,
                waypoints,
                source.getUserId()
        );
    }
}
