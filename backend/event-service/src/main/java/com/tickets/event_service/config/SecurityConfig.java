package com.tickets.event_service.config;

import com.tickets.event_service.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.disable())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Swagger — público
                        .requestMatchers("/api/schema", "/api/docs/**", "/api/docs.yaml").permitAll()
                        // Consulta pública de eventos publicados
                        .requestMatchers(HttpMethod.GET, "/api/v1/events", "/api/v1/events/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/events/{eventId}/ticket-types", "/api/v1/events/{eventId}/ticket-types/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/categories", "/api/v1/categories/{id}").permitAll()
                        // Solo ADMIN puede crear/modificar/eliminar categorías
                        .requestMatchers(HttpMethod.POST, "/api/v1/categories").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/categories/**").hasRole("ADMIN")
                        // Solo ADMIN puede crear/modificar/eliminar eventos
                        .requestMatchers(HttpMethod.POST, "/api/v1/events").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/events/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/events/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/events/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/events/my").hasRole("ADMIN")
                        // Solo ADMIN puede gestionar tipos de ticket
                        .requestMatchers(HttpMethod.POST, "/api/v1/events/*/ticket-types").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/events/*/ticket-types/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/events/*/ticket-types/**").hasRole("ADMIN")
                        // Subir imágenes requiere autenticación
                        .requestMatchers(HttpMethod.POST, "/api/v1/upload/**").authenticated()
                        // El resto requiere autenticación
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
