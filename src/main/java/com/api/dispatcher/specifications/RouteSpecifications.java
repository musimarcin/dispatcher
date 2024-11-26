package com.api.dispatcher.specifications;

import com.api.dispatcher.model.Route;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

@Component
public class RouteSpecifications {

    public static Specification<Route> containsStartLocation(String startLocation) {
        return (root, query, builder) -> startLocation == null || startLocation.isEmpty() ? null :
                builder.like(builder.lower(root.get("startLocation")), "%" + startLocation.toLowerCase() + "%");
    }

    public static Specification<Route> containsEndLocation(String endLocation) {
        return (root, query, builder) -> endLocation == null || endLocation.isEmpty() ? null :
                builder.like(builder.lower(root.get("endLocation")), "%" + endLocation.toLowerCase() + "%");
    }

    public static Specification<Route> distanceEqual(BigDecimal distance) {
        return (root, query, builder) -> distance == null ? null :
                builder.equal(root.get("distance"), distance);
    }

    public static Specification<Route> distanceGreaterThan(BigDecimal distance) {
        return (root, query, builder) -> distance == null ? null :
                builder.greaterThan(root.get("distance"), distance);
    }

    public static Specification<Route> distanceLessThan(BigDecimal distance) {
        return (root, query, builder) -> distance == null ? null :
                builder.lessThan(root.get("distance"), distance);
    }

    public static Specification<Route> estimatedTimeEqual(Integer estimatedTime) {
        return (root, query, builder) -> estimatedTime == null ? null :
                builder.equal(root.get("estimatedTime"), estimatedTime);
    }

    public static Specification<Route> estimatedTimeGreaterThan(Integer estimatedTime) {
        return (root, query, builder) -> estimatedTime == null ? null :
                builder.greaterThan(root.get("estimatedTime"), estimatedTime);
    }

    public static Specification<Route> estimatedTimeLessThan(Integer estimatedTime) {
        return (root, query, builder) -> estimatedTime == null ? null :
                builder.lessThan(root.get("estimatedTime"), estimatedTime);
    }

    public static Specification<Route> startTimeAfter(Date startTime) {
        return (root, query, builder) -> startTime == null ? null :
                builder.greaterThan(root.get("startTime"), startTime);
    }

    public static Specification<Route> endTimeBefore(Date endTime) {
        return (root, query, builder) -> endTime == null ? null :
                builder.lessThan(root.get("endTime"), endTime);
    }

    public static Specification<Route> containsStatus(String status) {
        return (root, query, builder) -> status == null || status.isEmpty() ? null :
                builder.like(builder.lower(root.get("status")), "%" + status.toLowerCase() + "%");
    }

    public static Specification<Route> createdAfter(Instant createdAt) {
        return (root, query, builder) -> createdAt == null ? null :
                builder.greaterThan(root.get("createdAt"), createdAt);
    }

    public static Specification<Route> containsVehicleLicensePlate(String vehicleLicensePlate) {
        return (root, query, builder) -> vehicleLicensePlate == null || vehicleLicensePlate.isEmpty() ? null :
                builder.like(builder.lower(root.get("vehicleLicensePlate")), "%" + vehicleLicensePlate.toLowerCase() + "%");
    }

    public static Specification<Route> containsUserId(Long userId) {
        return (root, query, builder) -> userId == null ? null :
                builder.equal(root.get("userId"), userId);
    }
}
