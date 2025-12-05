package com.app.service;

import com.app.dto.VehicleDto;
import com.app.dto.requests.VehicleUpdateRequest;
import com.app.events.EventType;
import com.app.events.VehicleEvent;
import com.app.model.UserEntity;
import com.app.model.Vehicle;
import com.app.repository.UserRepo;
import com.app.repository.VehicleRepo;
import com.app.specifications.VehicleSpecifications;
import com.app.utils.VehicleDtoToVehicle;
import com.app.utils.VehicleToVehicleDto;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class VehicleService {

    private final VehicleRepo vehicleRepo;
    private final UserRepo userRepo;
    private final ApplicationEventPublisher eventPublisher;
    private final VehicleToVehicleDto vehicleConverter;
    private final VehicleDtoToVehicle vehicleDtoConverter;
    private final VehicleSpecifications vehicleSpecifications;

    public VehicleService(VehicleRepo vehicleRepo, UserRepo userRepo, ApplicationEventPublisher eventPublisher, VehicleToVehicleDto vehicleConverter, VehicleDtoToVehicle vehicleDtoConverter, VehicleSpecifications vehicleSpecifications) {
        this.vehicleRepo = vehicleRepo;
        this.userRepo = userRepo;
        this.eventPublisher = eventPublisher;
        this.vehicleConverter = vehicleConverter;
        this.vehicleDtoConverter = vehicleDtoConverter;
        this.vehicleSpecifications = vehicleSpecifications;
    }

    private Pageable getPage(Integer page) {
        int pageNo = page < 1 ? 0 : page - 1;
        return PageRequest.of(pageNo, 10);
    }

    public Page<VehicleDto> getAllVehicles(String username, Integer page) {
        Optional<UserEntity> user = userRepo.findByUsername(username);
        if (user.isEmpty()) return Page.empty();
        List<Vehicle> vehicleList = vehicleRepo.findAll();
        Page<Vehicle> vehicles = new PageImpl<>(vehicleList);
        return vehicles.map(vehicleConverter::convert);
    }

    public Page<VehicleDto> getUsersVehicles(String username, Integer page) {
        Optional<UserEntity> user = userRepo.findByUsername(username);
        if (user.isEmpty()) return Page.empty();
        Page<Vehicle> vehiclePage = vehicleRepo.findByUserId(user.get().getId(), getPage(page));
        return vehiclePage.map(vehicleConverter::convert);
    }

    public VehicleDto getById(Long id) {
        Optional<Vehicle> vehicle = vehicleRepo.findById(id);
        return vehicle.map(vehicleConverter::convert).orElse(null);
    }

    public Page<VehicleDto> searchVehicles(String username, Integer page, Map<String, String> searchCriteria) {
        Optional<UserEntity> user = userRepo.findByUsername(username);
        if (user.isEmpty()) return Page.empty();
        Specification<Vehicle> specification = vehicleSpecifications.build(searchCriteria, user.get().getId());
        Page<Vehicle> vehiclePage = vehicleRepo.findAll(specification, getPage(page));
        return vehiclePage.map(vehicleConverter::convert);
    }

    @Transactional
    public VehicleDto addVehicle(String username, VehicleDto vehicleDto) {
        Optional<UserEntity> user = userRepo.findByUsername(username);
        if (user.isEmpty()) return null;
        vehicleDto.setUserId(user.get().getId());
        vehicleDto.setCreatedAt(Instant.now());
        vehicleDto.setRouteRecords(0);
        if (vehicleDto.getAverageConsumption() == null) vehicleDto.setAverageConsumption(BigDecimal.ZERO);
        Vehicle vehicle = vehicleDtoConverter.convert(vehicleDto);
        vehicleRepo.save(vehicle);
        VehicleEvent vehicleEvent = new VehicleEvent(
                EventType.CREATED,
                vehicle,
                user.get().getId(),
                Instant.now()
        );
        eventPublisher.publishEvent(vehicleEvent);
        return vehicleConverter.convert(vehicle);
    }

    @Transactional
    public boolean deleteVehicle(String username, String licensePlate) {
        Optional<UserEntity> user = userRepo.findByUsername(username);
        if (user.isEmpty()) return false;
        Optional<Vehicle> vehicle = vehicleRepo.findByLicensePlate(licensePlate);
        if (vehicle.isEmpty()) return false;
        vehicleRepo.delete(vehicle.get());
        VehicleEvent vehicleEvent = new VehicleEvent(
                EventType.DELETED,
                vehicle.get(),
                user.get().getId(),
                Instant.now()
        );
        eventPublisher.publishEvent(vehicleEvent);
        return true;
    }

    @Transactional
    public boolean editVehicleAfterRoute(String username, VehicleUpdateRequest request) {
        Optional<UserEntity> user = userRepo.findByUsername(username);
        if (user.isEmpty()) return false;
        Optional<Vehicle> optionalVehicle = vehicleRepo.findById(request.getId());
        if (optionalVehicle.isEmpty()) return false;
        Vehicle vehicle = optionalVehicle.get();
        double requestConsumption = request.getAverageConsumption().doubleValue() / (request.getMileage().doubleValue() / 100);
        double newAvg = (vehicle.getAverageConsumption().doubleValue() * vehicle.getRouteRecords() + requestConsumption)
                / (vehicle.getRouteRecords() + 1);
        vehicle.setRouteRecords(vehicle.getRouteRecords() + 1);
        vehicle.setAverageConsumption(new BigDecimal(newAvg));
        vehicle.setMileage(vehicle.getMileage() + request.getMileage());
        vehicleRepo.save(vehicle);
        return true;
    }

}