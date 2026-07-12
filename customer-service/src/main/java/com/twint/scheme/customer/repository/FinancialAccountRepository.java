package com.twint.scheme.customer.repository;

import com.twint.scheme.customer.entity.FinancialAccount;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface FinancialAccountRepository extends JpaRepository<FinancialAccount, UUID> {
  Optional<FinancialAccount> findByCustomerIdAndIsP2pDefaultTrue(UUID customerId);

  Optional<FinancialAccount> findByIdAndCustomerId(UUID id, UUID customerId);

  @Modifying
  @Query("UPDATE FinancialAccount fa SET fa.isP2pDefault = false WHERE fa.customer.id = :customerId")
  void clearP2pDefaultForCustomer(UUID customerId);
}
