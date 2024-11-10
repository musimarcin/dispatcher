package com.api.dispatcher.repository;

import com.api.dispatcher.model.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;


@Repository
public interface VehicleRepo extends JpaRepository<Vehicle, Long>, JpaSpecificationExecutor<Vehicle> {

    Page<Vehicle> findByUserId(Long userId, Pageable pageable);

}
