package com.api.dispatcher.controller;

import com.api.dispatcher.dto.CreateRouteRequest;
import com.api.dispatcher.dto.RouteDto;
import com.api.dispatcher.dto.RoutesDto;
import com.api.dispatcher.dto.VehicleDto;
import com.api.dispatcher.model.Route;
import com.api.dispatcher.model.Vehicle;
import com.api.dispatcher.service.NotificationService;
import com.api.dispatcher.service.RouteService;
import com.api.dispatcher.service.VehicleService;
import com.api.dispatcher.utils.converters.RouteToRouteDto;
import com.api.dispatcher.utils.converters.VehicleToVehicleDto;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.HashSet;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private final RouteService routeService;
    private final RouteToRouteDto routeConverter;
    private final NotificationService notificationService;
    private final VehicleService vehicleService;
    private final VehicleToVehicleDto vehicleConverter;

    public RouteController(RouteService routeService, RouteToRouteDto routeConverter, NotificationService notificationService, VehicleService vehicleService, VehicleToVehicleDto vehicleConverter) {
        this.routeService = routeService;
        this.routeConverter = routeConverter;
        this.notificationService = notificationService;
        this.vehicleService = vehicleService;
        this.vehicleConverter = vehicleConverter;
    }

    @GetMapping
    public RoutesDto getAllRoutes(@RequestParam(name = "page", defaultValue = "1" ) Integer page) {
        Page<Route> routePage = routeService.getAllRoutes(page);
        return new RoutesDto(routePage.map(routeConverter::convert));
    }

    @PostMapping("/add")
    public ResponseEntity<String> addRoute(@RequestBody CreateRouteRequest request, BindingResult bindingResult) {
        //check if any of notnull/notblank fields are empty
        if (bindingResult.hasErrors()) {
            StringBuilder errorMessage = new StringBuilder();
            bindingResult.getAllErrors().forEach(e -> {
                errorMessage.append(e.getDefaultMessage()).append(" ");
            });
            return new ResponseEntity<>(errorMessage.toString(), HttpStatus.BAD_REQUEST);
        }
        //TODO google api implementation and calculation for the rest of RouteDto
        //change start/end location from latitude/longitude to location name
        HashMap<String, String> licensePlate = new HashMap<>();
        licensePlate.put("licensePlate", request.getVehicleLicensePlate());
        Page<Vehicle> vehiclePage = vehicleService.searchVehicles(0, licensePlate);
        Page<VehicleDto> vehicleDtoPage = vehiclePage.map(vehicleConverter::convert);
        VehicleDto vehicleDto = vehicleDtoPage.getContent().getFirst();
        notificationService.sendNewRouteNotification(vehicleDto.getUserId(),
                vehicleDto.getManufacturer() + " " + vehicleDto.getModel(),
                request.getStartLocation() + " to " + request.getEndLocation());
        return new ResponseEntity<>("Route created successfully", HttpStatus.CREATED);
    }
}
