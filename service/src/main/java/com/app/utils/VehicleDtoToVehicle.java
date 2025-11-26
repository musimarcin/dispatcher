package com.app.utils;

import com.app.dto.VehicleDto;
import com.app.model.Vehicle;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class VehicleDtoToVehicle implements Converter<VehicleDto, Vehicle> {

    @Override
    public Vehicle convert(VehicleDto source) {
        return new Vehicle(
                source.getId(),
                source.getLicensePlate(),
                source.getModel(),
                source.getManufacturer(),
                source.getProductionYear(),
                source.getFuelCapacity(),
                source.getAverageConsumption(),
                source.getMileage(),
                source.getLastMaintenance(),
                source.getCreatedAt(),
                source.getUserId()
        );
    }
}
