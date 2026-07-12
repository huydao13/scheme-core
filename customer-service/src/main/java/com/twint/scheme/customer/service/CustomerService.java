package com.twint.scheme.customer.service;

import com.twint.scheme.customer.dto.request.AddDeviceRequest;
import com.twint.scheme.customer.dto.request.AddFinancialAccountRequest;
import com.twint.scheme.customer.dto.request.CreateCustomerRequest;
import com.twint.scheme.customer.dto.request.P2pCheckRequest;
import com.twint.scheme.customer.dto.request.UpdateCustomerStatusRequest;
import com.twint.scheme.customer.dto.request.UpdateKycRequest;
import com.twint.scheme.customer.dto.response.CustomerResponse;
import com.twint.scheme.customer.dto.response.DeviceResponse;
import com.twint.scheme.customer.dto.response.FinancialAccountResponse;
import com.twint.scheme.customer.dto.response.P2pCheckResponse;
import com.twint.scheme.customer.dto.response.P2pRecipientResponse;
import java.util.UUID;

public interface CustomerService {
  CustomerResponse createCustomer(CreateCustomerRequest request);
  CustomerResponse getCustomer(UUID id);
  CustomerResponse updateStatus(UUID id, UpdateCustomerStatusRequest request);
  CustomerResponse updateKyc(UUID id, UpdateKycRequest request);
  CustomerResponse resolveByPhone(String phone);
  P2pCheckResponse checkP2pEligibility(UUID id, P2pCheckRequest request);
  P2pRecipientResponse resolveP2pRecipient(String phone);
  DeviceResponse addDevice(UUID customerId, AddDeviceRequest request);
  DeviceResponse revokeDevice(UUID customerId, UUID deviceId);
  FinancialAccountResponse addFinancialAccount(UUID customerId, AddFinancialAccountRequest request);
  FinancialAccountResponse setP2pDefault(UUID customerId, UUID faId);
}
