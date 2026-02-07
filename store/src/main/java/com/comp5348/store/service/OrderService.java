package com.comp5348.store.service;

import com.comp5348.contracts.DeliveryRequest;
import com.comp5348.contracts.EmailRequest;
import com.comp5348.contracts.PaymentRequest;
import com.comp5348.contracts.PaymentResponse;
import com.comp5348.store.dto.OrderResponseDTO;
import com.comp5348.store.messaging.*;
import com.comp5348.store.model.*;
import com.comp5348.store.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private final RabbitTemplate rabbitTemplate;

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final OrderAllocationRepository orderAllocationRepository;

    private final BankMessageProducer bankProducer;
    private final DeliveryMessageProducer deliveryProducer;
    private final EmailMessageProducer emailProducer;

    public OrderService(CustomerRepository customerRepository,
                        ProductRepository productRepository,
                        OrderRepository orderRepository,
                        WarehouseStockRepository warehouseStockRepository,
                        OrderAllocationRepository orderAllocationRepository,
                        BankMessageProducer bankProducer,
                        DeliveryMessageProducer deliveryProducer,
                        EmailMessageProducer emailProducer,
                        RabbitTemplate rabbitTemplate) {
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.warehouseStockRepository = warehouseStockRepository;
        this.orderAllocationRepository = orderAllocationRepository;
        this.bankProducer = bankProducer;
        this.deliveryProducer = deliveryProducer;
        this.emailProducer = emailProducer;
        this.rabbitTemplate = rabbitTemplate;
    }

    // Place order function
    @Transactional
    public OrderResponseDTO placeOrder(Long customerId, Long productId, int quantity) {
        logger.info("Received order request: customerId={}, productId={}, quantity={}", customerId, productId, quantity);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        List<WarehouseStock> stockList = warehouseStockRepository.findByProductId(productId);
        int totalAvailable = stockList.stream().mapToInt(WarehouseStock::getQuantity).sum();
        if (totalAvailable < quantity) {
            return failGracefully(customer, product, "Insufficient stock (" + totalAvailable + " available)");
        }

        double totalAmount = product.getPrice() * quantity;

        // Create order
        Order order = new Order();
        order.setCustomer(customer);
        order.setProduct(product);
        order.setQuantity(quantity);
        order.setTotalAmount(totalAmount);
        order.setDeliveryAddress(customer.getAddress());
        order.setStatus(OrderStatus.PENDING);
        orderRepository.save(order);

        // Request payment via RabbitMQ (RPC)
        PaymentRequest paymentReq = new PaymentRequest(
                order.getId(),
                1L,
                String.valueOf(customer.getBankAccountId()),
                totalAmount,
                PaymentRequest.Type.CHARGE
        );
        PaymentResponse paymentRes = bankProducer.sendPayment(paymentReq);

        if (paymentRes == null || !paymentRes.isSuccess()) {
            order.setStatus(OrderStatus.FAILED);
            orderRepository.save(order);
            emailProducer.sendEmail(new EmailRequest(
                    customer.getEmail(),
                    "Order Failed",
                    "Payment failed — please check your balance or try again later.",
                    order.getId()
            ));
            return buildDto(order);
        }

        // Deduct stock and create allocations
        Map<Warehouse, Integer> allocationMap = new LinkedHashMap<>();
        int remaining = quantity;
        for (WarehouseStock ws : stockList) {
            if (remaining == 0) break;
            int available = ws.getQuantity();
            int used = Math.min(available, remaining);
            if (used > 0) {
                ws.setQuantity(available - used);
                warehouseStockRepository.save(ws);
                allocationMap.put(ws.getWarehouse(), used);
                remaining -= used;
            }
        }

        for (Map.Entry<Warehouse, Integer> entry : allocationMap.entrySet()) {
            OrderAllocation allocation = new OrderAllocation(order, entry.getKey(), entry.getValue());
            order.getAllocations().add(allocation);
        }

        order.setStatus(OrderStatus.PROCESSING);
        orderRepository.save(order);

        String deliveryLocation = (allocationMap.size() == 1)
                ? allocationMap.keySet().iterator().next().getLocation()
                : "Mixed Warehouses";

        // Async delivery + email after commit
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                CompletableFuture.runAsync(() -> {
                    try {
                        deliveryProducer.sendDeliveryRequest(new DeliveryRequest(
                                order.getId(),
                                deliveryLocation,
                                order.getDeliveryAddress()
                        ));
                        emailProducer.sendEmail(new EmailRequest(
                                customer.getEmail(),
                                "Order Processing",
                                "Your order is being prepared for shipment. You’ll receive updates soon.",
                                order.getId()
                        ));
                    } catch (Exception e) {
                        logger.error("Failed to send async RabbitMQ messages: {}", e.getMessage());
                    }
                });
            }
        });

        return buildDto(order);
    }

    // cancel order
    @Transactional
    public OrderResponseDTO cancelOrder(Long orderId) {
        logger.info("Received cancellation request for order {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != OrderStatus.PROCESSING) {
            throw new RuntimeException("Order cannot be cancelled after delivery request sent");
        }

        // Restore stock based on allocations
        for (OrderAllocation allocation : order.getAllocations()) {
            WarehouseStock stock = warehouseStockRepository
                    .findByWarehouseIdAndProductId(allocation.getWarehouse().getId(), order.getProduct().getId());
            if (stock != null) {
                stock.setQuantity(stock.getQuantity() + allocation.getQuantity());
                warehouseStockRepository.save(stock);
            }
        }

        // refund via RabbitMQ (RPC)
        PaymentRequest refundReq = new PaymentRequest(
                order.getId(),
                1L,
                String.valueOf(order.getCustomer().getBankAccountId()),
                order.getTotalAmount(),
                PaymentRequest.Type.REFUND
        );
        PaymentResponse refundRes = bankProducer.sendPayment(refundReq);

        if (refundRes == null || !refundRes.isSuccess()) {
            emailProducer.sendEmail(new EmailRequest(
                    order.getCustomer().getEmail(),
                    "Refund Failed",
                    "Your refund for order #" + order.getId() + " could not be processed.",
                    order.getId()
            ));
            throw new RuntimeException("Refund failed");
        }

        // update order + notify
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        emailProducer.sendEmail(new EmailRequest(
                order.getCustomer().getEmail(),
                "Order Cancelled",
                "Your order #" + order.getId() + " for " + order.getProduct().getName() + " has been cancelled and refunded successfully.",
                order.getId()
        ));

        rabbitTemplate.convertAndSend("cancel.exchange", "order.cancelled", order.getId());
        logger.info("[Store → DeliveryCo] Published cancellation event for order {}", order.getId());

        return buildDto(order);
    }

    // status update of order
    @Transactional
    public boolean updateOrderStatus(Long orderId, String status) {
        logger.info("Updating order {} to status {}", orderId, status);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (EnumSet.of(OrderStatus.CANCELLED, OrderStatus.DELIVERED, OrderStatus.DELIVERY_LOST, OrderStatus.FAILED)
                .contains(order.getStatus())) {
            logger.warn("Ignoring update for order {} — already finalized", orderId);
            return false;
        }

        OrderStatus newStatus = OrderStatus.valueOf(status.toUpperCase());
        order.setStatus(newStatus);
        orderRepository.save(order);

        Customer customer = order.getCustomer();
        Product product = order.getProduct();

        switch (newStatus) {
            case DELIVERED -> emailProducer.sendEmail(new EmailRequest(
                    customer.getEmail(),
                    "Order Delivered",
                    "Your order #" + order.getId() + " for " + product.getName() + " has been successfully delivered!",
                    order.getId()
            ));
            case DELIVERY_LOST -> {
                // For lost deliveries, issue a refund but do not restore stock
                PaymentRequest refundReq = new PaymentRequest(
                        order.getId(),
                        1L,
                        String.valueOf(customer.getBankAccountId()),
                        order.getTotalAmount(),
                        PaymentRequest.Type.REFUND
                );
                PaymentResponse refundRes = bankProducer.sendPayment(refundReq);

                if (refundRes == null || !refundRes.isSuccess()) {
                    emailProducer.sendEmail(new EmailRequest(
                            customer.getEmail(),
                            "Refund Failed",
                            "Your refund for lost order #" + order.getId() + " could not be processed.",
                            order.getId()
                    ));
                } else {
                    emailProducer.sendEmail(new EmailRequest(
                            customer.getEmail(),
                            "Order Lost and Refunded",
                            "Unfortunately, your order #" + order.getId() + " for " + product.getName() + " was lost during delivery. A full refund has been issued.",
                            order.getId()
                    ));
                }
            }
            case FAILED -> emailProducer.sendEmail(new EmailRequest(
                    customer.getEmail(),
                    "Order Failed",
                    "There was an unexpected issue with your order. Please contact support.",
                    order.getId()
            ));
            default -> logger.info("No email triggered for {}", newStatus);
        }

        return true;
    }

    // helper function
    public OrderResponseDTO buildDto(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setOrderId(order.getId());
        dto.setCustomerName(order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName());
        dto.setProductName(order.getProduct().getName());
        dto.setQuantity(order.getQuantity());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setDeliveryAddress(order.getDeliveryAddress());
        dto.setStatus(order.getStatus());
        dto.setOrderDate(order.getOrderDate());
        return dto;
    }

    private OrderResponseDTO failGracefully(Customer customer, Product product, String reason) {
        emailProducer.sendEmail(new EmailRequest(customer.getEmail(), "Order Failed", reason, null));
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setCustomerName(customer.getFirstName() + " " + customer.getLastName());
        dto.setProductName(product.getName());
        dto.setQuantity(0);
        dto.setTotalAmount(0);
        dto.setDeliveryAddress(customer.getAddress());
        dto.setStatus(OrderStatus.FAILED);
        return dto;
    }

    private Warehouse findWarehouseWithStock(Long productId, int quantity) {
        List<WarehouseStock> stockList = warehouseStockRepository.findByProductId(productId);
        for (WarehouseStock stock : stockList) {
            if (stock.getQuantity() >= quantity) return stock.getWarehouse();
        }
        return null;
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO getLatestOrder(Long customerId) {
        Order latest = orderRepository.findTopByCustomerIdOrderByOrderDateDesc(customerId)
                .orElseThrow(() -> new RuntimeException("No orders found for this customer"));
        return new OrderResponseDTO(latest);
    }

    public List<Order> getAllOrders() { return orderRepository.findAll(); }

    public Order getOrderById(Long id) { return orderRepository.findById(id).orElse(null); }
}
