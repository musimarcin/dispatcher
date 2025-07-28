package com.app.events;

import com.app.model.Vehicle;

import java.time.Instant;

public record VehicleEvent(EventType eventType, Vehicle vehicle, Long userId, Instant eventTime) {}
