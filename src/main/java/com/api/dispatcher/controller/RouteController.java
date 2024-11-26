package com.api.dispatcher.controller;

import com.api.dispatcher.dto.RouteDto;
import com.api.dispatcher.dto.RouteRequest;
import com.api.dispatcher.dto.RoutesDto;
import com.api.dispatcher.dto.VehicleDto;
import com.api.dispatcher.model.Route;
import com.api.dispatcher.model.Vehicle;
import com.api.dispatcher.service.NotificationService;
import com.api.dispatcher.service.RouteService;
import com.api.dispatcher.service.VehicleService;
import com.api.dispatcher.utils.converters.RouteToRouteDto;
import com.api.dispatcher.utils.converters.VehicleToVehicleDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

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

    @GetMapping("/search")
    public RoutesDto searchRoutes(@RequestParam(name = "page", defaultValue = "1") Integer page,
                                  @RequestBody @Valid HashMap<String, String> searchCriteria) {
        Page<Route> routePage = routeService.searchRoute(page, searchCriteria);
        return new RoutesDto(routePage.map(routeConverter::convert));
    }

    @PostMapping("/add")
    public ResponseEntity<String> addRoute(@RequestBody @Valid RouteDto routeDto, BindingResult bindingResult) {
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
        routeService.addRoute(routeDto);
        notificationService.sendNewRouteNotification(routeDto.getVehicleDto().getUserId(),
                routeDto.getVehicleDto().getManufacturer() + " " + routeDto.getVehicleDto().getModel(),
                routeDto.getStartLocation() + " to " + routeDto.getEndLocation());
        return new ResponseEntity<>("Route created successfully", HttpStatus.CREATED);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteRoute(@RequestBody @Valid HashMap<String, String> searchCriteria, VehicleDto vehicleDto) {
        boolean isDeleted = routeService.deleteRoute(searchCriteria);
        String startLocation = searchCriteria.get("startLocation");
        String endLocation = searchCriteria.get("endLocation");

        if (isDeleted) {
            notificationService.sendDeletedRouteNotification(vehicleDto.getUserId(),
                    vehicleDto.getManufacturer() + " " + vehicleDto.getModel(),
                    startLocation + " to " + endLocation);
            return new ResponseEntity<>("Route deleted successfully", HttpStatus.OK);
        }
        return new ResponseEntity<>("Route not found", HttpStatus.NOT_FOUND);
    }

}
