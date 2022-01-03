package com.appsdeveloperblog.estore.ordersservice.command.models;

import lombok.Data;

@Data
public class CreateOrderRestModel {

    private String orderId;
    private String userId;
    private String productId;
    private Integer quantity;
    private String addressId;
    private OrderStatus orderStatus;
}
