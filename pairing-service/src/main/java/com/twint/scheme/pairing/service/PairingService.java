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
import java.util.UUID;

public interface PairingService {
  PairingResponse createPairing(CreatePairingRequest request);
  PairingResponse getPairing(UUID id);
  ScanPairingResponse scanPairing(ScanPairingRequest request, UUID customerId);
  ConfirmPairingResponse confirmPairing(UUID id, ConfirmPairingRequest request);
  CancelPairingResponse cancelPairing(UUID id, CancelPairingRequest request);
  CompletePairingResponse completePairing(UUID id, CompletePairingRequest request);
  FindByTokenResponse findByToken(String token);
}
