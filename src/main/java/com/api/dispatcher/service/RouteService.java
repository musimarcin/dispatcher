package com.api.dispatcher.service;

import com.api.dispatcher.dto.RouteDto;
import com.api.dispatcher.dto.RouteRequest;
import com.api.dispatcher.model.Route;
import com.api.dispatcher.model.UserEntity;
import com.api.dispatcher.model.Vehicle;
import com.api.dispatcher.repository.RouteRepo;
import com.api.dispatcher.repository.UserRepo;
import com.api.dispatcher.security.SecurityUtil;
import com.api.dispatcher.specifications.RouteSpecifications;
import com.api.dispatcher.specifications.RouteSpecifications;
import com.api.dispatcher.specifications.VehicleSpecifications;
import com.api.dispatcher.utils.converters.RouteToRouteDto;
import com.api.dispatcher.utils.converters.VehicleDtoToVehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class RouteService {

    private final UserRepo userRepo;
    private final RouteRepo routeRepo;
    private final RouteToRouteDto routeConverter;
    private final VehicleDtoToVehicle vehicleConverter;

    public RouteService(UserRepo userRepo, RouteRepo routeRepo, RouteToRouteDto routeConverter, VehicleDtoToVehicle vehicleConverter) {
        this.userRepo = userRepo;
        this.routeRepo = routeRepo;
        this.routeConverter = routeConverter;
        this.vehicleConverter = vehicleConverter;
    }

    public Pageable getPage(Integer page) {
        int pageNo = page < 1 ? 0 : page - 1;
        return PageRequest.of(pageNo, 10);
    }

    private UserEntity getUser() {
        String username = SecurityUtil.getSessionUser();
        return userRepo.findByUsername(username);
    }

    public Page<Route> getAllRoutes(Integer page) {
        Long userId = getUser().getId();
        return routeRepo.findByUserId(userId, getPage(page));
    }

    public Specification<Route> getSpecification(HashMap<String, String> searchCriteria) {
        if (searchCriteria == null || searchCriteria.isEmpty()) {
            return (root, query, builder) -> builder.disjunction(); // Return no results
        }

        Specification<Route> specification = Specification.where(null);

        if (StringUtils.hasLength(searchCriteria.get("startLocation")))
            specification = specification.and(RouteSpecifications.containsStartLocation(searchCriteria.get("startLocation")));

        if (StringUtils.hasLength(searchCriteria.get("endLocation")))
            specification = specification.and(RouteSpecifications.containsEndLocation(searchCriteria.get("endLocation")));

        if (StringUtils.hasLength(searchCriteria.get("distance"))) {
            BigDecimal distance = new BigDecimal(searchCriteria.get("distance"));
            specification = switch (searchCriteria.get("distanceOperator")) {
                case ">" -> specification.and(RouteSpecifications.distanceLessThan(distance));
                case "<" -> specification.and(RouteSpecifications.distanceGreaterThan(distance));
                case null, default -> specification.and(RouteSpecifications.distanceEqual(distance));
            };
        }

        if (StringUtils.hasLength(searchCriteria.get("estimatedTime"))) {
            Integer estimatedTime = Integer.valueOf(searchCriteria.get("estimatedTime"));
            specification = switch (searchCriteria.get("estimatedTimeOperator")) {
                case ">" -> specification.and(RouteSpecifications.estimatedTimeGreaterThan(estimatedTime));
                case "<" -> specification.and(RouteSpecifications.estimatedTimeLessThan(estimatedTime));
                case null, default -> specification.and(RouteSpecifications.estimatedTimeEqual(estimatedTime));
            };
        }

        if (StringUtils.hasLength(searchCriteria.get("startTime"))) {
            Date startTime = new Date(Long.parseLong(searchCriteria.get("startTime")));
            specification = specification.and(RouteSpecifications.startTimeAfter(startTime));
        }

        if (StringUtils.hasLength(searchCriteria.get("endTime"))) {
            Date endTime = new Date(Long.parseLong(searchCriteria.get("endTime")));
            specification = specification.and(RouteSpecifications.endTimeBefore(endTime));
        }

        if (StringUtils.hasLength(searchCriteria.get("status")))
            specification = specification.and(RouteSpecifications.containsStatus(searchCriteria.get("status")));

        if (StringUtils.hasLength(searchCriteria.get("createdAt"))) {
            Instant createdAt = Instant.parse(searchCriteria.get("createdAt"));
            specification = specification.and(RouteSpecifications.createdAfter(createdAt));
        }

        if (StringUtils.hasLength(searchCriteria.get("vehicleLicensePlate")))
            specification = specification.and(RouteSpecifications.containsVehicleLicensePlate(searchCriteria.get("vehicleLicensePlate")));

        return specification;
    }

    public Page<Route> searchRoute(Integer page, HashMap<String, String> searchCriteria) {
        Long userId = getUser().getId();
        Specification<Route> specification = getSpecification(searchCriteria);
        if (specification == null) {
            return Page.empty();
        }
        specification = specification.and(RouteSpecifications.containsUserId(userId));
        return routeRepo.findAll(specification, getPage(page));
    }

    public RouteDto addRoute(RouteDto routeDto) {
        Long userId = getUser().getId();
        Vehicle vehicle = vehicleConverter.convert(routeDto.getVehicleDto());
        Route route = new Route(null,
                routeDto.getStartLocation(),
                routeDto.getEndLocation(),
                routeDto.getDistance(),
                routeDto.getEstimatedTime(),
                routeDto.getStartTime(),
                routeDto.getEndTime(),
                routeDto.getStatus(),
                Instant.now(),
                vehicle,
                userId
        );
        Route savedRoute = routeRepo.save(route);
        return routeConverter.convert(savedRoute);
    }

    public boolean deleteRoute(HashMap<String, String> searchCriteria) {
        Specification<Route> specification = getSpecification(searchCriteria);
        if (specification == null) return false;
        Long userId = getUser().getId();
        specification.and(RouteSpecifications.containsUserId(userId));
        List<Route> routesToDelete = routeRepo.findAll(specification);
        if (routesToDelete.isEmpty()) return false;
        routeRepo.deleteAll(routesToDelete);
        return true;
    }
}
