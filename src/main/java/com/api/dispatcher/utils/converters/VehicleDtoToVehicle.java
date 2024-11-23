package com.api.dispatcher.utils.converters;

import com.api.dispatcher.dto.VehicleDto;
import com.api.dispatcher.model.Vehicle;
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
                source.getFuelCapacity(),
                source.getAverageConsumption(),
                source.getMileage(),
                source.getLastMaintenance(),
                source.getCreatedAt(),
                source.getUserId()
        );
    }
}
