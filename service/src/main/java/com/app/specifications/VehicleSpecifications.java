package com.app.specifications;

import com.app.model.Vehicle;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class VehicleSpecifications {

    public static Specification<Vehicle> fuelCapacityGreaterThan(BigDecimal fuelCapacity) {
        return (root, query, builder) -> fuelCapacity == null ? null :
                builder.greaterThan(root.get("fuelCapacity"), fuelCapacity);
    }

    public static Specification<Vehicle> fuelCapacityLessThan(BigDecimal fuelCapacity) {
        return (root, query, builder) -> fuelCapacity == null ? null :
                builder.lessThan(root.get("fuelCapacity"), fuelCapacity);
    }

    public static Specification<Vehicle> averageConsumptionGreaterThan(BigDecimal averageConsumption) {
        return (root, query, builder) -> averageConsumption == null ? null :
                builder.greaterThan(root.get("fuelCapacity"), averageConsumption);
    }

    public static Specification<Vehicle> averageConsumptionLessThan(BigDecimal averageConsumption) {
        return (root, query, builder) -> averageConsumption == null ? null :
                builder.lessThan(root.get("fuelCapacity"), averageConsumption);
    }

    public static Specification<Vehicle> mileageGreaterThan(Integer mileage) {
        return (root, query, builder) -> mileage == null ? null :
                builder.greaterThan(root.get("mileage"), mileage);
    }

    public static Specification<Vehicle> mileageLessThan(Integer mileage) {
        return (root, query, builder) -> mileage == null ? null :
                builder.lessThan(root.get("mileage"), mileage);
    }

    public static Specification<Vehicle> productionYearGreaterThan(Integer productionYear) {
        return (root, query, builder) -> productionYear == null ? null :
                builder.greaterThan(root.get("productionYear"), productionYear);
    }

    public static Specification<Vehicle> productionYearLessThan(Integer productionYear) {
        return (root, query, builder) -> productionYear == null ? null :
                builder.lessThan(root.get("productionYear"), productionYear);
    }

    public static Specification<Vehicle> containsLicensePlate(String licensePlate) {
        return (root, query, builder) -> licensePlate == null || licensePlate.isEmpty() ? null :
                builder.like(builder.lower(root.get("licensePlate")), "%" + licensePlate.toLowerCase() + "%");
    }

    public static Specification<Vehicle> containsModel(String model) {
        return (root, query, builder) -> model == null || model.isEmpty() ? null :
                builder.like(builder.lower(root.get("model")), "%" + model.toLowerCase() + "%");
    }

    public static Specification<Vehicle> containsManufacturer(String manufacturer) {
        return (root, query, builder) -> manufacturer == null || manufacturer.isEmpty() ? null :
                builder.like(builder.lower(root.get("manufacturer")), "%" + manufacturer.toLowerCase() + "%");
    }

    public static Specification<Vehicle> containsUserId(Long userId) {
        return (root, query, builder) -> userId == null ? null :
                builder.equal(root.get("userId"), userId);
    }
}
