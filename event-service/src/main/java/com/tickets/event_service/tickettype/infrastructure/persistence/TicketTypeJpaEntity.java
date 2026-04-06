package com.tickets.event_service.tickettype.infrastructure.persistence;

import com.tickets.event_service.event.infrastructure.persistence.EventJpaEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Entidad JPA de TicketType — vive SOLO en infrastructure/persistence.
 * El dominio TicketType.java es un POJO puro sin anotaciones de JPA.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ticket_types")
class TicketTypeJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private EventJpaEntity event;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "price_currency", length = 3, nullable = false)
    private String priceCurrency = "USD";

    @Column(name = "total_quantity", nullable = false)
    private int totalQuantity;

    @Column(name = "available_quantity", nullable = false)
    private int availableQuantity;
}
