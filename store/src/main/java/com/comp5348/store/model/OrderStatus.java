package com.comp5348.store.model;

public enum OrderStatus {
    PENDING, // Order placed but not paid yet
    PAID, // Payment successful
    PROCESSING, // Warehouse preparing delivery
    IN_TRANSIT, // Transit in delivery
    DISPATCHED, // DeliveryCo picked up the package
    DELIVERED, // Delivered to customer
    CANCELLED, // Cancelled by user before dispatch
    FAILED, // Failed due to payment/delivery issue
    DELIVERY_LOST // Lost during delivery, the 5% chance
}
