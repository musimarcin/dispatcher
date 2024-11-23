package com.api.dispatcher.controller;

import com.api.dispatcher.dto.VehicleDto;
import com.api.dispatcher.dto.VehiclesDto;
import com.api.dispatcher.model.Vehicle;
import com.api.dispatcher.service.VehicleService;
import com.api.dispatcher.specifications.VehicleSpecifications;
import com.api.dispatcher.utils.converters.VehicleToVehicleDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;
    private final VehicleToVehicleDto vehicleConverter;

    public VehicleController(VehicleService vehicleService, VehicleToVehicleDto vehicleConverter) {
        this.vehicleService = vehicleService;
        this.vehicleConverter = vehicleConverter;
    }

    @GetMapping
    public VehiclesDto getAllVehicles(@RequestParam(name = "page", defaultValue = "1" ) Integer page) {
        Page<Vehicle> vehiclePage = vehicleService.getAllVehicles(page);
        return new VehiclesDto(vehiclePage.map(vehicleConverter::convert));
    }

    @GetMapping("/search")
    public VehiclesDto searchVehicles(@RequestParam(name = "page", defaultValue = "1") Integer page,
                                      @RequestBody @Valid HashMap<String, String> searchCriteria) {
        Page<Vehicle> vehiclePage = vehicleService.searchVehicles(page, searchCriteria);
        return new VehiclesDto(vehiclePage.map(vehicleConverter::convert));
    }

    @PostMapping
    public ResponseEntity<String> addVehicle(@RequestBody @Valid VehicleDto vehicleDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder errorMessage = new StringBuilder();
            bindingResult.getAllErrors().forEach(e -> {
                errorMessage.append(e.getDefaultMessage()).append(" ");
            });
            return new ResponseEntity<>(errorMessage.toString(), HttpStatus.BAD_REQUEST);
        }
        vehicleService.addVehicle(vehicleDto);
        return new ResponseEntity<>("Vehicle added successfully", HttpStatus.CREATED);
    }

    @DeleteMapping
    public ResponseEntity<String> deleteVehicle(@RequestBody @Valid HashMap<String, String> searchCriteria) {
        boolean isDeleted = vehicleService.deleteVehicle(searchCriteria);
        if (isDeleted) return new ResponseEntity<>("Vehicle deleted successfully", HttpStatus.OK);
        return new ResponseEntity<>("Vehicle not found", HttpStatus.NOT_FOUND);
    }

}
