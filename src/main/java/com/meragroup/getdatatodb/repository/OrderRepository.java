package com.meragroup.getdatatodb.repository;

import com.meragroup.getdatatodb.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
	Optional<Order> findByOrderCode(String orderCode);
}


