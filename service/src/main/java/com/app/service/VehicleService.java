package com.app.service;

import com.app.events.EventType;
import com.app.events.VehicleEvent;
import com.app.model.Route;
import com.app.model.Vehicle;
import com.app.repository.UserRepo;
import com.app.repository.VehicleRepo;
import com.app.security.SecurityUtil;
import com.app.specifications.VehicleSpecifications;
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
import java.util.List;
import java.util.Optional;

@Service
public class VehicleService {

    private final VehicleRepo vehicleRepo;
    private final UserRepo userRepo;
    private final ApplicationEventPublisher eventPublisher;

    public VehicleService(VehicleRepo vehicleRepo, UserRepo userRepo, ApplicationEventPublisher eventPublisher) {
        this.vehicleRepo = vehicleRepo;
        this.userRepo = userRepo;
        this.eventPublisher = eventPublisher;
    }

    private Pageable getPage(Integer page) {
        int pageNo = page < 1 ? 0 : page - 1;
        return PageRequest.of(pageNo, 10);
    }

    private Long getUser() {
        String username = SecurityUtil.getSessionUser();
        return userRepo.findByUsername(username).getId();
    }

    public Page<Vehicle> getAllVehicles(Integer page) {
        return vehicleRepo.findByUserId(getUser(), getPage(page));
    }

    private Specification<Vehicle> getSpecification(HashMap<String, String> searchCriteria) {
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

    public Page<Vehicle> searchVehicles(Integer page, HashMap<String, String> searchCriteria) {
        Specification<Vehicle> specification = getSpecification(searchCriteria);
        specification = specification.and(VehicleSpecifications.containsUserId(getUser()));
        return vehicleRepo.findAll(specification, getPage(page));
    }

    @Transactional
    public void addVehicle(Vehicle vehicle) {
        vehicle.setUserId(getUser());
        vehicle.setCreatedAt(Instant.now());
        vehicleRepo.save(vehicle);
        VehicleEvent vehicleEvent = new VehicleEvent(
                EventType.CREATED,
                vehicle,
                getUser(),
                Instant.now()
        );
        eventPublisher.publishEvent(vehicleEvent);
    }

    @Transactional
    public boolean deleteVehicle(String licensePlate) {
        Optional<Vehicle> route = vehicleRepo.findByLicensePlate(licensePlate);
        route.ifPresent(vehicleRepo::delete);
        return true;
    }

}
