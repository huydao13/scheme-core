package com.twint.scheme.pairing.controller;

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
import com.twint.scheme.pairing.service.PairingService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/pairings")
@RequiredArgsConstructor
public class PairingController {
  private final PairingService pairingService;

  // POST /v1/pairings — INTERNAL (order-service)
  @PostMapping
  public ResponseEntity<PairingResponse> createPairing(
      @Valid @RequestBody CreatePairingRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(pairingService.createPairing(request));
  }

  // GET /v1/pairings/{id} — MERCHANT, INTERNAL
  @GetMapping("/{id}")
  public ResponseEntity<PairingResponse> getPairing(@PathVariable UUID id) {
    return ResponseEntity.ok(pairingService.getPairing(id));
  }

  // POST /v1/pairings/scan — CUSTOMER
  @PostMapping("/scan")
  public ResponseEntity<ScanPairingResponse> scanPairing(
      @Valid @RequestBody ScanPairingRequest request,
      @RequestHeader(value = "X-Customer-Id", required = false) UUID customerId) {
    // TODO: lấy customerId từ JWT thật
    UUID cid = customerId != null ? customerId : UUID.randomUUID();
    return ResponseEntity.ok(pairingService.scanPairing(request, cid));
  }

  // POST /v1/pairings/{id}/confirm — INTERNAL
  @PostMapping("/{id}/confirm")
  public ResponseEntity<ConfirmPairingResponse> confirmPairing(
      @PathVariable UUID id,
      @Valid @RequestBody ConfirmPairingRequest request) {
    return ResponseEntity.ok(pairingService.confirmPairing(id, request));
  }

  // POST /v1/pairings/{id}/cancel — CUSTOMER, MERCHANT
  @PostMapping("/{id}/cancel")
  public ResponseEntity<CancelPairingResponse> cancelPairing(
      @PathVariable UUID id,
      @Valid @RequestBody CancelPairingRequest request) {
    return ResponseEntity.ok(pairingService.cancelPairing(id, request));
  }

  // POST /v1/pairings/{id}/complete — INTERNAL
  @PostMapping("/{id}/complete")
  public ResponseEntity<CompletePairingResponse> completePairing(
      @PathVariable UUID id,
      @Valid @RequestBody CompletePairingRequest request) {
    return ResponseEntity.ok(pairingService.completePairing(id, request));
  }

  // GET /v1/pairings/find-by-token — INTERNAL
  @GetMapping("/find-by-token")
  public ResponseEntity<FindByTokenResponse> findByToken(
      @RequestParam String token) {
    return ResponseEntity.ok(pairingService.findByToken(token));
  }
}
