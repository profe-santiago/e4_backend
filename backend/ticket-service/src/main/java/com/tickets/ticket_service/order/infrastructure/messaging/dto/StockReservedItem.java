package com.tickets.ticket_service.order.infrastructure.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockReservedItem {
    private UUID eventId;
    private Long ticketTypeId;
    private int quantity;
    private BigDecimal unitPrice;
}
