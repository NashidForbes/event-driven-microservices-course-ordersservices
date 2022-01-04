package com.appsdeveloperblog.estore.ordersservice.query.handlers;

import com.appsdeveloperblog.estore.ordersservice.core.data.domains.OrderEntity;
import com.appsdeveloperblog.estore.ordersservice.core.data.interfaces.OrdersRepository;
import com.appsdeveloperblog.estore.ordersservice.query.FindOrderQuery;
import com.appsdeveloperblog.estore.ordersservice.query.model.OrderSummary;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class OrderQueriesHandler {

    private final OrdersRepository ordersRepository;

    public OrderQueriesHandler(OrdersRepository ordersRepository) {
        this.ordersRepository = ordersRepository;
    }

    @QueryHandler
    public OrderSummary findOrder(FindOrderQuery findOrderQuery){
       OrderEntity orderEntity = ordersRepository.findByOrderId(findOrderQuery.getOrderId());
       return new OrderSummary(orderEntity.getOrderId(), orderEntity.getOrderStatus());
    }
}
