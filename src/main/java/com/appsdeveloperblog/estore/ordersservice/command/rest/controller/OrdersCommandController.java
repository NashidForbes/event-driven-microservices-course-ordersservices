package com.appsdeveloperblog.estore.ordersservice.command.rest.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/orders")
public class OrdersCommandController {

    @PostMapping
    public String createOrderCommand(@Valid @RequestBody CreateOrderRestModel createOrderRestModel){

    }
}
