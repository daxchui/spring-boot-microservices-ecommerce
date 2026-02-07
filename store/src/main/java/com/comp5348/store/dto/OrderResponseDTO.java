package com.comp5348.store.dto;

import com.comp5348.store.model.Order;
import com.comp5348.store.model.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderResponseDTO {
    private Long orderId;
    private String customerName;
    private String productName;
    private int quantity;
    private double totalAmount;
    private String deliveryAddress;
    private OrderStatus status;
    private LocalDateTime orderDate;

    public OrderResponseDTO() {}

    public OrderResponseDTO(Order order) {
        this.orderId = order.getId();
        this.customerName = order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName();
        this.productName = order.getProduct().getName();
        this.quantity = order.getQuantity();
        this.totalAmount = order.getTotalAmount();
        this.deliveryAddress = order.getDeliveryAddress();
        this.status = order.getStatus();
        this.orderDate = order.getOrderDate();
    }
}
