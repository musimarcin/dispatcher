package com.app.controller;

import com.app.dto.VehicleDto;
import com.app.dto.VehiclesDto;
import com.app.security.SecurityUtil;
import com.app.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vehicle")
public class VehicleController {

    private final VehicleService vehicleService;
    private final SecurityUtil securityUtil;


    public VehicleController(VehicleService vehicleService, SecurityUtil securityUtil) {
        this.vehicleService = vehicleService;
        this.securityUtil = securityUtil;
    }

    @GetMapping
    public ResponseEntity<?> getAllVehicles(@RequestParam(name = "page", defaultValue = "1" ) Integer page) {
        String username = securityUtil.getSessionUser();
        if (username == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not logged in"));
        Page<VehicleDto> vehicles = vehicleService.getAllVehicles(username, page);
        if (vehicles.isEmpty())
            return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "No vehicles found"));
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("body", new VehiclesDto(vehicles)));
    }

    @GetMapping(params = "id")
    public ResponseEntity<?> getById(@RequestParam(name = "id") Long id) {
        if (securityUtil.getSessionUser() == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not logged in"));
        VehicleDto vehicleDto = vehicleService.getById(id);
        if (vehicleDto == null)
            return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "No vehicles found"));
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("body", vehicleDto));
    }

    @GetMapping(params = "driver")
    public ResponseEntity<?> getByDriver(@RequestParam(name = "page", defaultValue = "1" ) Integer page,
                                         @RequestParam(name = "driver") String driver) {
        String username = securityUtil.getSessionUser();
        if (username == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not logged in"));
        Page<VehicleDto> vehicleDtoPage = vehicleService.getUsersVehicles(driver, page);
        if (vehicleDtoPage.isEmpty())
            return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "No vehicles found"));
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("body", new VehiclesDto(vehicleDtoPage)));
    }

    @PostMapping("/search")
    public ResponseEntity<?> searchVehicles(@RequestParam(name = "page", defaultValue = "1") Integer page,
                                      @RequestBody Map<String, String> searchCriteria) {
        String username = securityUtil.getSessionUser();
        if (username == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not logged in"));
        Page<VehicleDto> vehicleDtoPage = vehicleService.searchVehicles(username, page, searchCriteria);
        if (vehicleDtoPage.isEmpty())
            return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "No vehicles found"));
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("body", new VehiclesDto(vehicleDtoPage)));
    }

    @PostMapping
    public ResponseEntity<?> addVehicle(@RequestBody @Valid VehicleDto vehicleDto) {
        VehicleDto created = vehicleService.addVehicle(securityUtil.getSessionUser(), vehicleDto);
        if (created == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not logged in"));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("body", created, "message", "Vehicle added successfully"));
    }

    @DeleteMapping
    public ResponseEntity<?> deleteVehicle(@RequestParam(name = "licensePlate") String licensePlate) {
        String username = securityUtil.getSessionUser();
        if (username == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not logged in"));
        if (!vehicleService.deleteVehicle(username, licensePlate))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Vehicle not found"));
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Vehicle deleted successfully"));
    }

    @PutMapping("/route")
    public ResponseEntity<?> editVehicleAfterRoute(@RequestBody VehicleDto vehicleDto) {
        String username = securityUtil.getSessionUser();
        if (username == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not logged in"));
        if (!vehicleService.editVehicleAfterRoute(username, vehicleDto))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Unsuccessful vehicle update"));
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Successfully updated vehicle"));
    }

}
