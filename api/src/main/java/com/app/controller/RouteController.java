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
import java.util.Map;

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
    public ResponseEntity<?> getAllRoutes(@RequestParam(name = "page", defaultValue = "1") Integer page) {
        Page<RouteDto> routePage = routeService.getAllRoutes(securityUtil.getSessionUser(), page);
        if (routePage == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in");
        return ResponseEntity.status(HttpStatus.OK).body(new RoutesDto(routePage));
    }

    @GetMapping("/vehicle")
    public ResponseEntity<?> getVehicleRoutes(@RequestParam(name = "page", defaultValue = "1") Integer page,
                                      @RequestParam(name = "licensePlate") String licensePlate) {
        if (securityUtil.getSessionUser().isEmpty())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in");
        Page<RouteDto> routePage = routeService.getVehicleRoutes(securityUtil.getSessionUser(), licensePlate, page);
        if (routePage.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Routes not found");
        return ResponseEntity.status(HttpStatus.OK).body(new RoutesDto(routePage));
    }

    @PostMapping("/search")
    public ResponseEntity<?> searchRoutes(@RequestParam(name = "page", defaultValue = "1") Integer page,
                                  @RequestBody HashMap<String, String> searchCriteria) {
        if (securityUtil.getSessionUser().isEmpty())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in");
        Page<RouteDto> routeDtoPage = routeService.searchRoute(securityUtil.getSessionUser(), page, searchCriteria);
        if (routeDtoPage.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Routes not found");
        return ResponseEntity.status(HttpStatus.OK).body(new RoutesDto(routeDtoPage));
    }

    @PostMapping
    public ResponseEntity<?> addRoute(@RequestBody @Valid RouteDto routeDto, BindingResult bindingResult) {
        if (securityUtil.getSessionUser().isEmpty())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in");
        if (bindingResult.hasErrors()) {
            StringBuilder errorMessage = new StringBuilder();
            bindingResult.getAllErrors().forEach(e ->
                    errorMessage.append(e.getDefaultMessage()).append(" "));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", errorMessage.toString()));
        }
        RouteDto created = routeService.addRoute(securityUtil.getSessionUser(), routeDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Route added successfully", "route", created));
    }

    @DeleteMapping
    public ResponseEntity<String> deleteRoute(@RequestParam(name = "id") @Valid String id) {
        if (securityUtil.getSessionUser().isEmpty())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in");
        Long routeId = Long.valueOf(id);
        if (!routeService.deleteRoute(routeId))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Route not found");

        return ResponseEntity.status(HttpStatus.OK).body("Route deleted successfully");
    }
}
