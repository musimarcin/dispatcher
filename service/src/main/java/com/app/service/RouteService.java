package com.app.service;

import com.app.dto.RouteDto;
import com.app.events.EventType;
import com.app.events.RouteEvent;
import com.app.model.Route;
import com.app.model.RouteStatus;
import com.app.model.UserEntity;
import com.app.model.Vehicle;
import com.app.repository.RouteRepo;
import com.app.repository.UserRepo;
import com.app.repository.VehicleRepo;
import com.app.specifications.RouteSpecifications;
import com.app.utils.RouteDtoToRoute;
import com.app.utils.RouteMapper;
import com.app.utils.RouteToRouteDto;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class RouteService {

    private final RouteRepo routeRepo;
    private final UserRepo userRepo;
    private final VehicleRepo vehicleRepo;
    private final ApplicationEventPublisher eventPublisher;
    private final RouteToRouteDto routeConverter;
    private final RouteDtoToRoute routeDtoConverter;
    private final RouteSpecifications routeSpecifications;
    private final RouteMapper routeMapper;

    public RouteService(RouteRepo routeRepo, UserRepo userRepo, VehicleRepo vehicleRepo, ApplicationEventPublisher eventPublisher, RouteToRouteDto routeConverter, RouteDtoToRoute routeDtoConverter, RouteSpecifications routeSpecifications, RouteMapper routeMapper) {
        this.routeRepo = routeRepo;
        this.userRepo = userRepo;
        this.vehicleRepo = vehicleRepo;
        this.eventPublisher = eventPublisher;
        this.routeConverter = routeConverter;
        this.routeDtoConverter = routeDtoConverter;
        this.routeSpecifications = routeSpecifications;
        this.routeMapper = routeMapper;
    }

    private Pageable getPage(Integer page) {
        int pageNo = page < 1 ? 0 : page - 1;
        return PageRequest.of(pageNo, 10);
    }

    public Page<RouteDto> getAllRoutes(String username, Integer page) {
        Optional<UserEntity> user = userRepo.findByUsername(username);
        if (user.isEmpty()) return Page.empty();
        Page<Route> routePage = routeRepo.findByUserId(user.get().getId(), getPage(page));
        return routePage.map(routeConverter::convert);
    }

    public Page<RouteDto> getVehicleRoutes(String username, String licensePlate, Integer page) {
        Optional<UserEntity> user = userRepo.findByUsername(username);
        if (user.isEmpty()) return Page.empty();
        Optional<Vehicle> vehicle = vehicleRepo.findByLicensePlate(licensePlate);
        if (vehicle.isEmpty()) return Page.empty();
        Page<Route> routePage = routeRepo.findByVehicle(vehicle.get(), getPage(page));
        return routePage.map(routeConverter::convert);
    }

    public Page<RouteDto> searchRoute(String username, Integer page, Map<String, String> searchCriteria) {
        Optional<UserEntity> user = userRepo.findByUsername(username);
        if (user.isEmpty()) return Page.empty();
        Specification<Route> specification = routeSpecifications.build(searchCriteria, user.get().getId());
        Page<Route> routePage = routeRepo.findAll(specification, getPage(page));
        if (routePage.isEmpty()) return Page.empty();
        return routePage.map(routeConverter::convert);
    }

    @Transactional
    public RouteDto addRoute(String username, RouteDto routeDto) {
        Optional<UserEntity> user = userRepo.findByUsername(username);
        if (user.isEmpty()) return null;
        routeDto.setCreatedAt(Instant.now());
        routeDto.setStatus(RouteStatus.PLANNED);
        Route route = routeDtoConverter.convert(routeDto);
        routeRepo.save(route);
        RouteEvent routeEvent = new RouteEvent(
                EventType.CREATED,
                route,
                user.get().getId(),
                Instant.now()
        );
        eventPublisher.publishEvent(routeEvent);
        return routeConverter.convert(route);
    }

    @Transactional
    public boolean deleteRoute(String username, Long id) {
        Optional<UserEntity> user = userRepo.findByUsername(username);
        if (user.isEmpty()) return false;
        Optional<Route> route = routeRepo.findById(id);
        if (route.isEmpty()) return false;
        routeRepo.delete(route.get());
        RouteEvent routeEvent = new RouteEvent(
                EventType.DELETED,
                route.get(),
                user.get().getId(),
                Instant.now()
        );
        eventPublisher.publishEvent(routeEvent);
        return true;
    }

    @Transactional
    public boolean editRoute(String username, RouteDto routeDto) {
        Optional<UserEntity> user = userRepo.findByUsername(username);
        if (user.isEmpty()) return false;
        Optional<Route> route = routeRepo.findById(routeDto.getId());
        if (route.isEmpty()) return false;
        routeMapper.update(route.get(), routeDto);
        routeRepo.save(route.get());
        return true;
    }
}
