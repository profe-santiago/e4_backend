package com.tickets.api_gateway.filter;

import com.tickets.api_gateway.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;

@Slf4j
@Component
public class CustomRateLimitGatewayFilterFactory extends AbstractGatewayFilterFactory<CustomRateLimitGatewayFilterFactory.Config> {

    private final JwtUtil jwtUtil;
    private final ReactiveStringRedisTemplate redisTemplate;

    @Value("${rate.limit.default.replenishRate:100}")
    private int defaultReplenishRate;

    @Value("${rate.limit.default.burstCapacity:150}")
    private int defaultBurstCapacity;

    @Value("${rate.limit.authenticated.replenishRate:200}")
    private int authenticatedReplenishRate;

    @Value("${rate.limit.authenticated.burstCapacity:300}")
    private int authenticatedBurstCapacity;

    @Value("${rate.limit.order.replenishRate:10}")
    private int orderReplenishRate;

    @Value("${rate.limit.order.burstCapacity:15}")
    private int orderBurstCapacity;

    @Autowired
    public CustomRateLimitGatewayFilterFactory(JwtUtil jwtUtil, ReactiveStringRedisTemplate redisTemplate) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String requestPath = exchange.getRequest().getURI().getPath();
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            // Determine the key for rate limiting
            String rateLimitKey;
            int replenishRate;
            int burstCapacity;

            // Extract user ID from JWT if available
            String userId = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    String token = authHeader.substring(7);
                    if (jwtUtil.validateToken(token)) {
                        userId = jwtUtil.extractUserId(token);
                    }
                } catch (Exception e) {
                    log.debug("Failed to extract user from token: {}", e.getMessage());
                }
            }

            // Determine rate limit based on endpoint and authentication
            if (requestPath.contains("/orders") && requestPath.endsWith("/orders")) {
                // Strict limit for order creation
                replenishRate = orderReplenishRate;
                burstCapacity = orderBurstCapacity;
                rateLimitKey = userId != null
                    ? "rate_limit:order:user:" + userId
                    : "rate_limit:order:ip:" + getClientIp(exchange);
            } else if (userId != null) {
                // Authenticated user - higher limits
                replenishRate = authenticatedReplenishRate;
                burstCapacity = authenticatedBurstCapacity;
                rateLimitKey = "rate_limit:user:" + userId;
            } else {
                // Anonymous user - IP-based limiting
                replenishRate = defaultReplenishRate;
                burstCapacity = defaultBurstCapacity;
                rateLimitKey = "rate_limit:ip:" + getClientIp(exchange);
            }

            // Check rate limit using token bucket algorithm
            return checkRateLimit(rateLimitKey, replenishRate, burstCapacity)
                .flatMap(allowed -> {
                    if (!allowed) {
                        log.warn("Rate limit exceeded for key: {}", rateLimitKey);
                        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                        exchange.getResponse().getHeaders().add("X-RateLimit-Retry-After-Seconds", "60");
                        return exchange.getResponse().setComplete();
                    }

                    // Add rate limit headers
                    return getRemainingRequests(rateLimitKey, burstCapacity)
                        .flatMap(remaining -> {
                            exchange.getResponse().getHeaders().add("X-RateLimit-Limit", String.valueOf(burstCapacity));
                            exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", String.valueOf(remaining));
                            exchange.getResponse().getHeaders().add("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() / 1000 + 60));
                            return chain.filter(exchange);
                        });
                });
        };
    }

    private String getClientIp(org.springframework.web.server.ServerWebExchange exchange) {
        // Try to get real IP from X-Forwarded-For header
        String forwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            return forwardedFor.split(",")[0].trim();
        }

        // Fallback to remote address
        return Objects.requireNonNull(
            exchange.getRequest().getRemoteAddress()
        ).getAddress().getHostAddress();
    }

    private Mono<Boolean> checkRateLimit(String key, int replenishRate, int burstCapacity) {
        String tokensKey = key + ":tokens";
        String timestampKey = key + ":timestamp";
        long now = System.currentTimeMillis();

        return redisTemplate.opsForValue().get(tokensKey)
            .defaultIfEmpty("0")
            .zipWith(redisTemplate.opsForValue().get(timestampKey).defaultIfEmpty(String.valueOf(now)))
            .flatMap(tuple -> {
                double tokens = Double.parseDouble(tuple.getT1());
                long lastRefill = Long.parseLong(tuple.getT2());

                // Calculate tokens to add based on time elapsed
                long timeElapsed = now - lastRefill;
                double tokensToAdd = (timeElapsed / 1000.0) * (replenishRate / 60.0); // per second rate

                tokens = Math.min(burstCapacity, tokens + tokensToAdd);

                if (tokens < 1) {
                    return Mono.just(false);
                }

                // Consume one token
                double finalTokens = tokens - 1;

                // Update Redis
                return redisTemplate.opsForValue().set(tokensKey, String.valueOf(finalTokens), Duration.ofMinutes(2))
                    .then(redisTemplate.opsForValue().set(timestampKey, String.valueOf(now), Duration.ofMinutes(2)))
                    .thenReturn(true);
            });
    }

    private Mono<Integer> getRemainingRequests(String key, int burstCapacity) {
        String tokensKey = key + ":tokens";
        return redisTemplate.opsForValue().get(tokensKey)
            .defaultIfEmpty(String.valueOf(burstCapacity))
            .map(tokens -> (int) Math.floor(Double.parseDouble(tokens)));
    }

    public static class Config {
        // Configuration properties if needed
    }
}
