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
import java.util.UUID;

public interface MerchantService {
  MerchantResponse createMerchant(CreateMerchantRequest request);
  MerchantResponse getMerchant(UUID id);
  MerchantStatusResponse updateStatus(UUID id, UpdateMerchantStatusRequest request);
  MerchantConfirmationResponse updateConfirmationFlag(UUID id, UpdateConfirmationFlagRequest request);
  MerchantResolveResponse resolve(String terminalCode);
  TerminalResponse addTerminal(UUID merchantId, AddTerminalRequest request);
  TerminalResponse getTerminal(UUID merchantId, UUID terminalId);
  TerminalStatusResponse updateTerminalStatus(UUID merchantId, UUID terminalId, UpdateTerminalStatusRequest request);
}
