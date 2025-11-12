package com.app.controller;

import com.app.dto.RouteDto;
import com.app.dto.RoutesDto;
import com.app.security.SecurityUtil;
import com.app.service.RouteService;
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
    private final SecurityUtil securityUtil;

    public RouteController(RouteService routeService, SecurityUtil securityUtil) {
        this.routeService = routeService;
        this.securityUtil = securityUtil;
    }

    @GetMapping
    public RoutesDto getAllRoutes(@RequestParam(name = "page", defaultValue = "1") Integer page) {
        Page<RouteDto> routePage = routeService.getAllRoutes(securityUtil.getSessionUser(), page);
        return new RoutesDto(routePage);
    }

    @GetMapping("/vehicle")
    public RoutesDto getVehicleRoutes(@RequestParam(name = "page", defaultValue = "1") Integer page,
                                      @RequestParam(name = "licensePlate") String licensePlate) {
        Page<RouteDto> routePage = routeService.getVehicleRoutes(licensePlate, page);
        return new RoutesDto(routePage);
    }

    @PostMapping("/search")
    public RoutesDto searchRoutes(@RequestParam(name = "page", defaultValue = "1") Integer page,
                                  @RequestBody HashMap<String, String> searchCriteria) {
        Page<RouteDto> routeDtoPage = routeService.searchRoute(securityUtil.getSessionUser(), page, searchCriteria);
        return new RoutesDto(routeDtoPage);
    }

    @PostMapping
    public ResponseEntity<String> addRoute(@RequestBody @Valid RouteDto routeDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder errorMessage = new StringBuilder();
            bindingResult.getAllErrors().forEach(e ->
                    errorMessage.append(e.getDefaultMessage()).append(" "));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage.toString());
        }
        routeService.addRoute(securityUtil.getSessionUser(), routeDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Route added successfully");
    }

    @DeleteMapping
    public ResponseEntity<String> deleteRoute(@RequestParam(name = "id") @Valid String id) {
        Long routeId = Long.valueOf(id);
        if (!routeService.deleteRoute(routeId))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Route not found");

        return ResponseEntity.status(HttpStatus.OK).body("Route deleted successfully");
    }
}
