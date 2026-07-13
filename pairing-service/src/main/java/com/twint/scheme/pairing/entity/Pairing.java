package com.twint.scheme.pairing.entity;

import com.twint.scheme.pairing.enumeration.CancelledBy;
import com.twint.scheme.pairing.enumeration.PairingState;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "pairing")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pairing {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, unique = true, length = 20)
  private String token;

  @Column(name = "merchant_id", nullable = false)
  private UUID merchantId;

  @Column(name = "terminal_id", nullable = false)
  private UUID terminalId;

  @Column(name = "customer_id")
  private UUID customerId;

  @Column(name = "order_id")
  private UUID orderId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private PairingState state = PairingState.CREATED;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "cancelled_by", length = 20)
  private CancelledBy cancelledBy;

  @Column(name = "cancel_reason", length = 200)
  private String cancelReason;

  @Column(name = "token_blacklisted")
  private boolean tokenBlacklisted = false;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private Instant updatedAt;
}
