package com.app.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "fuel_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FuelHistory {

    @Id
    private String id;
    private Double fuelConsumed;
    private Instant createdAt;

    @Indexed
    private Long vehicleId;
    @Indexed
    private Long routeId;

}
