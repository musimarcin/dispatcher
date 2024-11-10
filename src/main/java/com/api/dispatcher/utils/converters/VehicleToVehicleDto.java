package com.api.dispatcher.utils.converters;

import com.api.dispatcher.dto.VehicleDto;
import com.api.dispatcher.model.Vehicle;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class VehicleToVehicleDto implements Converter<Vehicle, VehicleDto> {

    private final UserToUserDto userToUserDto;

    public VehicleToVehicleDto(UserToUserDto userToUserDto) {
        this.userToUserDto = userToUserDto;
    }

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
                userToUserDto.convert(source.getUser())
        );
    }
}
