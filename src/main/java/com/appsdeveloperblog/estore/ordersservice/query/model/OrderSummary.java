package com.appsdeveloperblog.estore.ordersservice.query.model;

import com.appsdeveloperblog.estore.ordersservice.command.models.OrderStatus;
import lombok.Value;

@Value
public class OrderSummary {
    private final String orderId;
    private final OrderStatus orderStatus;
}
