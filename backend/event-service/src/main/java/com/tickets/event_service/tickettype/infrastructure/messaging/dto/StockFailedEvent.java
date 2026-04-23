package com.tickets.event_service.tickettype.infrastructure.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockFailedEvent {
    private UUID orderId;
    private String reason;
}
