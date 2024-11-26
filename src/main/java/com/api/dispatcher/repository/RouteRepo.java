package com.api.dispatcher.repository;

import com.api.dispatcher.model.Route;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteRepo extends JpaRepository<Route, Long>, JpaSpecificationExecutor<Route> {

    Page<Route> findByUserId(Long userId, Pageable pageable);
}
