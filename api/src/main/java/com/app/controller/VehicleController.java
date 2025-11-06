package com.app.controller;

import com.app.dto.VehicleDto;
import com.app.dto.VehiclesDto;
import com.app.model.Vehicle;
import com.app.service.VehicleService;
import com.app.utils.converters.VehicleDtoToVehicle;
import com.app.utils.converters.VehicleToVehicleDto;
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
    private final VehicleToVehicleDto vehicleConverter;
    private final VehicleDtoToVehicle vehicleDtoConverter;

    public VehicleController(VehicleService vehicleService, VehicleToVehicleDto vehicleConverter, VehicleDtoToVehicle vehicleDtoConverter) {
        this.vehicleService = vehicleService;
        this.vehicleConverter = vehicleConverter;
        this.vehicleDtoConverter = vehicleDtoConverter;
    }

    @GetMapping
    public VehiclesDto getAllVehicles(@RequestParam(name = "page", defaultValue = "1" ) Integer page) {
        Page<Vehicle> vehiclePage = vehicleService.getAllVehicles(page);
        return new VehiclesDto(vehiclePage.map(vehicleConverter::convert));
    }

    @PostMapping("/search")
    public VehiclesDto searchVehicles(@RequestParam(name = "page", defaultValue = "1") Integer page,
                                      @RequestBody HashMap<String, String> searchCriteria) {
        Page<Vehicle> vehiclePage = vehicleService.searchVehicles(page, searchCriteria);
        return new VehiclesDto(vehiclePage.map(vehicleConverter::convert));
    }

    @PostMapping
    public ResponseEntity<String> addVehicle(@RequestBody @Valid VehicleDto vehicleDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder errorMessage = new StringBuilder();
            bindingResult.getAllErrors().forEach(e ->
                    errorMessage.append(e.getDefaultMessage()).append(" "));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage.toString());
        }
        vehicleService.addVehicle(vehicleDtoConverter.convert(vehicleDto));
        return ResponseEntity.status(HttpStatus.CREATED).body("Vehicle added successfully");
    }

    @DeleteMapping
    public ResponseEntity<String> deleteVehicle(@RequestParam(name = "licensePlate") @Valid String licensePlate) {
        boolean isDeleted = vehicleService.deleteVehicle(licensePlate);
        if (isDeleted) return ResponseEntity.status(HttpStatus.OK).body("Vehicle deleted successfully");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Vehicle not found");
    }

}
