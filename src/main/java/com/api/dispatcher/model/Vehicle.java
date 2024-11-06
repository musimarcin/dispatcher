package com.api.dispatcher.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

@Entity
@Table(name = "vehicles")
@Data
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String licensePlate;
    private String model;
    private String manufacturer;

    @Column(precision = 5, scale = 2)
    private BigDecimal fuelCapacity;

    @Column(precision = 5, scale = 2)
    private BigDecimal averageConsumption;

    private Integer mileage;
    private Date lastMaintenance;
    private Instant createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

}
