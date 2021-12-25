package com.appsdeveloperblog.estore.ordersservice.core.handlers;

import com.appsdeveloperblog.estore.ordersservice.core.data.domains.OrderEntity;
import com.appsdeveloperblog.estore.ordersservice.core.data.interfaces.OrdersRepository;
import com.appsdeveloperblog.estore.ordersservice.core.events.OrderCreatedEvent;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("order-group")
public class OrderEventsHandler {

    private final OrdersRepository ordersRepository;

    public OrderEventsHandler(OrdersRepository ordersRepository) {
        this.ordersRepository = ordersRepository;
    }


    @EventHandler
    public void on(OrderCreatedEvent event){
        OrderEntity orderEntity = new OrderEntity();
        BeanUtils.copyProperties(event, orderEntity);

        try {
            this.ordersRepository.save(orderEntity);
        } catch(Exception ex){
            ex.printStackTrace();
        }

    }
}
