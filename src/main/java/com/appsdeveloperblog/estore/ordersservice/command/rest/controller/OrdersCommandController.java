package com.appsdeveloperblog.estore.ordersservice.command.rest.controller;

import com.appsdeveloperblog.estore.ordersservice.command.models.CreateOrderCommand;
import com.appsdeveloperblog.estore.ordersservice.command.models.CreateOrderRestModel;
import com.appsdeveloperblog.estore.ordersservice.command.models.OrderStatus;
import com.appsdeveloperblog.estore.ordersservice.query.FindOrderQuery;
import com.appsdeveloperblog.estore.ordersservice.query.model.OrderSummary;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.SubscriptionQueryResult;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrdersCommandController {

    private final Environment env;
    private final CommandGateway commandGateway;
    private final QueryGateway queryGateway;
    private final static String USER_ID = "27b95829-4f3f-4ddf-8983-151ba010e35b";

    public OrdersCommandController(Environment env, CommandGateway commandGateway, QueryGateway queryGateway) {
        this.env = env;
        this.commandGateway = commandGateway;
        this.queryGateway = queryGateway;
    }

    @PostMapping
    public OrderSummary createOrder(@Valid @RequestBody CreateOrderRestModel createOrderRestModel){

        String returnValue;
        String orderId = UUID.randomUUID().toString();

        CreateOrderCommand createOrderCommand = CreateOrderCommand.builder().userId(USER_ID)
                .productId(createOrderRestModel.getProductId())
                .orderId(UUID.randomUUID().toString())
                .orderStatus(OrderStatus.CREATED)
                .addressId(createOrderRestModel.getAddressId())
                .quantity(createOrderRestModel.getQuantity())
                .build();

        SubscriptionQueryResult<OrderSummary, OrderSummary> queryResult =
                queryGateway.subscriptionQuery(new FindOrderQuery(orderId), ResponseTypes.instanceOf(OrderSummary.class),
                        ResponseTypes.instanceOf(OrderSummary.class));

        try {
            commandGateway.sendAndWait(createOrderCommand);
            return queryResult.updates().blockFirst();
        } finally {
            queryResult.close();
        }

    }

    @GetMapping
    public String getProduct() {
        return "HTTP GET Handled " + env.getProperty("local.server.port");
    }
}
