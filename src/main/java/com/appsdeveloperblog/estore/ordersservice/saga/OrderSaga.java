package com.appsdeveloperblog.estore.ordersservice.saga;

import com.appsdeveloperblog.estore.ordersservice.core.events.OrderCreatedEvent;
import com.appsdeveloperblog.estore.sagacoreapi.commands.ReserveProductCommand;
import com.appsdeveloperblog.estore.sagacoreapi.events.ProductReservedEvent;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.CommandResultMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

@Saga
public class OrderSaga {
    // Since @Saga creates serialization use transient
    // to prevent serialization of fields.
    @Autowired
    private transient CommandGateway commandGateway;

    @StartSaga
    @SagaEventHandler(associationProperty="orderId")
    public void handle(OrderCreatedEvent orderCreatedEvent){
        ReserveProductCommand reserveProductCommand = ReserveProductCommand.builder()
                .orderId(orderCreatedEvent.getOrderId())
                .productId(orderCreatedEvent.getProductId())
                .quantity(orderCreatedEvent.getQuantity())
                .userId(orderCreatedEvent.getUserId())
                .build();

        commandGateway.send(reserveProductCommand, new CommandCallback<ReserveProductCommand, Object>() {

            @Override
            public void onResult(CommandMessage<? extends ReserveProductCommand> commandMessage,
                                 CommandResultMessage<?> commandResultMessage) {
                if(commandResultMessage.isExceptional()){
                    // Start a compensating transaction
                }
            }
        });
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(ProductReservedEvent productReservedEvent){
        // Process user payment
    }
}
