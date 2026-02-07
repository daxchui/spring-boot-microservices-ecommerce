package com.comp5348.store.controller;

import com.comp5348.store.model.Order;
import com.comp5348.store.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.comp5348.store.dto.OrderResponseDTO;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // Place a new order
    @PostMapping("/place")
    public ResponseEntity<OrderResponseDTO> placeOrder(
            @RequestParam Long customerId,
            @RequestParam Long productId,
            @RequestParam int quantity) {
        OrderResponseDTO dto = orderService.placeOrder(customerId, productId, quantity);
        return ResponseEntity.ok(dto);
    }

    // View all orders
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    // View a specific order
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        OrderResponseDTO dto = orderService.buildDto(order);
        return ResponseEntity.ok(dto);
    }



    // Customer cancel order
    @PostMapping("/cancel/{orderId}")
    public ResponseEntity<OrderResponseDTO> cancelOrder(@PathVariable Long orderId) {
        OrderResponseDTO dto = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/customer/{customerId}/latest")
    public ResponseEntity<OrderResponseDTO> getLatestOrder(@PathVariable Long customerId) {
        OrderResponseDTO response = orderService.getLatestOrder(customerId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/updateStatus/{orderId}")
    public ResponseEntity<String> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status) {

        orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok("Order " + orderId + " updated to " + status);
    }


}
