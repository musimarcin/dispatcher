package com.app.service;

import com.app.events.EventType;
import com.app.events.VehicleEvent;
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

        if (StringUtils.hasLength(searchCriteria.get("fuelCapacity"))) {
            BigDecimal fuelCapacity = new BigDecimal(searchCriteria.get("fuelCapacity"));
            specification = switch (searchCriteria.get("fuelCapacityOperator")) {
                case ">" -> specification.and(VehicleSpecifications.fuelCapacityGreaterThan(fuelCapacity));
                case "<" -> specification.and(VehicleSpecifications.fuelCapacityLessThan(fuelCapacity));
                case null, default -> specification.and(VehicleSpecifications.fuelCapacityEqual(fuelCapacity));
            };
        }

        if (StringUtils.hasLength(searchCriteria.get("averageConsumption"))) {
            BigDecimal averageConsumption = new BigDecimal(searchCriteria.get("averageConsumption"));
            specification = switch (searchCriteria.get("averageConsumptionOperator")) {
                case ">" -> specification.and(VehicleSpecifications.averageConsumptionGreaterThan(averageConsumption));
                case "<" -> specification.and(VehicleSpecifications.averageConsumptionLessThan(averageConsumption));
                case null, default -> specification.and(VehicleSpecifications.averageConsumptionEqual(averageConsumption));
            };
        }

        if (StringUtils.hasLength(searchCriteria.get("mileage"))) {
            Integer mileage = Integer.parseInt(searchCriteria.get("mileage"));
            specification = switch (searchCriteria.get("mileageOperator")) {
                case ">" -> specification.and(VehicleSpecifications.mileageGreaterThan(mileage));
                case "<" -> specification.and(VehicleSpecifications.mileageLessThan(mileage));
                case null, default -> specification.and(VehicleSpecifications.mileageEqual(mileage));
            };
        }
        return specification;
    }

    public Page<Vehicle> searchVehicles(Integer page, HashMap<String, String> searchCriteria) {
        Specification<Vehicle> specification = getSpecification(searchCriteria);
        System.out.println("SEARCH MAP " + searchCriteria);
        if (specification != null)
            specification = specification.and(VehicleSpecifications.containsUserId(getUser()));
        else
            return null;
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
    public boolean deleteVehicle(HashMap<String, String> searchCriteria) {
        Specification<Vehicle> specification = getSpecification(searchCriteria);
        if (specification == null) return false;
        specification.and(VehicleSpecifications.containsUserId(getUser()));
        List<Vehicle> vehiclesToDelete = vehicleRepo.findAll(specification);
        if (vehiclesToDelete.isEmpty()) return false;
        vehicleRepo.deleteAll(vehiclesToDelete);
        return true;
    }

}
