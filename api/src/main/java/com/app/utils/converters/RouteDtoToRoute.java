package com.app.utils.converters;

import com.app.dto.RouteDto;
import com.app.model.Route;
import com.app.model.Vehicle;
import com.app.service.VehicleService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.HashMap;

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
        return new Route(
                source.getId(),
                source.getStartLocation(),
                source.getEndLocation(),
                source.getDistance(),
                source.getEstimatedTime(),
                source.getStartTime(),
                source.getEndTime(),
                source.getStatus(),
                source.getCreatedAt(),
                vehicle,
                source.getUserId()
        );
    }
}
