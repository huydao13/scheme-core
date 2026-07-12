package com.twint.scheme.merchant.service;

import com.twint.scheme.merchant.dto.request.AddTerminalRequest;
import com.twint.scheme.merchant.dto.request.CreateMerchantRequest;
import com.twint.scheme.merchant.dto.request.UpdateConfirmationFlagRequest;
import com.twint.scheme.merchant.dto.request.UpdateMerchantStatusRequest;
import com.twint.scheme.merchant.dto.request.UpdateTerminalStatusRequest;
import com.twint.scheme.merchant.dto.response.MerchantConfirmationResponse;
import com.twint.scheme.merchant.dto.response.MerchantResolveResponse;
import com.twint.scheme.merchant.dto.response.MerchantResponse;
import com.twint.scheme.merchant.dto.response.MerchantStatusResponse;
import com.twint.scheme.merchant.dto.response.TerminalResponse;
import com.twint.scheme.merchant.dto.response.TerminalStatusResponse;
import com.twint.scheme.merchant.entity.Merchant;
import com.twint.scheme.merchant.entity.Terminal;
import com.twint.scheme.merchant.enumeration.MerchantStatus;
import com.twint.scheme.merchant.enumeration.TerminalStatus;
import com.twint.scheme.merchant.exception.DuplicateTerminalCodeException;
import com.twint.scheme.merchant.exception.MerchantNotFoundException;
import com.twint.scheme.merchant.exception.MerchantSuspendedException;
import com.twint.scheme.merchant.exception.TerminalNotFoundException;
import com.twint.scheme.merchant.repository.MerchantRepository;
import com.twint.scheme.merchant.repository.TerminalRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MerchantServiceImpl implements MerchantService {
  private final MerchantRepository merchantRepository;
  private final TerminalRepository terminalRepository;

  @Override
  @Transactional
  public MerchantResponse createMerchant(CreateMerchantRequest request) {
    Merchant merchant = Merchant.builder()
        .name(request.getName())
        .mcc(request.getMcc())
        .confirmationFlag(request.isConfirmationFlag())
        .status(MerchantStatus.ACTIVE)
        .build();
    merchantRepository.save(merchant);
    return toResponse(merchant);
  }

  @Override
  @Transactional(readOnly = true)
  public MerchantResponse getMerchant(UUID id) {
    return toResponse(findMerchantById(id));
  }

  @Override
  @Transactional
  public MerchantStatusResponse updateStatus(UUID id, UpdateMerchantStatusRequest request) {
    Merchant merchant = findMerchantById(id);
    merchant.setStatus(request.getStatus());
    merchantRepository.save(merchant);
    return MerchantStatusResponse.builder()
        .id(merchant.getId())
        .status(merchant.getStatus().name())
        .updatedAt(merchant.getUpdatedAt())
        .build();
  }

  @Override
  @Transactional
  public MerchantConfirmationResponse updateConfirmationFlag(UUID id, UpdateConfirmationFlagRequest request) {
    Merchant merchant = findMerchantById(id);
    merchant.setConfirmationFlag(request.getConfirmationFlag());
    merchantRepository.save(merchant);
    return MerchantConfirmationResponse.builder()
        .id(merchant.getId())
        .confirmationFlag(merchant.isConfirmationFlag())
        .updatedAt(merchant.getUpdatedAt())
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public MerchantResolveResponse resolve(String terminalCode) {
    Terminal terminal = terminalRepository
        .findByTerminalCodeAndStatus(terminalCode, TerminalStatus.ACTIVE)
        .orElseThrow(() -> new TerminalNotFoundException(terminalCode));

    Merchant merchant = terminal.getMerchant();

    if (merchant.getStatus() == MerchantStatus.SUSPENDED) {
      throw new MerchantSuspendedException();
    }

    return MerchantResolveResponse.builder()
        .merchantId(merchant.getId())
        .merchantName(merchant.getName())
        .merchantStatus(merchant.getStatus().name())
        .confirmationFlag(merchant.isConfirmationFlag())
        .terminalId(terminal.getId())
        .terminalCode(terminal.getTerminalCode())
        .terminalType(terminal.getType().name())
        .terminalStatus(terminal.getStatus().name())
        .build();
  }

  @Override
  @Transactional
  public TerminalResponse addTerminal(UUID merchantId, AddTerminalRequest request) {
    Merchant merchant = findMerchantById(merchantId);

    if (terminalRepository.existsByTerminalCode(request.getTerminalCode())) {
      throw new DuplicateTerminalCodeException(request.getTerminalCode());
    }

    Terminal terminal = Terminal.builder()
        .merchant(merchant)
        .terminalCode(request.getTerminalCode())
        .type(request.getType())
        .status(TerminalStatus.ACTIVE)
        .build();
    terminalRepository.save(terminal);
    return toTerminalResponse(terminal);
  }

  @Override
  @Transactional(readOnly = true)
  public TerminalResponse getTerminal(UUID merchantId, UUID terminalId) {
    Terminal terminal = terminalRepository.findByIdAndMerchantId(terminalId, merchantId)
        .orElseThrow(() -> new TerminalNotFoundException(terminalId));
    return toTerminalResponse(terminal);
  }

  @Override
  @Transactional
  public TerminalStatusResponse updateTerminalStatus(UUID merchantId, UUID terminalId,
      UpdateTerminalStatusRequest request) {
    Terminal terminal = terminalRepository.findByIdAndMerchantId(terminalId, merchantId)
        .orElseThrow(() -> new TerminalNotFoundException(terminalId));
    terminal.setStatus(request.getStatus());
    terminalRepository.save(terminal);
    return TerminalStatusResponse.builder()
        .id(terminal.getId())
        .status(terminal.getStatus().name())
        .updatedAt(terminal.getUpdatedAt())
        .build();
  }

  // ── Helpers ──────────────────────────────────────────────────────────────

  private Merchant findMerchantById(UUID id) {
    return merchantRepository.findById(id)
        .orElseThrow(() -> new MerchantNotFoundException(id));
  }

  private MerchantResponse toResponse(Merchant m) {
    List<TerminalResponse> terminals = m.getTerminals() == null ? List.of() :
        m.getTerminals().stream().map(this::toTerminalResponse).toList();
    return MerchantResponse.builder()
        .id(m.getId())
        .name(m.getName())
        .mcc(m.getMcc())
        .confirmationFlag(m.isConfirmationFlag())
        .status(m.getStatus().name())
        .createdAt(m.getCreatedAt())
        .updatedAt(m.getUpdatedAt())
        .terminals(terminals)
        .build();
  }

  private TerminalResponse toTerminalResponse(Terminal t) {
    return TerminalResponse.builder()
        .id(t.getId())
        .merchantId(t.getMerchant().getId())
        .terminalCode(t.getTerminalCode())
        .type(t.getType().name())
        .status(t.getStatus().name())
        .createdAt(t.getCreatedAt())
        .updatedAt(t.getUpdatedAt())
        .build();
  }
}
