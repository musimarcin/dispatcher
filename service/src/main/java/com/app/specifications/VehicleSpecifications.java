package com.app.specifications;

import com.app.model.Vehicle;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class VehicleSpecifications {

    private Specification<Vehicle> fuelCapacityGreaterThan(BigDecimal fuelCapacity) {
        return (root, query, builder) -> fuelCapacity == null ? null :
                builder.greaterThan(root.get("fuelCapacity"), fuelCapacity);
    }

    private Specification<Vehicle> fuelCapacityLessThan(BigDecimal fuelCapacity) {
        return (root, query, builder) -> fuelCapacity == null ? null :
                builder.lessThan(root.get("fuelCapacity"), fuelCapacity);
    }

    private Specification<Vehicle> averageConsumptionGreaterThan(BigDecimal averageConsumption) {
        return (root, query, builder) -> averageConsumption == null ? null :
                builder.greaterThan(root.get("fuelCapacity"), averageConsumption);
    }

    private Specification<Vehicle> averageConsumptionLessThan(BigDecimal averageConsumption) {
        return (root, query, builder) -> averageConsumption == null ? null :
                builder.lessThan(root.get("fuelCapacity"), averageConsumption);
    }

    private Specification<Vehicle> mileageGreaterThan(Integer mileage) {
        return (root, query, builder) -> mileage == null ? null :
                builder.greaterThan(root.get("mileage"), mileage);
    }

    private Specification<Vehicle> mileageLessThan(Integer mileage) {
        return (root, query, builder) -> mileage == null ? null :
                builder.lessThan(root.get("mileage"), mileage);
    }

    private Specification<Vehicle> productionYearGreaterThan(Integer productionYear) {
        return (root, query, builder) -> productionYear == null ? null :
                builder.greaterThan(root.get("productionYear"), productionYear);
    }

    private Specification<Vehicle> productionYearLessThan(Integer productionYear) {
        return (root, query, builder) -> productionYear == null ? null :
                builder.lessThan(root.get("productionYear"), productionYear);
    }

    private Specification<Vehicle> containsLicensePlate(String licensePlate) {
        return (root, query, builder) -> licensePlate == null || licensePlate.isEmpty() ? null :
                builder.like(builder.lower(root.get("licensePlate")), "%" + licensePlate.toLowerCase() + "%");
    }

    private Specification<Vehicle> containsModel(String model) {
        return (root, query, builder) -> model == null || model.isEmpty() ? null :
                builder.like(builder.lower(root.get("model")), "%" + model.toLowerCase() + "%");
    }

    private Specification<Vehicle> containsManufacturer(String manufacturer) {
        return (root, query, builder) -> manufacturer == null || manufacturer.isEmpty() ? null :
                builder.like(builder.lower(root.get("manufacturer")), "%" + manufacturer.toLowerCase() + "%");
    }

    private Specification<Vehicle> containsUserId(Long userId) {
        return (root, query, builder) -> userId == null ? null :
                builder.equal(root.get("userId"), userId);
    }


    public Specification<Vehicle> build(Map<String, String> searchCriteria, Long userId) {
        Specification<Vehicle> specification = Specification.where(null);

        if (StringUtils.hasLength(searchCriteria.get("licensePlate")))
            specification = specification.and(containsLicensePlate(searchCriteria.get("licensePlate")));

        if (StringUtils.hasLength(searchCriteria.get("model")))
            specification = specification.and(containsModel(searchCriteria.get("model")));

        if (StringUtils.hasLength(searchCriteria.get("manufacturer")))
            specification = specification.and(containsManufacturer(searchCriteria.get("manufacturer")));

        if (StringUtils.hasLength(searchCriteria.get("productionYearFrom"))) {
            Integer productionYearFrom = Integer.parseInt(searchCriteria.get("productionYearFrom"));
            specification = specification.and(productionYearGreaterThan(productionYearFrom));
        } else specification = specification.and(productionYearGreaterThan(1900));

        if (StringUtils.hasLength(searchCriteria.get("productionYearTo"))) {
            Integer productionYearTo = Integer.parseInt(searchCriteria.get("productionYearTo"));
            specification = specification.and(productionYearLessThan(productionYearTo));
        } else specification = specification.and(productionYearLessThan(2100));

        if (StringUtils.hasLength(searchCriteria.get("fuelCapacityFrom"))) {
            BigDecimal fuelCapacityFrom = new BigDecimal(searchCriteria.get("fuelCapacityFrom"));
            specification = specification.and(fuelCapacityGreaterThan(fuelCapacityFrom));
        } else specification = specification.and(fuelCapacityGreaterThan(BigDecimal.ZERO));

        if (StringUtils.hasLength(searchCriteria.get("fuelCapacityTo"))) {
            BigDecimal fuelCapacityTo = new BigDecimal(searchCriteria.get("fuelCapacityTo"));
            specification = specification.and(fuelCapacityLessThan(fuelCapacityTo));
        } else specification = specification.and(fuelCapacityLessThan(BigDecimal.valueOf(10000)));

        if (StringUtils.hasLength(searchCriteria.get("averageConsumptionFrom"))) {
            BigDecimal averageConsumptionFrom = new BigDecimal(searchCriteria.get("averageConsumptionFrom"));
            specification = specification.and(averageConsumptionGreaterThan(averageConsumptionFrom));
        } else specification = specification.and(averageConsumptionGreaterThan(BigDecimal.ZERO));

        if (StringUtils.hasLength(searchCriteria.get("averageConsumptionTo"))) {
            BigDecimal averageConsumptionTo = new BigDecimal(searchCriteria.get("averageConsumptionTo"));
            specification = specification.and(averageConsumptionLessThan(averageConsumptionTo));
        } else specification = specification.and(averageConsumptionLessThan(BigDecimal.valueOf(1000)));

        if (StringUtils.hasLength(searchCriteria.get("mileageFrom"))) {
            Integer mileageFrom = Integer.parseInt(searchCriteria.get("mileageFrom"));
            specification = specification.and(mileageGreaterThan(mileageFrom));
        } else specification = specification.and(mileageGreaterThan(0));

        if (StringUtils.hasLength(searchCriteria.get("mileageTo"))) {
            Integer mileageTo = Integer.parseInt(searchCriteria.get("mileageTo"));
            specification = specification.and(mileageLessThan(mileageTo));
        } else specification = specification.and(mileageLessThan(Integer.MAX_VALUE));

        specification = specification.and(containsUserId(userId));

        return specification;
    }
}
