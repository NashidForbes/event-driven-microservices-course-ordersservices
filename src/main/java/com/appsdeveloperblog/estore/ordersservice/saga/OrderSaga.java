package com.appsdeveloperblog.estore.ordersservice.saga;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

@Saga
public class OrderSaga {
    // Since @Saga creates serialization use transient
    // to prevent serialization of fields.
    @Autowired
    private transient CommandGateway commandGateway;
}
