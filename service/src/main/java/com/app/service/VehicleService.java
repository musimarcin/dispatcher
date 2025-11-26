package com.app.service;

import com.app.dto.VehicleDto;
import com.app.events.EventType;
import com.app.events.VehicleEvent;
import com.app.model.UserEntity;
import com.app.model.Vehicle;
import com.app.repository.UserRepo;
import com.app.repository.VehicleRepo;
import com.app.specifications.VehicleSpecifications;
import com.app.utils.VehicleDtoToVehicle;
import com.app.utils.VehicleMapper;
import com.app.utils.VehicleToVehicleDto;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
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
    private final VehicleMapper vehicleMapper;

    public VehicleService(VehicleRepo vehicleRepo, UserRepo userRepo, ApplicationEventPublisher eventPublisher, VehicleToVehicleDto vehicleConverter, VehicleDtoToVehicle vehicleDtoConverter, VehicleSpecifications vehicleSpecifications, VehicleMapper vehicleMapper) {
        this.vehicleRepo = vehicleRepo;
        this.userRepo = userRepo;
        this.eventPublisher = eventPublisher;
        this.vehicleConverter = vehicleConverter;
        this.vehicleDtoConverter = vehicleDtoConverter;
        this.vehicleSpecifications = vehicleSpecifications;
        this.vehicleMapper = vehicleMapper;
    }

    private Pageable getPage(Integer page) {
        int pageNo = page < 1 ? 0 : page - 1;
        return PageRequest.of(pageNo, 10);
    }

    public Page<VehicleDto> getAllVehicles(String username, Integer page) {
        Optional<UserEntity> user = userRepo.findByUsername(username);
        if (user.isEmpty()) return Page.empty();
        Page<Vehicle> vehiclePage = vehicleRepo.findByUserId(user.get().getId(), getPage(page));
        return vehiclePage.map(vehicleConverter::convert);
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
        Optional<Vehicle> vehicle = vehicleRepo.findByUserIdAndLicensePlate(user.get().getId(), licensePlate);
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
    public boolean editVehicle(String username, VehicleDto vehicleDto) {
        Optional<UserEntity> user = userRepo.findByUsername(username);
        if (user.isEmpty()) return false;
        Optional<Vehicle> vehicle = vehicleRepo.findById(vehicleDto.getId());
        if (vehicle.isEmpty()) return false;
        vehicleMapper.update(vehicle.get(), vehicleDto);
        vehicleRepo.save(vehicle.get());
        return true;
    }

}
