package com.tickets.api_gateway.config;

import com.tickets.api_gateway.filter.CustomRateLimitGatewayFilterFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;

@Configuration
public class GatewayConfig {

    private final CustomRateLimitGatewayFilterFactory rateLimitFilter;

    public GatewayConfig(CustomRateLimitGatewayFilterFactory rateLimitFilter) {
        this.rateLimitFilter = rateLimitFilter;
    }

    @Bean
    public ReactiveStringRedisTemplate reactiveStringRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory) {
        return new ReactiveStringRedisTemplate(connectionFactory);
    }

    /**
     * Programmatic route configuration with custom filters
     * This adds the custom rate limiter to all routes
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // Auth service - no rate limiting on login/register (handled by application.properties routes)
            .route("auth-service-public", r -> r
                .path("/api/v1/auth/**")
                .filters(f -> f
                    .filter(rateLimitFilter.apply(new CustomRateLimitGatewayFilterFactory.Config()))
                )
                .uri("http://${AUTH_SERVICE_HOST:localhost}:${AUTH_SERVICE_PORT:8090}")
            )
            // Other routes will use application.properties configuration
            .build();
    }
}
