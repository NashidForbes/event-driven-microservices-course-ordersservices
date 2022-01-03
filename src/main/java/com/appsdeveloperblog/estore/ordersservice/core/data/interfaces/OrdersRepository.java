package com.appsdeveloperblog.estore.ordersservice.core.data.interfaces;

import com.appsdeveloperblog.estore.ordersservice.core.data.domains.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdersRepository extends JpaRepository<OrderEntity, String> {
    OrderEntity findByOrderId(String orderId);
}
