package com.app.events;

import com.app.model.Route;

import java.time.Instant;

public record RouteEvent(EventType eventType, Route route, Long userId, Instant eventTime) {}
