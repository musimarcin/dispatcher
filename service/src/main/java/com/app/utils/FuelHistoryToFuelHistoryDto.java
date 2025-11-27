package com.app.utils;

import com.app.dto.FuelHistoryDto;
import com.app.model.FuelHistory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class FuelHistoryToFuelHistoryDto implements Converter<FuelHistory, FuelHistoryDto> {

    @Override
    public FuelHistoryDto convert(FuelHistory source) {
        return new FuelHistoryDto(
                source.getId(),
                source.getFuelConsumed(),
                source.getCreatedAt(),
                source.getVehicleId(),
                source.getRouteId()
        );
    }
}
