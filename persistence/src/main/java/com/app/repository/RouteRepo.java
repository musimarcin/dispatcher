package com.app.repository;

import com.app.model.Route;
import com.app.model.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RouteRepo extends JpaRepository<Route, Long>, JpaSpecificationExecutor<Route> {

    Page<Route> findByUserId(Long userId, Pageable pageable);
    Page<Route> findByVehicle(Vehicle vehicle, Pageable pageable);

}
