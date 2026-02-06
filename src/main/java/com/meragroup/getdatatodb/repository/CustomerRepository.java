package com.meragroup.getdatatodb.repository;

import com.meragroup.getdatatodb.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
	Optional<Customer> findFirstByPhone(String phone);
}


