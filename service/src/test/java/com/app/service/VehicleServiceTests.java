package com.app.service;

import com.app.utils.VehicleDtoToVehicle;
import com.app.utils.VehicleToVehicleDto;
import com.app.dto.VehicleDto;
import com.app.model.Role;
import com.app.model.UserEntity;
import com.app.model.Vehicle;
import com.app.repository.UserRepo;
import com.app.repository.VehicleRepo;
import com.app.specifications.VehicleSpecifications;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class VehicleServiceTests {

    @Mock
    private VehicleRepo vehicleRepo;
    @Mock
    private UserRepo userRepo;
    @Mock
    private VehicleToVehicleDto vehicleConverter;
    @Mock
    private VehicleDtoToVehicle vehicleDtoConverter;
    @Mock
    private VehicleSpecifications vehicleSpecifications;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private VehicleService vehicleService;

    private VehicleDto vehicleDto;
    private Vehicle vehicle;
    private Page<Vehicle> vehicles;
    private Page<VehicleDto> vehicleDtos;
    private UserEntity userJohn;
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
        vehicles = new PageImpl<>(List.of(vehicle));
        vehicleDtos = new PageImpl<>(List.of(vehicleDto));
        userJohn = UserEntity.builder().id(2L).username("John").password("smith").email("john@smith").roles(new HashSet<>(Set.of(Role.ROLE_DISPATCHER))).build();
        searchCriteria = new HashMap<>(Map.of("licensePlate", "1234"));
    }

    @Test
    void givenValidUser_whenGetAllVehicles_thenReturnPage() {
        given(userRepo.findByUsername(userJohn.getUsername())).willReturn(Optional.of(userJohn));
        given(vehicleRepo.findByUserId(userJohn.getId(), PageRequest.of(0, 10))).willReturn(vehicles);
        given(vehicleConverter.convert(vehicle)).willReturn(vehicleDto);

        Page<VehicleDto> result = vehicleService.getAllVehicles(userJohn.getUsername(), 1);

        assertEquals(vehicleDtos, result);
    }

    @Test
    void givenInValidUser_whenGetAllVehicles_thenReturnPage() {
        given(userRepo.findByUsername(userJohn.getUsername())).willReturn(Optional.empty());

        Page<VehicleDto> result = vehicleService.getAllVehicles(userJohn.getUsername(), 1);

        assertEquals(Page.empty(), result);
    }

    @Test
    void givenValidUser_whenSearchVehicles_thenReturnPage() {
        given(userRepo.findByUsername(userJohn.getUsername())).willReturn(Optional.of(userJohn));
        given(vehicleSpecifications.build(anyMap(), anyLong())).willReturn(mock(Specification.class));
        given(vehicleRepo.findAll(any(Specification.class), any(Pageable.class))).willReturn(vehicles);
        given(vehicleConverter.convert(any(Vehicle.class))).willReturn(vehicleDto);

        Page<VehicleDto> result = vehicleService.searchVehicles(userJohn.getUsername(), 1, searchCriteria);

        assertEquals(vehicleDtos.getContent().get(0).getLicensePlate(),
                result.getContent().get(0).getLicensePlate());
    }

    @Test
    void givenValidUserAndInvalidMap_whenSearchVehicles_thenReturnEmptyPage() {
        given(userRepo.findByUsername(userJohn.getUsername())).willReturn(Optional.of(userJohn));
        given(vehicleSpecifications.build(anyMap(), anyLong())).willReturn(mock(Specification.class));
        given(vehicleRepo.findAll(any(Specification.class), any(Pageable.class))).willReturn(Page.empty());

        Page<VehicleDto> result = vehicleService.searchVehicles(userJohn.getUsername(), 1, searchCriteria);

        assertEquals(Page.empty(), result);
    }

    @Test
    void givenInvalidUser_whenSearchVehicles_thenReturnEmptyPage() {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.empty());

        Page<VehicleDto> result = vehicleService.searchVehicles(userJohn.getUsername(), 1, searchCriteria);

        assertEquals(Page.empty(), result);
    }

    @Test
    void givenValidUser_whenAddVehicle_thenReturnVehicle() {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(userJohn));
        given(vehicleDtoConverter.convert(any(VehicleDto.class))).willReturn(vehicle);
        given(vehicleConverter.convert(any(Vehicle.class))).willReturn(vehicleDto);

        VehicleDto result = vehicleService.addVehicle(userJohn.getUsername(), vehicleDto);

        assertEquals(vehicleDto, result);
    }

    @Test
    void givenInvalidUser_whenAddVehicle_thenReturnNull() {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.empty());

        VehicleDto result = vehicleService.addVehicle(userJohn.getUsername(), vehicleDto);

        assertNull(result);
    }

    @Test
    void givenValidUser_whenDeleteVehicle_thenReturnTrue() {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(userJohn));
        given(vehicleRepo.findByLicensePlate(anyString())).willReturn(Optional.of(vehicle));

        boolean result = vehicleService.deleteVehicle(userJohn.getUsername(), vehicle.getLicensePlate());

        assertTrue(result);
    }

    @Test
    void givenInvalidUser_whenDeleteVehicle_thenReturnFalse() {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(userJohn));

        boolean result = vehicleService.deleteVehicle(userJohn.getUsername(), vehicle.getLicensePlate());

        assertFalse(result);
    }

    @Test
    void givenValidUserAndInvalidVehicle_whenDeleteVehicle_thenReturnFalse() {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(userJohn));
        given(vehicleRepo.findByLicensePlate(anyString())).willReturn(Optional.empty());

        boolean result = vehicleService.deleteVehicle(userJohn.getUsername(), vehicle.getLicensePlate());

        assertFalse(result);
    }




}
