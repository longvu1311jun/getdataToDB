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
@Table(name = "customers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	@Column(name = "phone", nullable = false)
	private String phone;

	private String address;

	@Column(name = "created_at", updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at")
	private Instant updatedAt;

	@Column(name = "deleted_at")
	private Instant deletedAt;

}


