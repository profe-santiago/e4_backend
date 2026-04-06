package com.tickets.payment_service.config;

import com.stripe.StripeClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provee un StripeClient como bean de Spring.
 *
 * Se usa StripeClient (API inyectable) en lugar de mutar el campo estático
 * Stripe.apiKey — esto permite testear el gateway sin efectos globales y
 * facilita soporte multi-tenant en el futuro.
 */
@Configuration
public class StripeConfig {

    @Bean
    public StripeClient stripeClient(@Value("${stripe.secret-key}") String secretKey) {
        return new StripeClient(secretKey);
    }
}
