package com.twint.scheme.customer.repository;

import com.twint.scheme.customer.entity.Customer;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
  Optional<Customer> findByPhoneNumber(String phoneNumber);
  boolean existsByPhoneNumber(String phoneNumber);
}
