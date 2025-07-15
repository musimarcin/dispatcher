package com.app.utils.converters;

import com.app.dto.VehicleDto;
import com.app.model.Vehicle;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class VehicleToVehicleDto implements Converter<Vehicle, VehicleDto> {

    @Override
    public VehicleDto convert(Vehicle source) {
        return new VehicleDto(
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
