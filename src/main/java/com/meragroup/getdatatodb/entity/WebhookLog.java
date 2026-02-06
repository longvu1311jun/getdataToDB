package com.meragroup.getdatatodb.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "webhook_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "external_id", nullable = false)
	private String externalId;

	@Column(name = "event_type")
	private String eventType;

	@Column(columnDefinition = "longtext")
	private String payload;

	@Column(name = "created_at", updatable = false)
	private Instant createdAt;

}


