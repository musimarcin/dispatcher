package com.app.service;

import com.app.converters.RouteDtoToRoute;
import com.app.converters.RouteToRouteDto;
import com.app.dto.RouteDto;
import com.app.dto.VehicleDto;
import com.app.model.*;
import com.app.repository.RouteRepo;
import com.app.repository.UserRepo;
import com.app.repository.VehicleRepo;
import com.app.specifications.RouteSpecifications;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class RouteServiceTests {

    @InjectMocks
    private RouteService routeService;

    @Mock
    private UserRepo userRepo;
    @Mock
    private VehicleRepo vehicleRepo;
    @Mock
    private RouteRepo routeRepo;
    @Mock
    private RouteToRouteDto routeConverter;
    @Mock
    private RouteDtoToRoute routeDtoConverter;
    @Mock
    private RouteSpecifications routeSpecifications;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private UserEntity userJohn;
    private RouteDto routeDto;
    private Route route;
    private Page<Route> routes;
    private Page<RouteDto> routeDtos;
    private Vehicle vehicle;
    private VehicleDto vehicleDto;
    private HashMap<String, String> searchCriteria;

    @BeforeEach
    void setUp() {
        vehicle = Vehicle.builder().id(1L).licensePlate("1234").model("test_model")
                .manufacturer("test_manufacturer").productionYear(1999).fuelCapacity(new BigDecimal(2))
                .averageConsumption(new BigDecimal(3)).mileage(4).lastMaintenance(new Date())
                .createdAt(Instant.now()).userId(2L).build();
        vehicleDto = VehicleDto.builder().id(1L).licensePlate("1234").model("test_model")
                .manufacturer("test_manufacturer").productionYear(1999).fuelCapacity(new BigDecimal(2))
                .averageConsumption(new BigDecimal(3)).mileage(4).lastMaintenance(new Date())
                .createdAt(Instant.now()).userId(2L).build();
        route = Route.builder().id(1L).distance(new BigDecimal(20)).estimatedTime(30).startTime(new Date())
                .endTime(new Date()).status(RouteStatus.ACTIVE).createdAt(Instant.now()).vehicle(vehicle)
                .waypoints(List.of()).userId(2L).build();
        routeDto = RouteDto.builder().id(1L).distance(new BigDecimal(20)).estimatedTime(30).startTime(new Date())
                .endTime(new Date()).status(RouteStatus.ACTIVE).createdAt(Instant.now()).vehicleDto(vehicleDto)
                .waypoints(List.of()).userId(2L).build();
        routes = new PageImpl<>(List.of(route));
        routeDtos = new PageImpl<>(List.of(routeDto));
        userJohn = UserEntity.builder().id(2L).username("John").password("smith").email("john@smith").roles(new HashSet<>(Set.of(Role.ROLE_DISPATCHER))).build();
        searchCriteria = new HashMap<>(Map.of("status", "ACTIVE"));
    }

    @Test
    void givenValidUser_whenGetAllRoutes_thenReturnPage() {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(userJohn));
        given(routeRepo.findByUserId(anyLong(), any(Pageable.class))).willReturn(routes);
        given(routeConverter.convert(route)).willReturn(routeDto);

        Page<RouteDto> result = routeService.getAllRoutes(userJohn.getUsername(), 0);

        assertEquals(routeDtos.getContent().get(0).getId(), result.getContent().get(0).getId());
        assertEquals(routeDtos.getContent().get(0).getDistance(), result.getContent().get(0).getDistance());
        assertEquals(routeDtos.getContent().get(0).getStatus(), result.getContent().get(0).getStatus());
    }

    @Test
    void givenInvalidUser_whenGetAllRoutes_thenReturnEmptyPage() {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.empty());

        Page<RouteDto> result = routeService.getAllRoutes(userJohn.getUsername(), 0);

        assertEquals(Page.empty(), result);
    }

    @Test
    void givenValidUser_whenGetVehicleRoutes_thenReturnPage() {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(userJohn));
        given(vehicleRepo.findByUserIdAndLicensePlate(anyLong(), anyString())).willReturn(Optional.of(vehicle));
        given(routeRepo.findByVehicle(any(Vehicle.class), any(Pageable.class))).willReturn(routes);
        given(routeConverter.convert(route)).willReturn(routeDto);

        Page<RouteDto> result = routeService.getVehicleRoutes(userJohn.getUsername(), vehicle.getLicensePlate(), 0);

        assertEquals(routeDtos.getContent().get(0).getId(), result.getContent().get(0).getId());
        assertEquals(routeDtos.getContent().get(0).getDistance(), result.getContent().get(0).getDistance());
        assertEquals(routeDtos.getContent().get(0).getStatus(), result.getContent().get(0).getStatus());
    }

    @Test
    void givenInvalidUser_whenGetVehicleRoutes_thenReturnEmptyPage() {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.empty());

        Page<RouteDto> result = routeService.getVehicleRoutes(userJohn.getUsername(), vehicle.getLicensePlate(), 0);

        assertEquals(Page.empty(), result);
    }

    @Test
    void givenValidUserAndInvalidVehicle_whenGetAllRoutes_thenReturnEmptyPage() {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(userJohn));
        given(vehicleRepo.findByUserIdAndLicensePlate(anyLong(), anyString())).willReturn(Optional.empty());

        Page<RouteDto> result = routeService.getVehicleRoutes(userJohn.getUsername(), vehicle.getLicensePlate(), 0);

        assertEquals(Page.empty(), result);
    }

    @Test
    void givenValidUser_whenSearchRoute_thenReturnPage() {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(userJohn));
        given(routeSpecifications.build(anyMap(), anyLong())).willReturn(mock(Specification.class));
        given(routeRepo.findAll(any(Specification.class), any(Pageable.class))).willReturn(routes);
        given(routeConverter.convert(any(Route.class))).willReturn(routeDto);

        Page<RouteDto> result = routeService.searchRoute(userJohn.getUsername(), 0, searchCriteria);

        assertEquals(routeDtos.getContent().get(0).getId(), result.getContent().get(0).getId());
        assertEquals(routeDtos.getContent().get(0).getDistance(), result.getContent().get(0).getDistance());
        assertEquals(routeDtos.getContent().get(0).getStatus(), result.getContent().get(0).getStatus());
    }

    @Test
    void givenInvalidUser_whenSearchRoute_thenReturnEmptyPage() {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.empty());

        Page<RouteDto> result = routeService.searchRoute(userJohn.getUsername(), 0, searchCriteria);

        assertEquals(Page.empty(), result);
    }

    @Test
    void givenValidUserAndInvalidRoute_whenSearchRoute_thenReturnEmptyPage() {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(userJohn));
        given(routeSpecifications.build(anyMap(), anyLong())).willReturn(mock(Specification.class));
        given(routeRepo.findAll(any(Specification.class), any(Pageable.class))).willReturn(Page.empty());

        Page<RouteDto> result = routeService.searchRoute(userJohn.getUsername(), 0, searchCriteria);

        assertEquals(Page.empty(), result);
    }

    @Test
    void givenValidUser_whenAddRoute_thenReturnRouteDto() {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(userJohn));
        given(routeDtoConverter.convert(any(RouteDto.class))).willReturn(route);
        given(routeConverter.convert(any(Route.class))).willReturn(routeDto);

        RouteDto result = routeService.addRoute(userJohn.getUsername(), routeDto);

        assertEquals(routeDto, result);
    }

    @Test
    void givenInvalidUser_whenAddRoute_thenReturnNull() {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.empty());

        RouteDto result = routeService.addRoute(userJohn.getUsername(), routeDto);

        assertNull(result);
    }

    @Test
    void givenValidUser_whenDeleteRoute_thenReturnTrue() {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(userJohn));
        given(routeRepo.findById(anyLong())).willReturn(Optional.of(route));

        boolean result = routeService.deleteRoute(userJohn.getUsername(), 1L);

        assertTrue(result);
    }

    @Test
    void givenInvalidUser_whenDeleteRoute_thenReturnFalse() {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.empty());

        boolean result = routeService.deleteRoute(userJohn.getUsername(), 1L);

        assertFalse(result);
    }

    @Test
    void givenValidUserAndInvalidRoute_whenDeleteRoute_thenReturnFalse() {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(userJohn));
        given(routeRepo.findById(anyLong())).willReturn(Optional.empty());

        boolean result = routeService.deleteRoute(userJohn.getUsername(), 1L);

        assertFalse(result);
    }

}
