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
import com.twint.scheme.customer.entity.Customer;
import com.twint.scheme.customer.entity.Device;
import com.twint.scheme.customer.entity.FinancialAccount;
import com.twint.scheme.customer.enumeration.CustomerStatus;
import com.twint.scheme.customer.enumeration.DeviceStatus;
import com.twint.scheme.customer.enumeration.KycStatus;
import com.twint.scheme.customer.exception.CustomerNotFoundException;
import com.twint.scheme.customer.exception.DeviceLimitExceededException;
import com.twint.scheme.customer.exception.DuplicatePhoneException;
import com.twint.scheme.customer.exception.ResourceNotFoundException;
import com.twint.scheme.customer.repository.CustomerRepository;
import com.twint.scheme.customer.repository.DeviceRepository;
import com.twint.scheme.customer.repository.FinancialAccountRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
  private static final int MAX_ACTIVE_DEVICES = 3;
  private static final long DAILY_P2P_LIMIT = 5_000_000L;

  private final CustomerRepository customerRepository;
  private final DeviceRepository deviceRepository;
  private final FinancialAccountRepository financialAccountRepository;

  @Override
  @Transactional
  public CustomerResponse createCustomer(CreateCustomerRequest request) {
    if (customerRepository.existsByPhoneNumber(request.getPhoneNumber())) {
      throw new DuplicatePhoneException(request.getPhoneNumber());
    }
    Customer customer = Customer.builder()
        .phoneNumber(request.getPhoneNumber())
        .alias(request.getAlias())
        .kycStatus(KycStatus.PENDING)
        .status(CustomerStatus.ACTIVE)
        .build();
    customerRepository.save(customer);
    return toResponse(customer);
  }

  @Override
  @Transactional(readOnly = true)
  public CustomerResponse getCustomer(UUID id) {
    Customer customer = findCustomerById(id);
    return toResponse(customer);
  }

  @Override
  @Transactional
  public CustomerResponse updateStatus(UUID id, UpdateCustomerStatusRequest request) {
    Customer customer = findCustomerById(id);
    customer.setStatus(request.getStatus());
    customerRepository.save(customer);
    return toResponse(customer);
  }

  @Override
  @Transactional
  public CustomerResponse updateKyc(UUID id, UpdateKycRequest request) {
    Customer customer = findCustomerById(id);
    customer.setKycStatus(request.getKycStatus());
    customerRepository.save(customer);
    return toResponse(customer);
  }

  @Override
  @Transactional(readOnly = true)
  public CustomerResponse resolveByPhone(String phone) {
    Customer customer = customerRepository.findByPhoneNumber(phone)
        .orElseThrow(() -> new CustomerNotFoundException(phone));
    return toResponse(customer);
  }

  @Override
  @Transactional(readOnly = true)
  public P2pCheckResponse checkP2pEligibility(UUID id, P2pCheckRequest request) {
    Customer customer = findCustomerById(id);

    if (customer.getStatus() != CustomerStatus.ACTIVE) {
      return P2pCheckResponse.builder()
          .eligible(false)
          .build();
    }
    if (customer.getKycStatus() != KycStatus.VERIFIED) {
      return P2pCheckResponse.builder()
          .eligible(false)
          .build();
    }

    FinancialAccount defaultAccount = financialAccountRepository
        .findByCustomerIdAndIsP2pDefaultTrue(id)
        .orElse(null);

    if (defaultAccount == null) {
      return P2pCheckResponse.builder()
          .eligible(false)
          .build();
    }

    // TODO: tích hợp daily limit tracking thực tế (Redis hoặc DB)
    long dailyLimitRemaining = DAILY_P2P_LIMIT - request.getAmount();

    return P2pCheckResponse.builder()
        .eligible(true)
        .p2pDefaultAccountId(defaultAccount.getId())
        .dailyLimitRemaining(dailyLimitRemaining)
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public P2pRecipientResponse resolveP2pRecipient(String phone) {
    Customer customer = customerRepository.findByPhoneNumber(phone)
        .orElseThrow(() -> new CustomerNotFoundException(phone));

    FinancialAccount defaultAccount = financialAccountRepository
        .findByCustomerIdAndIsP2pDefaultTrue(customer.getId())
        .orElseThrow(() -> new CustomerNotFoundException(
            "No active customer with P2P default account for phone " + phone));

    return P2pRecipientResponse.builder()
        .customerId(customer.getId())
        .alias(customer.getAlias())
        .p2pDefaultAccountId(defaultAccount.getId())
        .status(customer.getStatus().name())
        .build();
  }

  @Override
  @Transactional
  public DeviceResponse addDevice(UUID customerId, AddDeviceRequest request) {
    findCustomerById(customerId);

    long activeDeviceCount = deviceRepository.countByCustomerIdAndStatus(customerId, DeviceStatus.ACTIVE);
    if (activeDeviceCount >= MAX_ACTIVE_DEVICES) {
      throw new DeviceLimitExceededException();
    }

    Customer customer = findCustomerById(customerId);
    Device device = Device.builder()
        .customer(customer)
        .fingerprint(request.getFingerprint())
        .certificate(request.getCertificate())
        .status(DeviceStatus.ACTIVE)
        .isActive(true)
        .build();
    deviceRepository.save(device);
    return toDeviceResponse(device);
  }

  @Override
  @Transactional
  public DeviceResponse revokeDevice(UUID customerId, UUID deviceId) {
    Device device = deviceRepository.findByIdAndCustomerId(deviceId, customerId)
        .orElseThrow(() -> new ResourceNotFoundException("Device", deviceId));

    device.setStatus(DeviceStatus.REVOKED);
    device.setActive(false);
    device.setRevokedAt(Instant.now());
    deviceRepository.save(device);
    return toDeviceResponse(device);
  }

  @Override
  @Transactional
  public FinancialAccountResponse addFinancialAccount(UUID customerId, AddFinancialAccountRequest request) {
    Customer customer = findCustomerById(customerId);

    FinancialAccount fa = FinancialAccount.builder()
        .customer(customer)
        .iban(request.getIban())
        .bankCode(request.getBankCode())
        .issuerId(request.getIssuerId())
        .isP2pDefault(false)
        .status("ACTIVE")
        .build();
    financialAccountRepository.save(fa);
    return toFaResponse(fa);
  }

  @Override
  @Transactional
  public FinancialAccountResponse setP2pDefault(UUID customerId, UUID faId) {
    findCustomerById(customerId);

    FinancialAccount fa = financialAccountRepository.findByIdAndCustomerId(faId, customerId)
        .orElseThrow(() -> new ResourceNotFoundException("FinancialAccount", faId));

    // unset tất cả default cũ rồi set cái mới
    financialAccountRepository.clearP2pDefaultForCustomer(customerId);
    fa.setP2pDefault(true);
    financialAccountRepository.save(fa);
    return toFaResponse(fa);
  }

  // ── Helpers ──────────────────────────────────────────────────────────────

  private Customer findCustomerById(UUID id) {
    return customerRepository.findById(id)
        .orElseThrow(() -> new CustomerNotFoundException(id));
  }

  private CustomerResponse toResponse(Customer c) {
    List<DeviceResponse> devices = c.getDevices() == null ? List.of() :
        c.getDevices().stream().map(this::toDeviceResponse).toList();

    List<FinancialAccountResponse> fas = c.getFinancialAccounts() == null ? List.of() :
        c.getFinancialAccounts().stream().map(this::toFaResponse).toList();

    return CustomerResponse.builder()
        .id(c.getId())
        .phoneNumber(c.getPhoneNumber())
        .alias(c.getAlias())
        .kycStatus(c.getKycStatus().name())
        .status(c.getStatus().name())
        .createdAt(c.getCreatedAt())
        .updatedAt(c.getUpdatedAt())
        .devices(devices)
        .financialAccounts(fas)
        .build();
  }

  private DeviceResponse toDeviceResponse(Device d) {
    return DeviceResponse.builder()
        .id(d.getId())
        .fingerprint(d.getFingerprint())
        .status(d.getStatus().name())
        .isActive(d.isActive())
        .registeredAt(d.getRegisteredAt())
        .revokedAt(d.getRevokedAt())
        .build();
  }

  private FinancialAccountResponse toFaResponse(FinancialAccount fa) {
    return FinancialAccountResponse.builder()
        .id(fa.getId())
        .iban(fa.getIban())
        .bankCode(fa.getBankCode())
        .issuerId(fa.getIssuerId())
        .isP2pDefault(fa.isP2pDefault())
        .status(fa.getStatus())
        .createdAt(fa.getCreatedAt())
        .build();
  }
}
