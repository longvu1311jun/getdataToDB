package com.meragroup.getdatatodb.repository;

import com.meragroup.getdatatodb.entity.WebhookLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WebhookLogRepository extends JpaRepository<WebhookLog, Long> {
	Optional<WebhookLog> findByExternalIdAndEventType(String externalId, String eventType);
}


