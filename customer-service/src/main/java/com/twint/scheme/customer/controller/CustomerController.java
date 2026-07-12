package com.twint.scheme.customer.controller;

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
import com.twint.scheme.customer.service.CustomerService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/customers")
@RequiredArgsConstructor
public class CustomerController {
  private final CustomerService customerService;

  // POST /v1/customers — ADMIN
  @PostMapping
  public ResponseEntity<CustomerResponse> createCustomer(
      @Valid @RequestBody CreateCustomerRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(customerService.createCustomer(request));
  }

  // GET /v1/customers/{id} — CUSTOMER, ADMIN
  @GetMapping("/{id}")
  public ResponseEntity<CustomerResponse> getCustomer(@PathVariable UUID id) {
    return ResponseEntity.ok(customerService.getCustomer(id));
  }

  // PUT /v1/customers/{id}/status — ADMIN
  @PutMapping("/{id}/status")
  public ResponseEntity<CustomerResponse> updateStatus(
      @PathVariable UUID id,
      @Valid @RequestBody UpdateCustomerStatusRequest request) {
    return ResponseEntity.ok(customerService.updateStatus(id, request));
  }

  // PUT /v1/customers/{id}/kyc — ADMIN
  @PutMapping("/{id}/kyc")
  public ResponseEntity<CustomerResponse> updateKyc(
      @PathVariable UUID id,
      @Valid @RequestBody UpdateKycRequest request) {
    return ResponseEntity.ok(customerService.updateKyc(id, request));
  }

  // GET /v1/customers/resolve?phone= — INTERNAL
  @GetMapping("/resolve")
  public ResponseEntity<CustomerResponse> resolveByPhone(
      @RequestParam String phone) {
    return ResponseEntity.ok(customerService.resolveByPhone(phone));
  }

  // POST /v1/customers/{id}/p2p/check — INTERNAL
  @PostMapping("/{id}/p2p/check")
  public ResponseEntity<P2pCheckResponse> checkP2pEligibility(
      @PathVariable UUID id,
      @Valid @RequestBody P2pCheckRequest request) {
    return ResponseEntity.ok(customerService.checkP2pEligibility(id, request));
  }

  // GET /v1/customers/p2p-recipient?phone= — INTERNAL
  @GetMapping("/p2p-recipient")
  public ResponseEntity<P2pRecipientResponse> resolveP2pRecipient(
      @RequestParam String phone) {
    return ResponseEntity.ok(customerService.resolveP2pRecipient(phone));
  }

  // POST /v1/customers/{id}/devices — CUSTOMER
  @PostMapping("/{id}/devices")
  public ResponseEntity<DeviceResponse> addDevice(
      @PathVariable UUID id,
      @Valid @RequestBody AddDeviceRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(customerService.addDevice(id, request));
  }

  // DELETE /v1/customers/{id}/devices/{deviceId} — CUSTOMER, ADMIN
  @DeleteMapping("/{id}/devices/{deviceId}")
  public ResponseEntity<DeviceResponse> revokeDevice(
      @PathVariable UUID id,
      @PathVariable UUID deviceId) {
    return ResponseEntity.ok(customerService.revokeDevice(id, deviceId));
  }

  // POST /v1/customers/{id}/financial-accounts — CUSTOMER
  @PostMapping("/{id}/financial-accounts")
  public ResponseEntity<FinancialAccountResponse> addFinancialAccount(
      @PathVariable UUID id,
      @Valid @RequestBody AddFinancialAccountRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(customerService.addFinancialAccount(id, request));
  }

  // PUT /v1/customers/{id}/financial-accounts/{faId}/p2p-default — CUSTOMER
  @PutMapping("/{id}/financial-accounts/{faId}/p2p-default")
  public ResponseEntity<FinancialAccountResponse> setP2pDefault(
      @PathVariable UUID id,
      @PathVariable UUID faId) {
    return ResponseEntity.ok(customerService.setP2pDefault(id, faId));
  }
}
