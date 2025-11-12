package com.app.service;

import com.app.dto.RouteDto;
import com.app.events.EventType;
import com.app.events.RouteEvent;
import com.app.model.Route;
import com.app.model.UserEntity;
import com.app.model.Vehicle;
import com.app.repository.RouteRepo;
import com.app.repository.UserRepo;
import com.app.repository.VehicleRepo;
import com.app.specifications.RouteSpecifications;
import com.app.converters.RouteDtoToRoute;
import com.app.converters.RouteToRouteDto;
import io.mongock.utils.StringUtils;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;

@Service
public class RouteService {

    private final RouteRepo routeRepo;
    private final UserRepo userRepo;
    private final VehicleRepo vehicleRepo;
    private final ApplicationEventPublisher eventPublisher;
    private final RouteToRouteDto routeConverter;
    private final RouteDtoToRoute routeDtoConverter;

    public RouteService(RouteRepo routeRepo, UserRepo userRepo, VehicleRepo vehicleRepo, ApplicationEventPublisher eventPublisher, RouteToRouteDto routeConverter, RouteDtoToRoute routeDtoConverter) {
        this.routeRepo = routeRepo;
        this.userRepo = userRepo;
        this.vehicleRepo = vehicleRepo;
        this.eventPublisher = eventPublisher;
        this.routeConverter = routeConverter;
        this.routeDtoConverter = routeDtoConverter;
    }

    private Pageable getPage(Integer page) {
        int pageNo = page < 1 ? 0 : page - 1;
        return PageRequest.of(pageNo, 10);
    }

    public Page<RouteDto> getAllRoutes(String username, Integer page) {
        if (userRepo.findByUsername(username).isEmpty()) return null;
        UserEntity user = userRepo.findByUsername(username).get();
        Page<Route> routePage = routeRepo.findByUserId(user.getId(), getPage(page));
        return routePage.map(routeConverter::convert);
    }

    public Page<RouteDto> getVehicleRoutes(String licensePlate, Integer page) {
        Vehicle vehicle;
        if (vehicleRepo.findByLicensePlate(licensePlate).isPresent()) {
            vehicle = vehicleRepo.findByLicensePlate(licensePlate).get();
        } else return null;
        Page<Route> routePage = routeRepo.findByVehicle(vehicle, getPage(page));
        return routePage.map(routeConverter::convert);
    }

    private Specification<Route> getSpecification(HashMap<String, String> searchCriteria) {
        Specification<Route> specification = Specification.where(null);

        if (StringUtils.hasText(searchCriteria.get("status")))
            specification = specification.and(RouteSpecifications.containsStatus(searchCriteria.get("status")));

        if (StringUtils.hasText(searchCriteria.get("licensePlate")))
            specification = specification.and(RouteSpecifications.containsLicensePlate(searchCriteria.get("licensePlate")));

        if (StringUtils.hasText(searchCriteria.get("distanceFrom"))) {
            BigDecimal distanceFrom = new BigDecimal(searchCriteria.get("distanceFrom"));
            specification = specification.and(RouteSpecifications.distanceGreaterThan(distanceFrom));
        } else specification = specification.and(RouteSpecifications.distanceGreaterThan(BigDecimal.ZERO));

        if (StringUtils.hasText(searchCriteria.get("distanceTo"))) {
            BigDecimal distanceTo = new BigDecimal(searchCriteria.get("distanceTo"));
            specification = specification.and(RouteSpecifications.distanceLessThan(distanceTo));
        } else specification = specification.and(RouteSpecifications.distanceLessThan(BigDecimal.valueOf(2000000)));

        if (StringUtils.hasText(searchCriteria.get("estimatedTimeFrom"))) {
            Integer estimatedTimeFrom = Integer.valueOf(searchCriteria.get("estimatedTimeFrom"));
            specification = specification.and(RouteSpecifications.estimatedTimeGreaterThan(estimatedTimeFrom));
        } else specification = specification.and(RouteSpecifications.estimatedTimeGreaterThan(0));

        if (StringUtils.hasText(searchCriteria.get("estimatedTimeTo"))) {
            Integer estimatedTimeTo = Integer.valueOf(searchCriteria.get("estimatedTimeTo"));
            specification = specification.and(RouteSpecifications.estimatedTimeLessThan(estimatedTimeTo));
        } else specification = specification.and(RouteSpecifications.estimatedTimeLessThan(Integer.MAX_VALUE));

        if (StringUtils.hasText(searchCriteria.get("startTimeFrom"))) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            LocalDateTime localDateTime = LocalDateTime.parse(searchCriteria.get("startTimeFrom"), formatter);
            Date startTimeFrom = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
            specification = specification.and(RouteSpecifications.startTimeGreaterThan(startTimeFrom));
        } else specification = specification.and(RouteSpecifications.startTimeGreaterThan(new Date(0)));

        if (StringUtils.hasText(searchCriteria.get("startTimeTo"))) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            LocalDateTime localDateTime = LocalDateTime.parse(searchCriteria.get("startTimeTo"), formatter);
            Date startTimeTo = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
            specification = specification.and(RouteSpecifications.startTimeLessThan(startTimeTo));
        } else specification = specification.and(RouteSpecifications.startTimeLessThan(Date.from(Instant.now())));

        if (StringUtils.hasText(searchCriteria.get("endTimeFrom"))) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            LocalDateTime localDateTime = LocalDateTime.parse(searchCriteria.get("startTimeFrom"), formatter);
            Date endTimeFrom = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
            specification = specification.and(RouteSpecifications.endTimeGreaterThan(endTimeFrom));
        } else specification = specification.and(RouteSpecifications.endTimeGreaterThan(new Date(0)));

        if (StringUtils.hasText(searchCriteria.get("endTimeTo"))) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            LocalDateTime localDateTime = LocalDateTime.parse(searchCriteria.get("startTimeTo"), formatter);
            Date endTimeTo = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
            specification = specification.and(RouteSpecifications.endTimeLessThan(endTimeTo));
        } else specification = specification.and(RouteSpecifications.endTimeLessThan(Date.from(Instant.now())));

        if (StringUtils.hasText(searchCriteria.get("createdAtFrom"))) {
            Instant createdAtFrom = Instant.parse(searchCriteria.get("createdAtFrom"));
            specification = specification.and(RouteSpecifications.createdAtGreaterThan(createdAtFrom));
        } else specification = specification.and(RouteSpecifications.createdAtGreaterThan(Instant.EPOCH));

        if (StringUtils.hasText(searchCriteria.get("createdAtTo"))) {
            Instant createdAtTo = Instant.parse(searchCriteria.get("createdAtTo"));
            specification = specification.and(RouteSpecifications.createdAtLessThan(createdAtTo));
        } else specification = specification.and(RouteSpecifications.createdAtLessThan(Instant.now()));

        return specification;
    }

    public Page<RouteDto> searchRoute(String username, Integer page, HashMap<String, String> searchCriteria) {
        if (userRepo.findByUsername(username).isEmpty()) return null;
        UserEntity user = userRepo.findByUsername(username).get();
        Specification<Route> specification = getSpecification(searchCriteria);
        specification = specification.and(RouteSpecifications.containsUserId(user.getId()));
        Page<Route> routePage = routeRepo.findAll(specification, getPage(page));
        return routePage.map(routeConverter::convert);
    }

    @Transactional
    public void addRoute(String username, RouteDto routeDto) {
        if (userRepo.findByUsername(username).isEmpty()) return;
        UserEntity user = userRepo.findByUsername(username).get();
        routeDto.setUserId(user.getId());
        routeDto.setCreatedAt(Instant.now());
        Route route = routeDtoConverter.convert(routeDto);
        routeRepo.save(route);
        RouteEvent routeEvent = new RouteEvent(
                EventType.CREATED,
                route,
                user.getId(),
                Instant.now()
        );
        eventPublisher.publishEvent(routeEvent);
    }

    @Transactional
    public boolean deleteRoute(Long id) {
        Optional<Route> route = routeRepo.findById(id);
        route.ifPresent(routeRepo::delete);
        return true;
    }
}
