package com.app.service;

import com.app.dto.VehicleDto;
import com.app.events.EventType;
import com.app.events.VehicleEvent;
import com.app.model.UserEntity;
import com.app.model.Vehicle;
import com.app.repository.UserRepo;
import com.app.repository.VehicleRepo;
import com.app.specifications.VehicleSpecifications;
import com.app.converters.VehicleDtoToVehicle;
import com.app.converters.VehicleToVehicleDto;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class VehicleService {

    private final VehicleRepo vehicleRepo;
    private final UserRepo userRepo;
    private final ApplicationEventPublisher eventPublisher;
    private final VehicleToVehicleDto vehicleConverter;
    private final VehicleDtoToVehicle vehicleDtoConverter;

    public VehicleService(VehicleRepo vehicleRepo, UserRepo userRepo, ApplicationEventPublisher eventPublisher, VehicleToVehicleDto vehicleConverter, VehicleDtoToVehicle vehicleDtoConverter) {
        this.vehicleRepo = vehicleRepo;
        this.userRepo = userRepo;
        this.eventPublisher = eventPublisher;
        this.vehicleConverter = vehicleConverter;
        this.vehicleDtoConverter = vehicleDtoConverter;
    }

    private Pageable getPage(Integer page) {
        int pageNo = page < 1 ? 0 : page - 1;
        return PageRequest.of(pageNo, 10);
    }

    public Page<VehicleDto> getAllVehicles(String username, Integer page) {
        if (userRepo.findByUsername(username).isEmpty()) return null;
        UserEntity user = userRepo.findByUsername(username).get();
        Page<Vehicle> vehiclePage = vehicleRepo.findByUserId(user.getId(), getPage(page));
        return vehiclePage.map(vehicleConverter::convert);
    }

    private Specification<Vehicle> getSpecification(Map<String, String> searchCriteria) {
        Specification<Vehicle> specification = Specification.where(null);

        if (StringUtils.hasLength(searchCriteria.get("licensePlate")))
            specification = specification.and(VehicleSpecifications.containsLicensePlate(searchCriteria.get("licensePlate")));

        if (StringUtils.hasLength(searchCriteria.get("model")))
            specification = specification.and(VehicleSpecifications.containsModel(searchCriteria.get("model")));

        if (StringUtils.hasLength(searchCriteria.get("manufacturer")))
            specification = specification.and(VehicleSpecifications.containsManufacturer(searchCriteria.get("manufacturer")));

        if (StringUtils.hasLength(searchCriteria.get("productionYearFrom"))) {
            Integer productionYearFrom = Integer.parseInt(searchCriteria.get("productionYearFrom"));
            specification = specification.and(VehicleSpecifications.productionYearGreaterThan(productionYearFrom));
        } else specification = specification.and(VehicleSpecifications.productionYearGreaterThan(1900));

        if (StringUtils.hasLength(searchCriteria.get("productionYearTo"))) {
            Integer productionYearTo = Integer.parseInt(searchCriteria.get("productionYearTo"));
            specification = specification.and(VehicleSpecifications.productionYearLessThan(productionYearTo));
        } else specification = specification.and(VehicleSpecifications.productionYearLessThan(2100));

        if (StringUtils.hasLength(searchCriteria.get("fuelCapacityFrom"))) {
            BigDecimal fuelCapacityFrom = new BigDecimal(searchCriteria.get("fuelCapacityFrom"));
            specification = specification.and(VehicleSpecifications.fuelCapacityGreaterThan(fuelCapacityFrom));
        } else specification = specification.and(VehicleSpecifications.fuelCapacityGreaterThan(BigDecimal.ZERO));

        if (StringUtils.hasLength(searchCriteria.get("fuelCapacityTo"))) {
            BigDecimal fuelCapacityTo = new BigDecimal(searchCriteria.get("fuelCapacityTo"));
            specification = specification.and(VehicleSpecifications.fuelCapacityLessThan(fuelCapacityTo));
        } else specification = specification.and(VehicleSpecifications.fuelCapacityLessThan(BigDecimal.valueOf(10000)));

        if (StringUtils.hasLength(searchCriteria.get("averageConsumptionFrom"))) {
            BigDecimal averageConsumptionFrom = new BigDecimal(searchCriteria.get("averageConsumptionFrom"));
            specification = specification.and(VehicleSpecifications.averageConsumptionGreaterThan(averageConsumptionFrom));
        } else specification = specification.and(VehicleSpecifications.averageConsumptionGreaterThan(BigDecimal.ZERO));

        if (StringUtils.hasLength(searchCriteria.get("averageConsumptionTo"))) {
            BigDecimal averageConsumptionTo = new BigDecimal(searchCriteria.get("averageConsumptionTo"));
            specification = specification.and(VehicleSpecifications.averageConsumptionLessThan(averageConsumptionTo));
        } else specification = specification.and(VehicleSpecifications.averageConsumptionLessThan(BigDecimal.valueOf(1000)));

        if (StringUtils.hasLength(searchCriteria.get("mileageFrom"))) {
            Integer mileageFrom = Integer.parseInt(searchCriteria.get("mileageFrom"));
            specification = specification.and(VehicleSpecifications.mileageGreaterThan(mileageFrom));
        } else specification = specification.and(VehicleSpecifications.mileageGreaterThan(0));

        if (StringUtils.hasLength(searchCriteria.get("mileageTo"))) {
            Integer mileageTo = Integer.parseInt(searchCriteria.get("mileageTo"));
            specification = specification.and(VehicleSpecifications.mileageLessThan(mileageTo));
        } else specification = specification.and(VehicleSpecifications.mileageLessThan(Integer.MAX_VALUE));

        return specification;
    }

    public Page<VehicleDto> searchVehicles(String username, Integer page, Map<String, String> searchCriteria) {
        Specification<Vehicle> specification = getSpecification(searchCriteria);
        if (userRepo.findByUsername(username).isEmpty()) return null;
        UserEntity user = userRepo.findByUsername(username).get();
        specification = specification.and(VehicleSpecifications.containsUserId(user.getId()));
        Page<Vehicle> vehiclePage = vehicleRepo.findAll(specification, getPage(page));
        return vehiclePage.map(vehicleConverter::convert);
    }

    @Transactional
    public VehicleDto addVehicle(String username, VehicleDto vehicleDto) {
        if (userRepo.findByUsername(username).isEmpty()) return null;
        UserEntity user = userRepo.findByUsername(username).get();
        vehicleDto.setUserId(user.getId());
        vehicleDto.setCreatedAt(Instant.now());
        Vehicle vehicle = vehicleDtoConverter.convert(vehicleDto);
        vehicleRepo.save(vehicle);
        VehicleEvent vehicleEvent = new VehicleEvent(
                EventType.CREATED,
                vehicle,
                user.getId(),
                Instant.now()
        );
        eventPublisher.publishEvent(vehicleEvent);
        return vehicleConverter.convert(vehicle);
    }

    @Transactional
    public boolean deleteVehicle(String username, String licensePlate) {
        if (userRepo.findByUsername(username).isEmpty()) return false;
        UserEntity user = userRepo.findByUsername(username).get();
        Optional<Vehicle> route = vehicleRepo.findByUserIdAndLicensePlate(user.getId(), licensePlate);
        route.ifPresent(vehicleRepo::delete);
        return true;
    }

}
