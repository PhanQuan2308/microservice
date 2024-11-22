package org.example.orderservice.repository;

import org.example.orderservice.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByPaymentToken(String token);
    Optional<Order> findByTransactionId(String transactionId);

    Page<Order> findAll(Pageable pageable);
}
