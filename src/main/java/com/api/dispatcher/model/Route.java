package com.api.dispatcher.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

@Entity
@Table(name = "routes")
@Data
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String startLocation;
    private String endLocation;

    @Column(precision = 6, scale = 2)
    private BigDecimal distance;

    private Integer estimatedTime;
    private Date startTime;
    private Date endTime;
    private String status;
    private Instant createdAt;

    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    private Long userId;

}
