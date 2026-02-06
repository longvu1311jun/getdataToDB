package com.meragroup.getdatatodb.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meragroup.getdatatodb.dto.WebhookOrderDTO;
import com.meragroup.getdatatodb.entity.Customer;
import com.meragroup.getdatatodb.entity.Order;
import com.meragroup.getdatatodb.entity.OrderItem;
import com.meragroup.getdatatodb.entity.WebhookLog;
import com.meragroup.getdatatodb.repository.CustomerRepository;
import com.meragroup.getdatatodb.repository.OrderItemRepository;
import com.meragroup.getdatatodb.repository.OrderRepository;
import com.meragroup.getdatatodb.repository.WebhookLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookProcessingService {

	private final ObjectMapper objectMapper;
	private final WebhookLogRepository webhookLogRepository;
	private final CustomerRepository customerRepository;
	private final OrderRepository orderRepository;
	private final OrderItemRepository orderItemRepository;

	@Transactional
	public void processWebhook(String rawPayload) {
		try {
			WebhookOrderDTO dto = objectMapper.readValue(rawPayload, WebhookOrderDTO.class);
			if (dto == null || dto.getId() == null) {
				throw new IllegalArgumentException("missing id in webhook payload");
			}

			String externalId = String.valueOf(dto.getId());
			String eventType = dto.getEvent_type();

			// Idempotency check: external_id + event_type
			Optional<WebhookLog> existing = webhookLogRepository.findByExternalIdAndEventType(externalId, eventType);
			if (existing.isPresent()) {
				log.info("Duplicate webhook received: externalId={}, eventType={}", externalId, eventType);
				return;
			}

			// Insert webhook log first (save raw payload)
			WebhookLog logEntry = WebhookLog.builder()
					.externalId(externalId)
					.eventType(eventType)
					.payload(rawPayload)
					.createdAt(Instant.now())
					.build();
			webhookLogRepository.save(logEntry);

			// Upsert customer by phone
			String phone = extractPhone(dto);
			Customer customer = null;
			if (StringUtils.hasText(phone)) {
				Optional<Customer> found = customerRepository.findFirstByPhone(phone);
				if (found.isPresent()) {
					customer = found.get();
					boolean changed = false;
					if (StringUtils.hasText(dto.getBill_full_name()) && !dto.getBill_full_name().equals(customer.getName())) {
						customer.setName(dto.getBill_full_name());
						changed = true;
					}
					String addr = extractAddress(dto);
					if (StringUtils.hasText(addr) && !addr.equals(customer.getAddress())) {
						customer.setAddress(addr);
						changed = true;
					}
					if (changed) {
						customerRepository.save(customer);
					}
				} else {
					customer = Customer.builder()
							.name(dto.getBill_full_name())
							.phone(phone)
							.address(extractAddress(dto))
							.createdAt(Instant.now())
							.build();
					customerRepository.save(customer);
				}
			}

			// Upsert order by external order id (use externalId as order_code)
			final Customer finalCustomer = customer;
			Order order = orderRepository.findByOrderCode(externalId)
					.map(existingOrder -> {
						existingOrder.setStatus(dto.getStatus());
						existingOrder.setTotalAmount(dto.getTotal_price());
						existingOrder.setNote(dto.getNote());
						if (finalCustomer != null) {
							existingOrder.setCustomer(finalCustomer);
						}
						existingOrder.setUpdatedAt(Instant.now());
						return orderRepository.save(existingOrder);
					})
					.orElseGet(() -> {
						Order newOrder = Order.builder()
								.orderCode(externalId)
								.status(dto.getStatus())
								.totalAmount(dto.getTotal_price())
								.note(dto.getNote())
								.customer(finalCustomer)
								.createdAt(Instant.now())
								.build();
						return orderRepository.save(newOrder);
					});

			// Sync order items: delete old -> insert new
			orderItemRepository.deleteByOrderId(order.getId());

			List<OrderItem> items = new ArrayList<>();
			if (dto.getItems() != null) {
				for (WebhookOrderDTO.ItemDTO it : dto.getItems()) {
					String productName = null;
					if (it.getVariation_info() != null && it.getVariation_info().getName() != null) {
						productName = it.getVariation_info().getName();
					}
					if (!StringUtils.hasText(productName)) {
						productName = "item-" + (it.getId() != null ? it.getId() : "unknown");
					}
					Integer qty = it.getQuantity() != null ? it.getQuantity() : 1;
					Long price = it.getRetail_price() != null ? it.getRetail_price()
							: (it.getVariation_info() != null ? it.getVariation_info().getRetail_price() : 0L);
					OrderItem orderItem = OrderItem.builder()
							.order(order)
							.productName(productName)
							.quantity(qty)
							.price(price)
							.build();
					items.add(orderItem);
				}
			}
			if (!items.isEmpty()) {
				orderItemRepository.saveAll(items);
			}

		} catch (RuntimeException re) {
			log.error("Runtime exception processing webhook", re);
			throw re;
		} catch (Exception e) {
			log.error("Failed to process webhook", e);
			throw new RuntimeException(e);
		}
	}

	private String extractPhone(WebhookOrderDTO dto) {
		if (dto.getBill_phone_number() != null && !dto.getBill_phone_number().isBlank()) {
			return dto.getBill_phone_number();
		}
		if (dto.getCustomer() != null && dto.getCustomer().getPhone_numbers() != null && !dto.getCustomer().getPhone_numbers().isEmpty()) {
			return dto.getCustomer().getPhone_numbers().get(0);
		}
		return null;
	}

	private String extractAddress(WebhookOrderDTO dto) {
		if (dto.getShipping_address() != null && dto.getShipping_address().getFull_address() != null) {
			return dto.getShipping_address().getFull_address();
		}
		return null;
	}

}


