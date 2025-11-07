package com.app.controller;

import com.app.dto.VehicleDto;
import com.app.dto.VehiclesDto;
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


    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping
    public VehiclesDto getAllVehicles(@RequestParam(name = "page", defaultValue = "1" ) Integer page) {
        Page<VehicleDto> vehicleDtoPage = vehicleService.getAllVehicles(page);
        return new VehiclesDto(vehicleDtoPage);
    }

    @PostMapping("/search")
    public VehiclesDto searchVehicles(@RequestParam(name = "page", defaultValue = "1") Integer page,
                                      @RequestBody HashMap<String, String> searchCriteria) {
        Page<VehicleDto> vehicleDtoPage = vehicleService.searchVehicles(page, searchCriteria);
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
        vehicleService.addVehicle(vehicleDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Vehicle added successfully");
    }

    @DeleteMapping
    public ResponseEntity<String> deleteVehicle(@RequestParam(name = "licensePlate") @Valid String licensePlate) {
        boolean isDeleted = vehicleService.deleteVehicle(licensePlate);
        if (isDeleted) return ResponseEntity.status(HttpStatus.OK).body("Vehicle deleted successfully");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Vehicle not found");
    }

}
