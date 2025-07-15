package com.app.mongo.migrations;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.springframework.data.mongodb.core.MongoTemplate;

@ChangeUnit(id = "createFuelPredictionsCollection", order = "003", author = "admin")
public class CreateFuelPredictionsChangeUnit {

    private final MongoTemplate mongoTemplate;

    public CreateFuelPredictionsChangeUnit(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Execution
    public void migrate() {
        mongoTemplate.createCollection("fuel_predictions");
    }

    @RollbackExecution
    public void rollback() {
        mongoTemplate.dropCollection("fuel_predictions");
    }
}
