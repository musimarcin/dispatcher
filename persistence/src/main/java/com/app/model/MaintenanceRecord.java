package com.app.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.Date;

@Entity
@Table(name = "maintenance_records")
@Data
public class MaintenanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Date maintenanceDate;
    private Integer mileage;
    private String description;
    private Instant createdAt;

    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

}
