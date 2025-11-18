package com.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

@Entity
@Table(name = "vehicles")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String licensePlate;
    private String model;
    private String manufacturer;
    private Integer productionYear;

    @Column(precision = 5, scale = 2)
    private BigDecimal fuelCapacity;

    @Column(precision = 5, scale = 2)
    private BigDecimal averageConsumption;

    private Integer mileage;
    private Date lastMaintenance;
    private Instant createdAt;
    private Long userId;

}
