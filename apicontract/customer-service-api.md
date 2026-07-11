## API Contract — customer-service (:8083)

Base URL: `http://localhost:8083`
Package: `com.twint.scheme.customer`

---

### Tổng quan endpoints

| Method | Path | Mô tả | Auth |
|---|---|---|---|
| POST | `/v1/customers` | Tạo customer account | ADMIN |
| GET | `/v1/customers/{id}` | Lấy thông tin customer | CUSTOMER, ADMIN |
| PUT | `/v1/customers/{id}/status` | Block/unblock customer | ADMIN |
| PUT | `/v1/customers/{id}/kyc` | Cập nhật KYC status | ADMIN |
| GET | `/v1/customers/resolve` | Resolve customer theo phone | INTERNAL |
| POST | `/v1/customers/{id}/p2p/check` | Check P2P eligibility | INTERNAL |
| GET | `/v1/customers/p2p-recipient` | Resolve receiver P2P | INTERNAL |
| POST | `/v1/customers/{id}/devices` | Thêm device | CUSTOMER |
| DELETE | `/v1/customers/{id}/devices/{deviceId}` | Revoke device | CUSTOMER, ADMIN |
| POST | `/v1/customers/{id}/financial-accounts` | Thêm financial account | CUSTOMER |
| PUT | `/v1/customers/{id}/financial-accounts/{faId}/p2p-default` | Set P2P default | CUSTOMER |

---

### POST `/v1/customers`
Tạo mới customer account.

**Request Header:**
```
Authorization: Bearer {admin_jwt}
Content-Type: application/json
```

**Request Body:**
```json
{
  "phoneNumber": "0901234567",
  "alias": "Nguyen Van A"
}
```

**Response 201 Created:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "phoneNumber": "0901234567",
  "alias": "Nguyen Van A",
  "kycStatus": "PENDING",
  "status": "ACTIVE",
  "createdAt": "2025-07-11T10:00:00Z"
}
```

**Response 409 Conflict:**
```json
{
  "type": "https://api.twint.scheme/errors/duplicate-phone",
  "title": "Phone Number Already Exists",
  "status": 409,
  "detail": "Phone number 0901234567 is already registered",
  "traceId": "4bf92f3577b34da6a3ce929d0e0e4736"
}
```

---

### GET `/v1/customers/{id}`
Lấy thông tin đầy đủ của customer.

**Request Header:**
```
Authorization: Bearer {jwt}
X-User-Id: {customerId}
```

**Response 200 OK:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "phoneNumber": "0901234567",
  "alias": "Nguyen Van A",
  "kycStatus": "VERIFIED",
  "status": "ACTIVE",
  "createdAt": "2025-07-11T10:00:00Z",
  "devices": [
    {
      "id": "device-uuid",
      "fingerprint": "abc123def456",
      "status": "ACTIVE",
      "isActive": true,
      "registeredAt": "2025-07-11T10:00:00Z"
    }
  ],
  "financialAccounts": [
    {
      "id": "fa-uuid",
      "iban": "CH9300762011623852957",
      "bankCode": "VCB",
      "issuerId": "issuer-001",
      "isP2pDefault": true,
      "status": "ACTIVE"
    }
  ]
}
```

**Response 404 Not Found:**
```json
{
  "type": "https://api.twint.scheme/errors/customer-not-found",
  "title": "Customer Not Found",
  "status": 404,
  "detail": "Customer 550e8400 not found",
  "traceId": "4bf92f3577b34da6a3ce929d0e0e4736"
}
```

---

### PUT `/v1/customers/{id}/status`
Block hoặc unblock customer. Chỉ ADMIN.

**Request Body:**
```json
{
  "status": "BLOCKED",
  "reason": "Suspicious activity detected"
}
```

**Response 200 OK:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "status": "BLOCKED",
  "updatedAt": "2025-07-11T10:00:00Z"
}
```

---

### PUT `/v1/customers/{id}/kyc`
Cập nhật KYC status. Chỉ ADMIN.

**Request Body:**
```json
{
  "kycStatus": "VERIFIED"
}
```

**Response 200 OK:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "kycStatus": "VERIFIED",
  "updatedAt": "2025-07-11T10:00:00Z"
}
```

---

### GET `/v1/customers/resolve?phone={phoneNumber}`
Resolve customer theo số điện thoại.
Được gọi bởi order-service trong P2P flow (INTERNAL).

**Request Header:**
```
X-Internal-Service: order-service
```

**Response 200 OK:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "alias": "Nguyen Van A",
  "status": "ACTIVE",
  "kycStatus": "VERIFIED"
}
```

**Response 404 Not Found:**
```json
{
  "type": "https://api.twint.scheme/errors/customer-not-found",
  "title": "Customer Not Found",
  "status": 404,
  "detail": "No customer registered with phone 0901234567",
  "traceId": "4bf92f3577b34da6a3ce929d0e0e4736"
}
```

---

### POST `/v1/customers/{id}/p2p/check`
Kiểm tra customer có đủ điều kiện thực hiện P2P không.
Được gọi bởi order-service trước khi tạo P2P order (INTERNAL).

**Request Body:**
```json
{
  "amount": 150000,
  "currency": "VND"
}
```

**Response 200 OK:**
```json
{
  "eligible": true,
  "p2pDefaultAccountId": "fa-uuid",
  "dailyLimitRemaining": 4850000
}
```

**Response 422 Unprocessable Entity:**
```json
{
  "type": "https://api.twint.scheme/errors/not-eligible",
  "title": "Customer Not Eligible",
  "status": 422,
  "detail": "Customer KYC status is PENDING",
  "reasonCode": "KYC_NOT_VERIFIED",
  "traceId": "4bf92f3577b34da6a3ce929d0e0e4736"
}
```

**reasonCode có thể có:**
| reasonCode | Mô tả |
|---|---|
| `KYC_NOT_VERIFIED` | KYC chưa được xác minh |
| `ACCOUNT_BLOCKED` | Tài khoản bị block |
| `NO_P2P_DEFAULT_ACCOUNT` | Chưa set tài khoản P2P mặc định |
| `DAILY_LIMIT_EXCEEDED` | Vượt hạn mức ngày |

---

### GET `/v1/customers/p2p-recipient?phone={phoneNumber}`
Resolve receiver trong P2P flow — trả về thông tin cần thiết để thực hiện transfer.
Được gọi bởi order-service (INTERNAL).

**Response 200 OK:**
```json
{
  "customerId": "receiver-uuid",
  "alias": "Tran Thi B",
  "p2pDefaultAccountId": "fa-receiver-uuid",
  "status": "ACTIVE"
}
```

**Response 404 Not Found:**
```json
{
  "type": "https://api.twint.scheme/errors/recipient-not-found",
  "title": "P2P Recipient Not Found",
  "status": 404,
  "detail": "No active customer with P2P default account for phone 0909999999",
  "traceId": "4bf92f3577b34da6a3ce929d0e0e4736"
}
```

---

### POST `/v1/customers/{id}/devices`
Đăng ký device mới cho customer.

**Request Body:**
```json
{
  "fingerprint": "abc123def456",
  "certificate": "-----BEGIN CERTIFICATE-----\n...\n-----END CERTIFICATE-----"
}
```

**Response 201 Created:**
```json
{
  "id": "device-uuid",
  "customerId": "550e8400-e29b-41d4-a716-446655440000",
  "fingerprint": "abc123def456",
  "status": "ACTIVE",
  "isActive": true,
  "registeredAt": "2025-07-11T10:00:00Z"
}
```

**Response 409 Conflict:**
```json
{
  "type": "https://api.twint.scheme/errors/device-limit",
  "title": "Device Limit Reached",
  "status": 409,
  "detail": "Customer already has 3 active devices",
  "traceId": "4bf92f3577b34da6a3ce929d0e0e4736"
}
```

---

### DELETE `/v1/customers/{id}/devices/{deviceId}`
Revoke device.

**Response 200 OK:**
```json
{
  "id": "device-uuid",
  "status": "REVOKED",
  "revokedAt": "2025-07-11T10:00:00Z"
}
```

---

### POST `/v1/customers/{id}/financial-accounts`
Thêm tài khoản ngân hàng.

**Request Body:**
```json
{
  "iban": "CH9300762011623852957",
  "bankCode": "VCB",
  "issuerId": "issuer-001"
}
```

**Response 201 Created:**
```json
{
  "id": "fa-uuid",
  "customerId": "550e8400-e29b-41d4-a716-446655440000",
  "iban": "CH9300762011623852957",
  "bankCode": "VCB",
  "issuerId": "issuer-001",
  "isP2pDefault": false,
  "status": "ACTIVE",
  "createdAt": "2025-07-11T10:00:00Z"
}
```

---

### PUT `/v1/customers/{id}/financial-accounts/{faId}/p2p-default`
Set tài khoản làm P2P default.
Tự động unset tài khoản default cũ.

**Response 200 OK:**
```json
{
  "id": "fa-uuid",
  "isP2pDefault": true,
  "updatedAt": "2025-07-11T10:00:00Z"
}
```

---

### Error codes tổng hợp

| HTTP Status | Error Type | Mô tả |
|---|---|---|
| 400 | `bad-request` | Request body không hợp lệ |
| 401 | `unauthorized` | JWT missing hoặc invalid |
| 403 | `forbidden` | Không đủ quyền |
| 404 | `customer-not-found` | Customer không tồn tại |
| 404 | `recipient-not-found` | Receiver P2P không tồn tại |
| 409 | `duplicate-phone` | Số điện thoại đã đăng ký |
| 409 | `device-limit` | Quá số lượng device cho phép |
| 422 | `not-eligible` | Customer không đủ điều kiện P2P |
| 500 | `internal-error` | Lỗi hệ thống |
