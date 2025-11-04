package com.app.controller;

import com.app.dto.RouteDto;
import com.app.dto.RoutesDto;
import com.app.model.Route;
import com.app.service.RouteService;
import com.app.utils.converters.RouteDtoToRoute;
import com.app.utils.converters.RouteToRouteDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("/api/route")
public class RouteController {

    private final RouteService routeService;
    private final RouteToRouteDto routeConverter;
    private final RouteDtoToRoute routeDtoConverter;

    public RouteController(RouteService routeService, RouteToRouteDto routeConverter, RouteDtoToRoute routeDtoConverter) {
        this.routeService = routeService;
        this.routeConverter = routeConverter;
        this.routeDtoConverter = routeDtoConverter;
    }

    @GetMapping
    public RoutesDto getAllRoutes(@RequestParam(name = "page", defaultValue = "1") Integer page) {
        Page<Route> routePage = routeService.getAllRoutes(page);
        return new RoutesDto(routePage.map(routeConverter::convert));
    }

    @GetMapping("/vehicle")
    public RoutesDto getVehicleRoutes(@RequestParam(name = "page", defaultValue = "1") Integer page,
                                      @RequestParam(name = "licensePlate") String licensePlate) {
        Page<Route> routePage = routeService.getVehicleRoutes(licensePlate, page);
        return new RoutesDto(routePage.map(routeConverter::convert));
    }

    @PostMapping("/search")
    public RoutesDto searchRoutes(@RequestParam(name = "page", defaultValue = "1") Integer page,
                                  @RequestBody HashMap<String, String> searchCriteria) {
        Page<Route> routePage = routeService.searchRoute(page, searchCriteria);
        return new RoutesDto(routePage.map(routeConverter::convert));
    }

    @PostMapping
    public ResponseEntity<String> addRoute(@RequestBody @Valid RouteDto routeDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder errorMessage = new StringBuilder();
            bindingResult.getAllErrors().forEach(e ->
                    errorMessage.append(e.getDefaultMessage()).append(" "));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage.toString());
        }
        routeService.addRoute(routeDtoConverter.convert(routeDto));
        return ResponseEntity.status(HttpStatus.CREATED).body("Route added successfully");
    }

    @DeleteMapping
    public ResponseEntity<String> deleteRoute(@RequestBody @Valid HashMap<String, String> searchCriteria) {
        boolean isDeleted = routeService.deleteRoute(searchCriteria);
        if (isDeleted) return ResponseEntity.status(HttpStatus.OK).body("Route deleted successfully");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Route not found");
    }
}
