package com.app.utils.converters;

import com.app.dto.RoutePointDto;
import com.app.model.RoutePoints;
import org.springframework.core.convert.converter.Converter;

public class RoutePointToRoutePointDto implements Converter<RoutePoints, RoutePointDto> {

    @Override
    public RoutePointDto convert(RoutePoints source) {
        return new RoutePointDto(
                source.getId(),
                source.getName(),
                source.getLatitude(),
                source.getLongitude(),
                source.getSequence(),
                source.getRoute()
        );
    }
}
