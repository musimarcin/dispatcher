package com.app.controller;

import com.app.dto.VehicleDto;
import com.app.dto.VehiclesDto;
import com.app.security.SecurityUtil;
import com.app.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;
    private final SecurityUtil securityUtil;


    public VehicleController(VehicleService vehicleService, SecurityUtil securityUtil) {
        this.vehicleService = vehicleService;
        this.securityUtil = securityUtil;
    }

    @GetMapping
    public VehiclesDto getAllVehicles(@RequestParam(name = "page", defaultValue = "1" ) Integer page) {
        Page<VehicleDto> vehicleDtoPage = vehicleService.getAllVehicles(securityUtil.getSessionUser(), page);
        return new VehiclesDto(vehicleDtoPage);
    }

    @PostMapping("/search")
    public VehiclesDto searchVehicles(@RequestParam(name = "page", defaultValue = "1") Integer page,
                                      @RequestBody HashMap<String, String> searchCriteria) {
        Page<VehicleDto> vehicleDtoPage = vehicleService.searchVehicles(securityUtil.getSessionUser(), page, searchCriteria);
        return new VehiclesDto(vehicleDtoPage);
    }

    @PostMapping
    public ResponseEntity<String> addVehicle(@RequestBody @Valid VehicleDto vehicleDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder errorMessage = new StringBuilder();
            bindingResult.getAllErrors().forEach(e ->
                    errorMessage.append(e.getDefaultMessage()).append(" "));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage.toString());
        }
        vehicleService.addVehicle(securityUtil.getSessionUser(), vehicleDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Vehicle added successfully");
    }

    @DeleteMapping
    public ResponseEntity<String> deleteVehicle(@RequestParam(name = "licensePlate") @Valid String licensePlate) {
        if (!vehicleService.deleteVehicle(licensePlate))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Vehicle not found");

        return ResponseEntity.status(HttpStatus.OK).body("Vehicle deleted successfully");
    }

}
