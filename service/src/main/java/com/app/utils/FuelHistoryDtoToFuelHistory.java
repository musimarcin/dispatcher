package com.app.utils;

import com.app.dto.FuelHistoryDto;
import com.app.model.FuelHistory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class FuelHistoryDtoToFuelHistory  implements Converter<FuelHistoryDto, FuelHistory> {

    @Override
    public FuelHistory convert(FuelHistoryDto source) {
        return new FuelHistory(
                source.getId(),
                source.getFuelConsumed(),
                source.getCreatedAt(),
                source.getVehicleId(),
                source.getRouteId()
        );
    }
}
