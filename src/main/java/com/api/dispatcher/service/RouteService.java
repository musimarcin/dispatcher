package com.api.dispatcher.service;

import com.api.dispatcher.model.Route;
import com.api.dispatcher.model.UserEntity;
import com.api.dispatcher.repository.RouteRepo;
import com.api.dispatcher.repository.UserRepo;
import com.api.dispatcher.security.SecurityUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class RouteService {

    private final UserRepo userRepo;
    private final RouteRepo routeRepo;

    public RouteService(UserRepo userRepo, RouteRepo routeRepo) {
        this.userRepo = userRepo;
        this.routeRepo = routeRepo;
    }

    public Pageable getPage(Integer page) {
        int pageNo = page < 1 ? 0 : page - 1;
        return PageRequest.of(pageNo, 10);
    }

    private UserEntity getUser() {
        String username = SecurityUtil.getSessionUser();
        return userRepo.findByUsername(username);
    }

    public Page<Route> getAllRoutes(Integer page) {
        Long userId = getUser().getId();
        return routeRepo.findByUserId(userId, getPage(page));
    }
}
