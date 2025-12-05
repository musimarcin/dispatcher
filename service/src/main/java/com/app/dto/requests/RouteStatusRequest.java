package com.app.dto.requests;

import com.app.model.RouteStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RouteStatusRequest {

    @NotNull(message = "Insert selected route id")
    private Long id;
    @NotNull(message = "Insert new route status")
    private RouteStatus status;

    public RouteStatusRequest(Long id, RouteStatus status) {
        this.id = id;
        this.status = status;
    }
}
