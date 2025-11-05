package com.app.utils.converters;

import com.app.dto.RouteDto;
import com.app.model.Route;
import com.app.model.RoutePoint;
import com.app.model.Vehicle;
import com.app.service.VehicleService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

@Component
public class RouteDtoToRoute implements Converter<RouteDto, Route> {

    private final VehicleService vehicleService;

    public RouteDtoToRoute(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
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

        HashMap<String, String> vehiclePlate = new HashMap<>();
        vehiclePlate.put("licensePlate", source.getLicensePlate());
        Vehicle vehicle;
        if (vehicleService.searchVehicles(1, vehiclePlate).stream().findFirst().isPresent()) {
            vehicle = vehicleService.searchVehicles(1, vehiclePlate).stream().findFirst().get();
        } else return null;

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

        route.setVehicle(vehicle);
        route.setWaypoints(waypoints);
        return route;
    }
}
