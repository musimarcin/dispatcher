package com.app.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "routes")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Distance cannot be null")
    @Column(precision = 10, scale = 2)
    private BigDecimal distance;

    private Integer estimatedTime;
    private Date startTime;
    private Date endTime;
    private RouteStatus status;
    private Instant createdAt;

    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoutePoint> waypoints = new ArrayList<>();

    private Long userId;

}
