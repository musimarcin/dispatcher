package com.app.service;

import com.app.converters.VehicleDtoToVehicle;
import com.app.converters.VehicleToVehicleDto;
import com.app.dto.VehicleDto;
import com.app.model.Role;
import com.app.model.UserEntity;
import com.app.model.Vehicle;
import com.app.repository.UserRepo;
import com.app.repository.VehicleRepo;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class VehicleServiceTests {

    @Mock
    private VehicleRepo vehicleRepo;
    @Mock
    private UserRepo userRepo;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private VehicleToVehicleDto vehicleConverter;
    @Mock
    private VehicleDtoToVehicle vehicleDtoConverter;

    @InjectMocks
    private VehicleService vehicleService;

    private VehicleDto vehicleDto;
    private Vehicle vehicle;
    private Page<Vehicle> vehicles;
    private Page<VehicleDto> vehiclesDto;
    private UserEntity userJohn;

    @BeforeEach
    void setUp() {
        vehicleDto = VehicleDto.builder().id(1L).licensePlate("1234").model("test_model")
                .manufacturer("test_manufacturer").productionYear(1999).fuelCapacity(new BigDecimal(2))
                .averageConsumption(new BigDecimal(3)).mileage(4).lastMaintenance(new Date())
                .createdAt(Instant.now()).userId(2L).build();
        vehicle = Vehicle.builder().id(1L).licensePlate("1234").model("test_model")
                .manufacturer("test_manufacturer").productionYear(1999).fuelCapacity(new BigDecimal(2))
                .averageConsumption(new BigDecimal(3)).mileage(4).lastMaintenance(new Date())
                .createdAt(Instant.now()).userId(2L).build();
        vehicles = new PageImpl<>(List.of(vehicle));
        vehiclesDto = new PageImpl<>(List.of(vehicleDto));
        userJohn = UserEntity.builder().id(2L).username("John").password("smith").email("john@smith").roles(new HashSet<>(Set.of(Role.ROLE_DISPATCHER))).build();
    }

    @Test
    void givenValidUser_whenGetAllVehicles_thenReturnPage() {
        given(userRepo.findByUsername(userJohn.getUsername())).willReturn(Optional.of(userJohn));
        given(vehicleRepo.findByUserId(userJohn.getId(), PageRequest.of(0, 10))).willReturn(vehicles);
        given(vehicleConverter.convert(vehicle)).willReturn(vehicleDto);

        Page<VehicleDto> result = vehicleService.getAllVehicles(userJohn.getUsername(), 1);

        assertEquals(vehiclesDto, result);
    }
}
