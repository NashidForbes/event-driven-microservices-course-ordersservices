package com.appsdeveloperblog.estore.ordersservice.core.events;

import com.appsdeveloperblog.estore.ordersservice.command.models.OrderStatus;
import lombok.Value;

@Value
public class OrderRejectedEvent {

    private String orderId;
    private String reason;
    private final OrderStatus orderStatus = OrderStatus.REJECTED;
}
