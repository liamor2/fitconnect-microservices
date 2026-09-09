package com.fitconnect.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GatewayApplication {
    public static void main(String[] args) { SpringApplication.run(GatewayApplication.class, args); }

    @Bean
    RouteLocator fitConnectRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("classes", r -> r.path("/api/classes", "/api/classes/**").uri("lb://class-service"))
                .route("bookings", r -> r.path("/api/bookings", "/api/bookings/**").uri("lb://booking-service"))
                .route("payments", r -> r.path("/api/payments", "/api/payments/**").uri("lb://payment-service"))
                .route("notifications", r -> r.path("/api/notifications", "/api/notifications/**").uri("lb://notification-service"))
                .build();
    }
}
