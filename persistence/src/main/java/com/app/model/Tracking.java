package com.app.model;

import org.springframework.data.annotation.Id;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "tracking")
@Data
public class Tracking {

    @Id
    private String id;
    private Instant timestamp;
    private Double latitude;
    private Double longitude;

    @Indexed
    private Long routeId;
    @Indexed
    private Long vehicleId;
}
