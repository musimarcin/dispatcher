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
            bindingResult.getAllErrors().forEach(e ->
                    errorMessage.append(e.getDefaultMessage()).append(" "));
            return new ResponseEntity<>(errorMessage.toString(), HttpStatus.BAD_REQUEST);
        }
        vehicleService.addVehicle(vehicleDtoConverter.convert(vehicleDto));
        return new ResponseEntity<>("Vehicle added successfully", HttpStatus.CREATED);
    }

    @DeleteMapping
    public ResponseEntity<String> deleteVehicle(@RequestBody @Valid HashMap<String, String> searchCriteria) {
        boolean isDeleted = vehicleService.deleteVehicle(searchCriteria);
        if (isDeleted) return new ResponseEntity<>("Vehicle deleted successfully", HttpStatus.OK);
        return new ResponseEntity<>("Vehicle not found", HttpStatus.NOT_FOUND);
    }

}
