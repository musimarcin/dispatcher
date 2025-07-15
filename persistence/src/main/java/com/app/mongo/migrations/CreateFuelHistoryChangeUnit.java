package com.app.mongo.migrations;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.springframework.data.mongodb.core.MongoTemplate;

@ChangeUnit(id = "createFuelHistoryCollection", order = "002", author = "admin")
public class CreateFuelHistoryChangeUnit {

    private final MongoTemplate mongoTemplate;

    public CreateFuelHistoryChangeUnit(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Execution
    public void migrate() {
        mongoTemplate.createCollection("fuel_history");
    }

    @RollbackExecution
    public void rollback() {
        mongoTemplate.dropCollection("fuel_history");
    }
}
