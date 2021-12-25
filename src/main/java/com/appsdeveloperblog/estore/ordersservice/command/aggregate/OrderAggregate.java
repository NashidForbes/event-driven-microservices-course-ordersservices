package com.appsdeveloperblog.estore.ordersservice.command.aggregate;

import com.appsdeveloperblog.estore.ordersservice.command.rest.models.OrderStatus;
import com.appsdeveloperblog.estore.ordersservice.core.events.OrderCreatedEvent;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

@Aggregate
public class OrderAggregate {

    // associate the dispatch command e.g. CreateOrderCommand class
    // with the right aggregate
    // via the id (target id -> AggregateIdentifier)
    @AggregateIdentifier
    private String orderId;
    private String userId;
    private String productId;
    private Integer quantity;
    private String addressId;
    private OrderStatus orderStatus;

    // use initialize the aggregate class with the latest information state
    // avoid adding any business logic, use this handler handler to update the
    // aggregate state.

    @EventSourcingHandler
    public void on(OrderCreatedEvent orderCreatedEvent){
        this.orderId = orderCreatedEvent.getOrderId();
        this.userId = orderCreatedEvent.getUserId();
        this.productId = orderCreatedEvent.getProductId();
        this.quantity = orderCreatedEvent.getQuantity();
        this.addressId = orderCreatedEvent.getAddressId();
        this.orderStatus = orderCreatedEvent.getOrderStatus();

    }
}
