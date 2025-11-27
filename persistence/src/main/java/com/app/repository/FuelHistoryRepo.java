package com.app.repository;

import com.app.model.FuelHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FuelHistoryRepo extends MongoRepository<FuelHistory, String> {

    Page<FuelHistory> findByVehicleId(Long id, Pageable page);
}
