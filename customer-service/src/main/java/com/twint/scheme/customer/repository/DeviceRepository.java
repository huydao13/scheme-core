package com.twint.scheme.customer.repository;

import com.twint.scheme.customer.entity.Device;
import com.twint.scheme.customer.enumeration.DeviceStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRepository extends JpaRepository<Device, UUID> {
  long countByCustomerIdAndStatus(UUID customerId, DeviceStatus status);
  Optional<Device> findByIdAndCustomerId(UUID id, UUID customerId);
  List<Device> findAllByCustomerId(UUID customerId);
}
