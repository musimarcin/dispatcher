package com.app.utils.converters;

import com.app.dto.RoutePointDto;
import com.app.model.RoutePoints;
import org.springframework.core.convert.converter.Converter;

public class RoutePointDtoToRoutePoint implements Converter<RoutePointDto, RoutePoints> {

    @Override
    public RoutePoints convert(RoutePointDto source) {
        return new RoutePoints(
                source.getId(),
                source.getName(),
                source.getLatitude(),
                source.getLongitude(),
                source.getSequence(),
                source.getRoute()
        );
    }
}
