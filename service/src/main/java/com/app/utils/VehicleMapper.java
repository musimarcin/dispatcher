package com.app.utils;

import com.app.dto.VehicleDto;
import com.app.model.Vehicle;
import org.springframework.stereotype.Component;

@Component
public class VehicleMapper {

    public void update(Vehicle vehicle, VehicleDto vehicleDto) {
        if (vehicleDto.getLicensePlate() != null) vehicle.setLicensePlate(vehicle.getLicensePlate());
        if (vehicleDto.getModel() != null) vehicle.setModel(vehicleDto.getModel());
        if (vehicleDto.getManufacturer() != null) vehicle.setManufacturer(vehicle.getManufacturer());
        if (vehicleDto.getProductionYear() != null) vehicle.setProductionYear(vehicle.getProductionYear());
        if (vehicleDto.getFuelCapacity() != null) vehicle.setFuelCapacity(vehicleDto.getFuelCapacity());
        if (vehicleDto.getAverageConsumption() != null) vehicle.setAverageConsumption(vehicleDto.getAverageConsumption());
        if (vehicleDto.getMileage() != null) vehicle.setMileage(vehicleDto.getMileage());
        if (vehicleDto.getLastMaintenance() != null) vehicle.setLastMaintenance(vehicleDto.getLastMaintenance());
    }

}
