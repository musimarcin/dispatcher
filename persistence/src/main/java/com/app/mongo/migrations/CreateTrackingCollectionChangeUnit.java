package com.app.mongo.migrations;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.springframework.data.mongodb.core.MongoTemplate;

@ChangeUnit(id = "createTrackingCollection", order = "001", author = "admin")
public class CreateTrackingCollectionChangeUnit {

    private final MongoTemplate mongoTemplate;

    public CreateTrackingCollectionChangeUnit(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Execution
    public void migrate() {
        mongoTemplate.createCollection("tracking");
    }

    @RollbackExecution
    public void rollback() {
        mongoTemplate.dropCollection("tracking");
    }

}
