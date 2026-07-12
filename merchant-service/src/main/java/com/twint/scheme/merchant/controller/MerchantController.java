package com.twint.scheme.merchant.controller;

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
import com.twint.scheme.merchant.service.MerchantService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/merchants")
@RequiredArgsConstructor
public class MerchantController {
  private final MerchantService merchantService;

  // POST /v1/merchants — ADMIN
  @PostMapping
  public ResponseEntity<MerchantResponse> createMerchant(
      @Valid @RequestBody CreateMerchantRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(merchantService.createMerchant(request));
  }

  // GET /v1/merchants/{id} — MERCHANT, ADMIN
  @GetMapping("/{id}")
  public ResponseEntity<MerchantResponse> getMerchant(@PathVariable UUID id) {
    return ResponseEntity.ok(merchantService.getMerchant(id));
  }

  // PUT /v1/merchants/{id}/status — ADMIN
  @PutMapping("/{id}/status")
  public ResponseEntity<MerchantStatusResponse> updateStatus(
      @PathVariable UUID id,
      @Valid @RequestBody UpdateMerchantStatusRequest request) {
    return ResponseEntity.ok(merchantService.updateStatus(id, request));
  }

  // PUT /v1/merchants/{id}/confirmation-flag — MERCHANT, ADMIN
  @PutMapping("/{id}/confirmation-flag")
  public ResponseEntity<MerchantConfirmationResponse> updateConfirmationFlag(
      @PathVariable UUID id,
      @Valid @RequestBody UpdateConfirmationFlagRequest request) {
    return ResponseEntity.ok(merchantService.updateConfirmationFlag(id, request));
  }

  // GET /v1/merchants/resolve?terminalCode= — INTERNAL
  @GetMapping("/resolve")
  public ResponseEntity<MerchantResolveResponse> resolve(
      @RequestParam String terminalCode) {
    return ResponseEntity.ok(merchantService.resolve(terminalCode));
  }

  // POST /v1/merchants/{id}/terminals — MERCHANT, ADMIN
  @PostMapping("/{id}/terminals")
  public ResponseEntity<TerminalResponse> addTerminal(
      @PathVariable UUID id,
      @Valid @RequestBody AddTerminalRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(merchantService.addTerminal(id, request));
  }

  // GET /v1/merchants/{id}/terminals/{terminalId} — MERCHANT, ADMIN
  @GetMapping("/{id}/terminals/{terminalId}")
  public ResponseEntity<TerminalResponse> getTerminal(
      @PathVariable UUID id,
      @PathVariable UUID terminalId) {
    return ResponseEntity.ok(merchantService.getTerminal(id, terminalId));
  }

  // PUT /v1/merchants/{id}/terminals/{terminalId}/status — MERCHANT, ADMIN
  @PutMapping("/{id}/terminals/{terminalId}/status")
  public ResponseEntity<TerminalStatusResponse> updateTerminalStatus(
      @PathVariable UUID id,
      @PathVariable UUID terminalId,
      @Valid @RequestBody UpdateTerminalStatusRequest request) {
    return ResponseEntity.ok(merchantService.updateTerminalStatus(id, terminalId, request));
  }
}
