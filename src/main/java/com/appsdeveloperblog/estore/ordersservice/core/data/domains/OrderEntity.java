package com.appsdeveloperblog.estore.ordersservice.core.data.domains;

import com.appsdeveloperblog.estore.ordersservice.command.rest.models.OrderStatus;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name="orders")
@Data
public class OrderEntity implements Serializable {

    private static final long serialVersionUID = -8456860737853529953L;

    @Id
    @Column(unique = true)
    private String orderId;
    private String userId;
    private String productId;
    private Integer quantity;
    private String addressId;
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;
}
