package com.twint.scheme.pairing.service;

import com.twint.scheme.pairing.dto.request.CancelPairingRequest;
import com.twint.scheme.pairing.dto.request.CompletePairingRequest;
import com.twint.scheme.pairing.dto.request.ConfirmPairingRequest;
import com.twint.scheme.pairing.dto.request.CreatePairingRequest;
import com.twint.scheme.pairing.dto.request.ScanPairingRequest;
import com.twint.scheme.pairing.dto.response.CancelPairingResponse;
import com.twint.scheme.pairing.dto.response.CompletePairingResponse;
import com.twint.scheme.pairing.dto.response.ConfirmPairingResponse;
import com.twint.scheme.pairing.dto.response.FindByTokenResponse;
import com.twint.scheme.pairing.dto.response.PairingResponse;
import com.twint.scheme.pairing.dto.response.ScanPairingResponse;
import com.twint.scheme.pairing.entity.Pairing;
import com.twint.scheme.pairing.enumeration.PairingState;
import com.twint.scheme.pairing.exception.CannotCancelPairingException;
import com.twint.scheme.pairing.exception.InvalidPairingStateException;
import com.twint.scheme.pairing.exception.PairingNotFoundException;
import com.twint.scheme.pairing.exception.TokenAlreadyUsedException;
import com.twint.scheme.pairing.exception.TokenExpiredException;
import com.twint.scheme.pairing.exception.TokenNotFoundException;
import com.twint.scheme.pairing.repository.PairingRedisRepository;
import com.twint.scheme.pairing.repository.PairingRepository;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PairingServiceImpl implements PairingService {
  private static final int TOKEN_TTL_SECONDS = 30;
  private static final Set<PairingState> CANCELLABLE_STATES =
      Set.of(PairingState.CREATED, PairingState.SCANNED, PairingState.CONFIRMED);

  private final PairingRepository pairingRepository;
  private final PairingRedisRepository redisRepository;
  private final QrCodeService         qrCodeService;

  @Override
  @Transactional
  public PairingResponse createPairing(CreatePairingRequest request) {
    String token    = generateToken();
    Instant expires = Instant.now().plusSeconds(TOKEN_TTL_SECONDS);

    Pairing pairing = Pairing.builder()
        .token(token)
        .merchantId(request.getMerchantId())
        .terminalId(request.getTerminalId())
        .state(PairingState.CREATED)
        .expiresAt(expires)
        .build();
    pairingRepository.save(pairing);

    // Lưu token vào Redis với TTL 30s
    redisRepository.saveToken(token, pairing.getId());

    // Generate QR code
    String qrCode = qrCodeService.generateQrCodeBase64(token);

    return PairingResponse.builder()
        .id(pairing.getId())
        .token(token)
        .qrCode(qrCode)
        .merchantId(pairing.getMerchantId())
        .terminalId(pairing.getTerminalId())
        .state(pairing.getState().name())
        .expiresAt(pairing.getExpiresAt())
        .createdAt(pairing.getCreatedAt())
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public PairingResponse getPairing(UUID id) {
    Pairing pairing = findPairingById(id);
    return PairingResponse.builder()
        .id(pairing.getId())
        .merchantId(pairing.getMerchantId())
        .terminalId(pairing.getTerminalId())
        .customerId(pairing.getCustomerId())
        .state(pairing.getState().name())
        .expiresAt(pairing.getExpiresAt())
        .createdAt(pairing.getCreatedAt())
        .updatedAt(pairing.getUpdatedAt())
        .build();
  }

  @Override
  @Transactional
  public ScanPairingResponse scanPairing(ScanPairingRequest request, UUID customerId) {
    String token = request.getToken();

    // Check token đã dùng chưa
    if (redisRepository.isTokenUsed(token)) {
      throw new TokenAlreadyUsedException();
    }

    // Check token còn hạn không
    if (redisRepository.isTokenExpired(token)) {
      throw new TokenExpiredException();
    }

    // Lấy pairingId từ Redis
    String pairingIdStr = redisRepository.getPairingIdByToken(token);
    if (pairingIdStr == null) {
      throw new TokenNotFoundException(token);
    }

    Pairing pairing = findPairingById(UUID.fromString(pairingIdStr));

    // Validate state
    if (pairing.getState() != PairingState.CREATED) {
      throw new InvalidPairingStateException(PairingState.CREATED, pairing.getState());
    }

    // Update state → SCANNED
    pairing.setState(PairingState.SCANNED);
    pairing.setCustomerId(customerId);
    pairingRepository.save(pairing);

    // Blacklist token — không cho scan lần 2
    redisRepository.blacklistToken(token);

    return ScanPairingResponse.builder()
        .pairingId(pairing.getId())
        .merchantId(pairing.getMerchantId())
        .merchantName("") // TODO: gọi merchant-service nếu cần
        .terminalId(pairing.getTerminalId())
        .terminalCode("") // TODO: gọi merchant-service nếu cần
        .state(pairing.getState().name())
        .customerId(customerId)
        .build();
  }

  @Override
  @Transactional
  public ConfirmPairingResponse confirmPairing(UUID id, ConfirmPairingRequest request) {
    Pairing pairing = findPairingById(id);

    if (pairing.getState() != PairingState.SCANNED) {
      throw new InvalidPairingStateException(PairingState.SCANNED, pairing.getState());
    }

    pairing.setState(PairingState.CONFIRMED);
    pairing.setOrderId(request.getOrderId());
    pairingRepository.save(pairing);

    return ConfirmPairingResponse.builder()
        .id(pairing.getId())
        .state(pairing.getState().name())
        .orderId(pairing.getOrderId())
        .updatedAt(pairing.getUpdatedAt())
        .build();
  }

  @Override
  @Transactional
  public CancelPairingResponse cancelPairing(UUID id, CancelPairingRequest request) {
    Pairing pairing = findPairingById(id);

    if (!CANCELLABLE_STATES.contains(pairing.getState())) {
      throw new CannotCancelPairingException(pairing.getState());
    }

    pairing.setState(PairingState.CANCELLED);
    pairing.setCancelledBy(request.getCancelledBy());
    pairing.setCancelReason(request.getReason());
    redisRepository.blacklistToken(pairing.getToken());
    pairingRepository.save(pairing);

    return CancelPairingResponse.builder()
        .id(pairing.getId())
        .state(pairing.getState().name())
        .cancelledBy(pairing.getCancelledBy().name())
        .reason(pairing.getCancelReason())
        .updatedAt(pairing.getUpdatedAt())
        .build();
  }

  @Override
  @Transactional
  public CompletePairingResponse completePairing(UUID id, CompletePairingRequest request) {
    Pairing pairing = findPairingById(id);

    if (pairing.getState() != PairingState.CONFIRMED) {
      throw new InvalidPairingStateException(PairingState.CONFIRMED, pairing.getState());
    }

    pairing.setState(PairingState.COMPLETED);
    pairing.setOrderId(request.getOrderId());
    pairing.setTokenBlacklisted(true);
    redisRepository.blacklistToken(pairing.getToken());
    pairingRepository.save(pairing);

    return CompletePairingResponse.builder()
        .id(pairing.getId())
        .state(pairing.getState().name())
        .orderId(pairing.getOrderId())
        .tokenBlacklisted(true)
        .updatedAt(pairing.getUpdatedAt())
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public FindByTokenResponse findByToken(String token) {
    Pairing pairing = pairingRepository.findByToken(token)
        .orElseThrow(() -> new TokenNotFoundException(token));

    return FindByTokenResponse.builder()
        .id(pairing.getId())
        .state(pairing.getState().name())
        .merchantId(pairing.getMerchantId())
        .terminalId(pairing.getTerminalId())
        .customerId(pairing.getCustomerId())
        .expiresAt(pairing.getExpiresAt())
        .build();
  }

  // ── Helpers ──────────────────────────────────────────────────────────────

  private Pairing findPairingById(UUID id) {
    return pairingRepository.findById(id)
        .orElseThrow(() -> new PairingNotFoundException(id));
  }

  private String generateToken() {
    String uuid = UUID.randomUUID().toString().replace("-", "").toUpperCase();
    return "TKN-" + uuid.substring(0, 12);
  }
}
