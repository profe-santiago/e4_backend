package com.tickets.event_service.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockReserveCommand {
    private UUID orderId;
    private UUID userId;
    private List<StockReserveItem> items;
}
