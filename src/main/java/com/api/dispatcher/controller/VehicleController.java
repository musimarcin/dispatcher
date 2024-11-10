package com.api.dispatcher.controller;

import com.api.dispatcher.dto.VehiclesDto;
import com.api.dispatcher.model.Vehicle;
import com.api.dispatcher.service.VehicleService;
import com.api.dispatcher.utils.converters.VehicleToVehicleDto;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("/api/vehicles/")
public class VehicleController {

    private final VehicleService vehicleService;
    private final VehicleToVehicleDto vehicleConverter;

    public VehicleController(VehicleService vehicleService, VehicleToVehicleDto vehicleConverter) {
        this.vehicleService = vehicleService;
        this.vehicleConverter = vehicleConverter;
    }

    @GetMapping
    public VehiclesDto getAllVehicles(Integer page) {
        Page<Vehicle> vehiclePage = vehicleService.getAllVehicles(page);
        return new VehiclesDto(vehiclePage.map(vehicleConverter::convert));
    }

    @GetMapping("search")
    public VehiclesDto searchVehicles(Integer page, HashMap<String, String> searchCriteria) {
        Page<Vehicle> vehiclePage = vehicleService.searchVehicles(page, searchCriteria);
        return new VehiclesDto(vehiclePage.map(vehicleConverter::convert));
    }



}
