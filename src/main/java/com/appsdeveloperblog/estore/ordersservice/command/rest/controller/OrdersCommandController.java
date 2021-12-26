package com.appsdeveloperblog.estore.ordersservice.command.rest.controller;

import com.appsdeveloperblog.estore.ordersservice.command.rest.models.CreateOrderCommand;
import com.appsdeveloperblog.estore.ordersservice.command.rest.models.CreateOrderRestModel;
import com.appsdeveloperblog.estore.ordersservice.command.rest.models.OrderStatus;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrdersCommandController {

    private final Environment env;
    private final CommandGateway commandGateway;
    private final static String USER_ID = "27b95829-4f3f-4ddf-8983-151ba010e35b";

    public OrdersCommandController(Environment env, CommandGateway commandGateway) {
        this.env = env;
        this.commandGateway = commandGateway;
    }

    @PostMapping
    public String createOrder(@Valid @RequestBody CreateOrderRestModel createOrderRestModel){

        String returnValue;

        CreateOrderCommand createOrderCommand = CreateOrderCommand.builder().userId(USER_ID)
                .productId(createOrderRestModel.getProductId())
                .orderId(UUID.randomUUID().toString())
                .orderStatus(OrderStatus.CREATED)
                .addressId(createOrderRestModel.getAddressId())
                .quantity(createOrderRestModel.getQuantity())
                .build();

        returnValue = commandGateway.sendAndWait(createOrderCommand);

        return returnValue;
    }

    @GetMapping
    public String getProduct() {
        return "HTTP GET Handled " + env.getProperty("local.server.port");
    }
}
