package com.tickets.ticket_service.order.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class OrderItemResponse {
    private Long id;
    private UUID eventId;
    private Long ticketTypeId;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}
