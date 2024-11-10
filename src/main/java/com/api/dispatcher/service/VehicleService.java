package com.api.dispatcher.service;

import com.api.dispatcher.model.UserEntity;
import com.api.dispatcher.model.Vehicle;
import com.api.dispatcher.repository.UserRepo;
import com.api.dispatcher.repository.VehicleRepo;
import com.api.dispatcher.security.SecurityUtil;
import com.api.dispatcher.specifications.VehicleSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.HashMap;

@Service
public class VehicleService {

    private final VehicleRepo vehicleRepo;
    private final UserRepo userRepo;

    public VehicleService(VehicleRepo vehicleRepo, UserRepo userRepo) {
        this.vehicleRepo = vehicleRepo;
        this.userRepo = userRepo;
    }

    public Pageable getPage(Integer page) {
        int pageNo = page < 1 ? 0 : page - 1;
        return PageRequest.of(pageNo, 10);
    }

    private UserEntity getUser() {
        String username = SecurityUtil.getSessionUser();
        return userRepo.findByUsername(username);
    }

    public Page<Vehicle> getAllVehicles(Integer page) {
        Long userId = getUser().getId();
        return vehicleRepo.findByUserId(userId, getPage(page));
    }

    public Page<Vehicle> searchVehicles(Integer page, HashMap<String, String> searchCriteria) {
        Specification<Vehicle> specification = Specification.where(null);

        if (StringUtils.hasLength(searchCriteria.get("licensePlate"))) {
            specification = specification.and(VehicleSpecifications.containsLicensePlate(searchCriteria.get("licensePlate")));
        }

        if (StringUtils.hasLength(searchCriteria.get("model"))) {
            specification = specification.and(VehicleSpecifications.containsModel(searchCriteria.get("model")));
        }

        if (StringUtils.hasLength(searchCriteria.get("manufacturer"))) {
            specification = specification.and(VehicleSpecifications.containsManufacturer(searchCriteria.get("manufacturer")));
        }

        if (StringUtils.hasLength(searchCriteria.get("fuelCapacity"))) {
            BigDecimal fuelCapacity = new BigDecimal(searchCriteria.get("fuelCapacity"));
            specification = switch (searchCriteria.get("fuelCapacityOperator")) {
                case ">" -> specification.and(VehicleSpecifications.fuelCapacityGreaterThan(fuelCapacity));
                case "<" -> specification.and(VehicleSpecifications.fuelCapacityLessThan(fuelCapacity));
                default -> specification.and(VehicleSpecifications.fuelCapacityEqual(fuelCapacity));
            };
        }

        if (StringUtils.hasLength(searchCriteria.get("averageConsumption"))) {
            BigDecimal averageConsumption = new BigDecimal(searchCriteria.get("averageConsumption"));
            specification = switch (searchCriteria.get("averageConsumptionOperator")) {
                case ">" -> specification.and(VehicleSpecifications.averageConsumptionGreaterThan(averageConsumption));
                case "<" -> specification.and(VehicleSpecifications.averageConsumptionLessThan(averageConsumption));
                default -> specification.and(VehicleSpecifications.averageConsumptionEqual(averageConsumption));
            };
        }

        if (StringUtils.hasLength(searchCriteria.get("mileage"))) {
            Integer mileage = Integer.parseInt(searchCriteria.get("mileage"));
            specification = switch (searchCriteria.get("mileageOperator")) {
                case ">" -> specification.and(VehicleSpecifications.mileageGreaterThan(mileage));
                case "<" -> specification.and(VehicleSpecifications.mileageLessThan(mileage));
                default -> specification.and(VehicleSpecifications.mileageEqual(mileage));
            };
        }

        return vehicleRepo.findAll(specification, getPage(page));
    }


}
