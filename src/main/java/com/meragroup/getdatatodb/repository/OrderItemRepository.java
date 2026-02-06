package com.meragroup.getdatatodb.repository;

import com.meragroup.getdatatodb.entity.OrderItem;
import com.meragroup.getdatatodb.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
	List<OrderItem> findByOrder(Order order);

	@Modifying
	@Transactional
	@Query("delete from OrderItem oi where oi.order.id = :orderId")
	void deleteByOrderId(Long orderId);
}


