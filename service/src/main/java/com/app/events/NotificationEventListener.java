package com.app.events;

import com.app.model.Notification;
import com.app.repository.NotificationRepo;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class NotificationEventListener {

    private final ApplicationEventPublisher eventPublisher;
    private final NotificationRepo notificationRepo;

    public NotificationEventListener(ApplicationEventPublisher eventPublisher, NotificationRepo notificationRepo) {
        this.eventPublisher = eventPublisher;
        this.notificationRepo = notificationRepo;
    }

    @EventListener
    public void handleVehicleEvent(VehicleEvent event) {
        String message = switch (event.eventType()) {
            case CREATED -> "New vehicle added: " + event.vehicle().getModel();
            case UPDATED -> "Vehicle updated: " + event.vehicle().getModel();
            case DELETED -> "Vehicle removed: " + event.vehicle().getModel();
        };

        Notification notification = new Notification(
                null,
                message,
                false,
                Instant.now(),
                event.userId()
        );

        notificationRepo.save(notification);
        eventPublisher.publishEvent(new NotificationEvent(notification));
    }

    @EventListener
    public void handleRouteEvent(RouteEvent event) {
        String message = switch (event.eventType()) {
            case CREATED -> "New route from " + event.route().getWaypoints().stream().findFirst() +
                    " to " + event.route().getWaypoints().get(event.route().getWaypoints().size() - 1).getName();
            case UPDATED -> "Route updated: " + event.route().getWaypoints().stream().findFirst() +
                    " to " + event.route().getWaypoints().get(event.route().getWaypoints().size() - 1).getName();
            case DELETED -> "Route deleted: " + event.route().getWaypoints().stream().findFirst() +
                    " to " + event.route().getWaypoints().get(event.route().getWaypoints().size() - 1).getName();
        };

        Notification notification = new Notification(
                null,
                message,
                false,
                Instant.now(),
                event.userId()
        );

        notificationRepo.save(notification);
        eventPublisher.publishEvent(new NotificationEvent(notification));
    }
}
