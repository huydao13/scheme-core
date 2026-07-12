package com.twint.scheme.merchant.repository;

import com.twint.scheme.merchant.entity.Merchant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MerchantRepository extends JpaRepository<Merchant, UUID> {
}
