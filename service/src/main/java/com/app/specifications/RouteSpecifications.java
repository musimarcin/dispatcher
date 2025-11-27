package com.app.specifications;

import com.app.model.Route;
import io.mongock.utils.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

@Component
public class RouteSpecifications {

    private Specification<Route> containsStatus(String status) {
        return (root, query, builder) -> status == null || status.isEmpty() ? null :
                builder.like(builder.lower(root.get("status")), "%" + status.toLowerCase() + "%");
    }

    private Specification<Route> containsLicensePlate(String licensePlate) {
        return (root, query, builder) -> licensePlate == null || licensePlate.isEmpty() ? null :
                builder.like(builder.lower(root.get("licensePlate")), "%" + licensePlate.toLowerCase() + "%");
    }

    private Specification<Route> distanceLessThan(BigDecimal distance) {
        return (root, query, builder) -> distance == null ? null :
                builder.lessThan(root.get("distance"), distance);
    }

    private Specification<Route> distanceGreaterThan(BigDecimal distance) {
        return (root, query, builder) -> distance == null ? null :
                builder.greaterThan(root.get("distance"), distance);
    }

    private Specification<Route> estimatedTimeGreaterThan(Integer estimatedTime) {
        return (root, query, builder) -> estimatedTime == null ? null :
                builder.greaterThan(root.get("estimatedTime"), estimatedTime);
    }

    private Specification<Route> estimatedTimeLessThan(Integer estimatedTime) {
        return (root, query, builder) -> estimatedTime == null ? null :
                builder.lessThan(root.get("estimatedTime"), estimatedTime);
    }

    private Specification<Route> startTimeGreaterThan(Date startTime) {
        return (root, query, builder) -> startTime == null ? null :
                builder.greaterThan(root.get("startTime"), startTime);
    }

    private Specification<Route> startTimeLessThan(Date startTime) {
        return (root, query, builder) -> startTime == null ? null :
                builder.lessThan(root.get("startTime"), startTime);
    }

    private Specification<Route> endTimeGreaterThan(Date endTime) {
        return (root, query, builder) -> endTime == null ? null :
                builder.greaterThan(root.get("endTime"), endTime);
    }

    private Specification<Route> endTimeLessThan(Date endTime) {
        return (root, query, builder) -> endTime == null ? null :
                builder.lessThan(root.get("endTime"), endTime);
    }

    private Specification<Route> createdAtGreaterThan(Instant createdAt) {
        return (root, query, builder) -> createdAt == null ? null :
                builder.greaterThan(root.get("createdAt"), createdAt);
    }

    private Specification<Route> createdAtLessThan(Instant createdAt) {
        return (root, query, builder) -> createdAt == null ? null :
                builder.lessThan(root.get("createdAt"), createdAt);
    }

    private Specification<Route> containsUserId(Long userId) {
        return (root, query, builder) -> userId == null ? null :
                builder.equal(root.get("userId"), userId);
    }


    public Specification<Route> build(Map<String, String> searchCriteria, Long userId) {
        Specification<Route> specification = Specification.where(null);

        if (StringUtils.hasText(searchCriteria.get("status")))
            specification = specification.and(containsStatus(searchCriteria.get("status")));

        if (StringUtils.hasText(searchCriteria.get("licensePlate")))
            specification = specification.and(containsLicensePlate(searchCriteria.get("licensePlate")));

        if (StringUtils.hasText(searchCriteria.get("distanceFrom"))) {
            BigDecimal distanceFrom = new BigDecimal(searchCriteria.get("distanceFrom"));
            specification = specification.and(distanceGreaterThan(distanceFrom));
        } else specification = specification.and(distanceGreaterThan(BigDecimal.ZERO));

        if (StringUtils.hasText(searchCriteria.get("distanceTo"))) {
            BigDecimal distanceTo = new BigDecimal(searchCriteria.get("distanceTo"));
            specification = specification.and(distanceLessThan(distanceTo));
        } else specification = specification.and(distanceLessThan(BigDecimal.valueOf(2000000)));

        if (StringUtils.hasText(searchCriteria.get("estimatedTimeFrom"))) {
            Integer estimatedTimeFrom = Integer.valueOf(searchCriteria.get("estimatedTimeFrom"));
            specification = specification.and(estimatedTimeGreaterThan(estimatedTimeFrom));
        } else specification = specification.and(estimatedTimeGreaterThan(0));

        if (StringUtils.hasText(searchCriteria.get("estimatedTimeTo"))) {
            Integer estimatedTimeTo = Integer.valueOf(searchCriteria.get("estimatedTimeTo"));
            specification = specification.and(estimatedTimeLessThan(estimatedTimeTo));
        } else specification = specification.and(estimatedTimeLessThan(Integer.MAX_VALUE));

        if (StringUtils.hasText(searchCriteria.get("startTimeFrom"))) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            LocalDateTime localDateTime = LocalDateTime.parse(searchCriteria.get("startTimeFrom"), formatter);
            Date startTimeFrom = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
            specification = specification.and(startTimeGreaterThan(startTimeFrom));
        } else specification = specification.and(startTimeGreaterThan(new Date(0)));

        if (StringUtils.hasText(searchCriteria.get("startTimeTo"))) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            LocalDateTime localDateTime = LocalDateTime.parse(searchCriteria.get("startTimeTo"), formatter);
            Date startTimeTo = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
            specification = specification.and(startTimeLessThan(startTimeTo));
        } else specification = specification.and(startTimeLessThan(Date.from(Instant.now())));

        if (StringUtils.hasText(searchCriteria.get("endTimeFrom"))) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            LocalDateTime localDateTime = LocalDateTime.parse(searchCriteria.get("startTimeFrom"), formatter);
            Date endTimeFrom = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
            specification = specification.and(endTimeGreaterThan(endTimeFrom));
        } else specification = specification.and(endTimeGreaterThan(new Date(0)));

        if (StringUtils.hasText(searchCriteria.get("endTimeTo"))) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            LocalDateTime localDateTime = LocalDateTime.parse(searchCriteria.get("startTimeTo"), formatter);
            Date endTimeTo = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
            specification = specification.and(endTimeLessThan(endTimeTo));
        } else specification = specification.and(endTimeLessThan(Date.from(Instant.now())));

        if (StringUtils.hasText(searchCriteria.get("createdAtFrom"))) {
            Instant createdAtFrom = Instant.parse(searchCriteria.get("createdAtFrom"));
            specification = specification.and(createdAtGreaterThan(createdAtFrom));
        } else specification = specification.and(createdAtGreaterThan(Instant.EPOCH));

        if (StringUtils.hasText(searchCriteria.get("createdAtTo"))) {
            Instant createdAtTo = Instant.parse(searchCriteria.get("createdAtTo"));
            specification = specification.and(createdAtLessThan(createdAtTo));
        } else specification = specification.and(createdAtLessThan(Instant.now()));

        specification = specification.and(containsUserId(userId));

        return specification;
    }
}
