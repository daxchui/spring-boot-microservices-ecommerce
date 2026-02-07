package com.comp5348.store.repository;

import com.comp5348.store.model.Order;
import com.comp5348.store.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomer(Customer customer);
    Optional<Order> findTopByCustomerIdOrderByOrderDateDesc(Long customerId);

}
