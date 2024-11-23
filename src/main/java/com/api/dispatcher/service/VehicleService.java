package com.api.dispatcher.service;

import com.api.dispatcher.dto.VehicleDto;
import com.api.dispatcher.model.UserEntity;
import com.api.dispatcher.model.Vehicle;
import com.api.dispatcher.repository.UserRepo;
import com.api.dispatcher.repository.VehicleRepo;
import com.api.dispatcher.security.SecurityUtil;
import com.api.dispatcher.specifications.VehicleSpecifications;
import com.api.dispatcher.utils.converters.VehicleToVehicleDto;
import jakarta.persistence.EntityNotFoundException;
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
    private final VehicleToVehicleDto vehicleConverter;

    public VehicleService(VehicleRepo vehicleRepo, UserRepo userRepo, VehicleToVehicleDto vehicleToVehicleDto) {
        this.vehicleRepo = vehicleRepo;
        this.userRepo = userRepo;
        this.vehicleConverter = vehicleToVehicleDto;
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

    public Specification<Vehicle> getSpecification(HashMap<String, String> searchCriteria) {
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
        if (specification == null) System.out.println("SPEC IS NULL");
        return specification;
    }

    public Page<Vehicle> searchVehicles(Integer page, HashMap<String, String> searchCriteria) {
        Long userId = getUser().getId();
        Specification<Vehicle> specification = getSpecification(searchCriteria);
        System.out.println("SEARCH MAP " + searchCriteria);
        if (specification != null)
            specification = specification.and(VehicleSpecifications.containsUserId(userId));
        else
            return null;
        return vehicleRepo.findAll(specification, getPage(page));
    }

    @Transactional
    public VehicleDto addVehicle(VehicleDto vehicleDto) {
        Long userId = getUser().getId();
        Vehicle vehicle = new Vehicle(null,
                vehicleDto.getLicensePlate(),
                vehicleDto.getModel(),
                vehicleDto.getManufacturer(),
                vehicleDto.getFuelCapacity(),
                vehicleDto.getAverageConsumption(),
                vehicleDto.getMileage(),
                vehicleDto.getLastMaintenance(),
                Instant.now(),
                userId);
        Vehicle savedVehicle = vehicleRepo.save(vehicle);
        return vehicleConverter.convert(savedVehicle);
    }

    @Transactional
    public boolean deleteVehicle(HashMap<String, String> searchCriteria) {
        Specification<Vehicle> specification = getSpecification(searchCriteria);
        if (specification == null) return false;
        Long userId = getUser().getId();
        specification.and(VehicleSpecifications.containsUserId(userId));
        List<Vehicle> vehiclesToDelete = vehicleRepo.findAll(specification);
        if (vehiclesToDelete.isEmpty()) return false;
        vehicleRepo.deleteAll(vehiclesToDelete);
        return true;
    }

}
