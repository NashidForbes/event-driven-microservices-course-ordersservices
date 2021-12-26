package com.appsdeveloperblog.estore.ordersservice.command.aggregate;

import com.appsdeveloperblog.estore.ordersservice.command.rest.models.CreateOrderCommand;
import com.appsdeveloperblog.estore.ordersservice.command.rest.models.OrderStatus;
import com.appsdeveloperblog.estore.ordersservice.core.events.OrderCreatedEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.BeanUtils;

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

    public OrderAggregate() {
    }

    @CommandHandler
    public OrderAggregate(CreateOrderCommand createOrderCommand) {
        if (createOrderCommand.getProductId() == null || createOrderCommand.getProductId().isBlank()) {
            throw new IllegalArgumentException("Order ProductId field cannot be blank");
        }

        if (createOrderCommand.getAddressId() == null || createOrderCommand.getAddressId().isBlank()) {
            throw new IllegalArgumentException("Order AddressId field cannot be blank");
        }

        if (createOrderCommand.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent();

        // copy property values from source object to corresponding destination object properties.
        // thank you BeanUtils!
        BeanUtils.copyProperties(createOrderCommand, orderCreatedEvent);

        // apply "stages" event for publish, if no exceptions,
        // staged object is published update the OrderAggregate
        // state with the latest values
        AggregateLifecycle.apply(orderCreatedEvent);
    }


    // use initialize the aggregate class with the latest information state
    // avoid adding any business logic, use this handler handler to update the
    // aggregate state.
    @EventSourcingHandler
    public void on(OrderCreatedEvent orderCreatedEvent) {
        this.orderId = orderCreatedEvent.getOrderId();
        this.userId = orderCreatedEvent.getUserId();
        this.productId = orderCreatedEvent.getProductId();
        this.quantity = orderCreatedEvent.getQuantity();
        this.addressId = orderCreatedEvent.getAddressId();
        this.orderStatus = orderCreatedEvent.getOrderStatus();

    }
}
