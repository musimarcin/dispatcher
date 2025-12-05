package com.app.controller;

import com.app.dto.RouteDto;
import com.app.dto.RoutesDto;
import com.app.dto.requests.RouteStatusRequest;
import com.app.security.SecurityUtil;
import com.app.service.RouteService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> getVehicleRoutes(@RequestParam(name = "page", defaultValue = "1") Integer page,
                                      @RequestParam(name = "licensePlate", defaultValue = "") String licensePlate) {
        String username = securityUtil.getSessionUser();
        if (username == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not logged in"));
        Page<RouteDto> routePage = routeService.getVehicleRoutes(username, licensePlate, page);
        if (routePage.isEmpty())
            return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "No routes found"));
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("body", new RoutesDto(routePage)));
    }

    @PostMapping("/search")
    public ResponseEntity<?> searchRoutes(@RequestParam(name = "page", defaultValue = "1") Integer page,
                                  @RequestBody HashMap<String, String> searchCriteria) {
        String username = securityUtil.getSessionUser();
        if (username == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not logged in"));
        Page<RouteDto> routeDtoPage = routeService.searchRoute(username, page, searchCriteria);
        if (routeDtoPage.isEmpty())
            return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "No routes found"));
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("body", new RoutesDto(routeDtoPage)));
    }

    @PostMapping
    public ResponseEntity<?> addRoute(@RequestBody @Valid RouteDto routeDto) {
        String username = securityUtil.getSessionUser();
        if (username == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not logged in"));
        RouteDto created = routeService.addRoute(username, routeDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Route added successfully", "body", created));
    }

    @DeleteMapping
    public ResponseEntity<?> deleteRoute(@RequestParam(name = "id") @Valid String id) {
        String username = securityUtil.getSessionUser();
        if (username == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not logged in"));
        Long routeId = Long.valueOf(id);
        if (!routeService.deleteRoute(username, routeId))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Route not found"));

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Route deleted successfully"));
    }

    @PutMapping
    public ResponseEntity<?> editRoute(@RequestBody @Valid RouteStatusRequest request) {
        String username = securityUtil.getSessionUser();
        if (username == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not logged in"));
        if (!routeService.editRoute(securityUtil.getSessionUser(), request))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Unsuccessful route update"));
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Successful route update"));
    }
}
