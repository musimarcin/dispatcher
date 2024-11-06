package com.api.dispatcher.model;

import org.springframework.data.annotation.Id;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "fuel_predictions")
@Data
public class FuelPrediction {

    @Id
    private String id;

    @Indexed
    private Double predictedConsumption;

    private Double actualConsumption;
    private Instant createdAt;

    @Indexed
    private Long routeId;

    @Indexed
    private Long vehicleId;

}
