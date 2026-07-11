## API Contract — order-service (:8081)

Base URL: `http://localhost:8081`
Package: `com.twint.scheme.order`

---

### Tổng quan endpoints

| Method | Path | Mô tả | Auth |
|---|---|---|---|
| POST | `/v1/orders` | Tạo merchant payment order | MERCHANT |
| POST | `/v1/orders/p2p/send` | Tạo P2P transfer order | INTERNAL |
| GET | `/v1/orders/{id}` | Lấy thông tin order | CUSTOMER, MERCHANT, ADMIN |
| GET | `/v1/orders` | Lấy lịch sử order | CUSTOMER, MERCHANT |
| POST | `/v1/orders/{id}/confirm` | Merchant confirm payment | MERCHANT |
| POST | `/v1/orders/{id}/cancel` | Huỷ order | CUSTOMER, MERCHANT |

---

### POST `/v1/orders`
Tạo merchant payment order.
Merchant gọi khi muốn bắt đầu nhận thanh toán.

**Request Header:**
```
X-Api-Key: {merchant_api_key}
X-Signature: hmac-sha256={signature}
X-Idempotency-Key: {uuid}
Content-Type: application/json
```

**Request Body:**
```json
{
  "terminalCode": "TRM-001",
  "amount": 150000,
  "currency": "VND",
  "description": "Coffee x2, Cake x1"
}
```

**Xử lý nội bộ:**
1. Validate idempotency key — check Redis `idempotency:{key}`
2. Resolve merchant + terminal qua merchant-service `GET /v1/merchants/resolve?terminalCode=`
3. Tạo Order `RECEIVED`, persist DB + write outbox
4. Gọi pairing-service `POST /v1/pairings` → nhận `pairingId + token + qrCode`
5. Trả response về merchant

**Response 201 Created:**
```json
{
  "orderId": "order-uuid",
  "type": "PAYMENT_IMMEDIATE",
  "state": "RECEIVED",
  "amount": 150000,
  "currency": "VND",
  "merchantId": "merchant-uuid",
  "terminalId": "terminal-uuid",
  "pairingId": "pairing-uuid",
  "qrCode": "data:image/png;base64,iVBORw0KGgoAAAANS...",
  "expiresAt": "2025-07-11T10:05:00Z",
  "createdAt": "2025-07-11T10:00:00Z"
}
```

**Response 409 Conflict — duplicate request:**
```json
{
  "type": "https://api.twint.scheme/errors/duplicate-request",
  "title": "Duplicate Request",
  "status": 409,
  "detail": "Order already created for this idempotency key",
  "existingOrderId": "order-uuid",
  "traceId": "4bf92f3577b34da6a3ce929d0e0e4736"
}
```

**Response 422 Unprocessable Entity:**
```json
{
  "type": "https://api.twint.scheme/errors/merchant-suspended",
  "title": "Merchant Suspended",
  "status": 422,
  "detail": "Merchant is currently suspended",
  "traceId": "4bf92f3577b34da6a3ce929d0e0e4736"
}
```

---

### POST `/v1/orders/p2p/send`
Tạo P2P transfer order.
Được gọi bởi smartphone-service (INTERNAL).

**Request Header:**
```
X-Internal-Service: smartphone-service
X-Idempotency-Key: {uuid}
X-User-Id: {senderId}
Content-Type: application/json
```

**Request Body:**
```json
{
  "receiverPhone": "0909999999",
  "amount": 150000,
  "currency": "VND",
  "description": "Trả tiền ăn trưa",
  "resendOrderUuid": null
}
```

**`resendOrderUuid`:** nếu không null → trả lại kết quả của order cũ (idempotency resend).

**Xử lý nội bộ:**
1. Check `resendOrderUuid` — nếu không null trả cached response
2. Check Redis idempotency key
3. Gọi customer-service:
    - `GET /v1/customers/{senderId}` — lấy sender info
    - `POST /v1/customers/{senderId}/p2p/check` — check eligibility
    - `GET /v1/customers/p2p-recipient?phone={receiverPhone}` — resolve receiver
4. Persist P2P Order `PENDING`
5. Gọi bank-core-service `POST /portfolios/{senderAccountId}/p2pMoney/reserveAndTransfer`
6. Update Order `PENDING → SUCCESSFUL` hoặc `PENDING → FAILED`
7. Write outbox, publish Kafka event

**Response 200 OK:**
```json
{
  "orderId": "order-uuid",
  "type": "P2P",
  "state": "SUCCESSFUL",
  "amount": 150000,
  "currency": "VND",
  "senderAccountId": "fa-sender-uuid",
  "receiverAccountId": "fa-receiver-uuid",
  "txnRef": "TXN-REF-001",
  "createdAt": "2025-07-11T10:00:00Z",
  "completedAt": "2025-07-11T10:00:01Z"
}
```

**Response 404 Not Found:**
```json
{
  "type": "https://api.twint.scheme/errors/recipient-not-found",
  "title": "Recipient Not Found",
  "status": 404,
  "detail": "No active customer with P2P account for phone 0909999999",
  "traceId": "4bf92f3577b34da6a3ce929d0e0e4736"
}
```

**Response 422 Unprocessable Entity:**
```json
{
  "type": "https://api.twint.scheme/errors/transfer-failed",
  "title": "Transfer Failed",
  "status": 422,
  "detail": "Insufficient funds in sender account",
  "reasonCode": "INSUFFICIENT_FUNDS",
  "traceId": "4bf92f3577b34da6a3ce929d0e0e4736"
}
```

**reasonCode có thể có:**
| reasonCode | Mô tả |
|---|---|
| `INSUFFICIENT_FUNDS` | Số dư không đủ |
| `SENDER_NOT_ELIGIBLE` | Sender không đủ điều kiện |
| `RECEIVER_NOT_FOUND` | Receiver không tồn tại |
| `DAILY_LIMIT_EXCEEDED` | Vượt hạn mức ngày |
| `BANK_ERROR` | Lỗi từ bank-core-service |

---

### GET `/v1/orders/{id}`
Lấy thông tin chi tiết order.

**Request Header:**
```
Authorization: Bearer {jwt}
```

**Response 200 OK — Merchant Payment:**
```json
{
  "orderId": "order-uuid",
  "type": "PAYMENT_IMMEDIATE",
  "state": "SUCCESSFUL",
  "amount": 150000,
  "currency": "VND",
  "merchantId": "merchant-uuid",
  "terminalId": "terminal-uuid",
  "pairingId": "pairing-uuid",
  "authCode": "AC-999",
  "txnRef": "TXN-REF-001",
  "cancelReason": null,
  "createdAt": "2025-07-11T10:00:00Z",
  "completedAt": "2025-07-11T10:00:45Z",
  "stateHistory": [
    {
      "fromState": null,
      "toState": "RECEIVED",
      "trigger": "MERCHANT_START_ORDER",
      "changedAt": "2025-07-11T10:00:00Z"
    },
    {
      "fromState": "RECEIVED",
      "toState": "PAIRED",
      "trigger": "APP_SCAN",
      "changedAt": "2025-07-11T10:00:15Z"
    },
    {
      "fromState": "PAIRED",
      "toState": "WAITING_BANK_AUTH",
      "trigger": "IMPLICIT_CONFIRM",
      "changedAt": "2025-07-11T10:00:20Z"
    },
    {
      "fromState": "WAITING_BANK_AUTH",
      "toState": "ADVICE_PENDING",
      "trigger": "AUTH_OK_FLAG_FALSE",
      "changedAt": "2025-07-11T10:00:25Z"
    },
    {
      "fromState": "ADVICE_PENDING",
      "toState": "SUCCESSFUL",
      "trigger": "CAPTURED_OK",
      "changedAt": "2025-07-11T10:00:45Z"
    }
  ]
}
```

**Response 200 OK — P2P:**
```json
{
  "orderId": "order-uuid",
  "type": "P2P",
  "state": "SUCCESSFUL",
  "amount": 150000,
  "currency": "VND",
  "senderAccountId": "fa-sender-uuid",
  "receiverAccountId": "fa-receiver-uuid",
  "txnRef": "TXN-REF-001",
  "cancelReason": null,
  "createdAt": "2025-07-11T10:00:00Z",
  "completedAt": "2025-07-11T10:00:01Z"
}
```

**Response 403 Forbidden:**
```json
{
  "type": "https://api.twint.scheme/errors/forbidden",
  "title": "Forbidden",
  "status": 403,
  "detail": "You do not have permission to view this order",
  "traceId": "4bf92f3577b34da6a3ce929d0e0e4736"
}
```

---

### GET `/v1/orders`
Lấy lịch sử order theo filter.

**Request Header:**
```
Authorization: Bearer {jwt}
```

**Query Parameters:**
| Param | Type | Required | Mô tả |
|---|---|---|---|
| `customerId` | uuid | false | Filter theo sender/receiver |
| `merchantId` | uuid | false | Filter theo merchant |
| `type` | string | false | `P2P`, `PAYMENT_IMMEDIATE` |
| `state` | string | false | Filter theo state |
| `page` | int | false | Default 0 |
| `size` | int | false | Default 20, max 100 |

**Response 200 OK:**
```json
{
  "content": [
    {
      "orderId": "order-uuid",
      "type": "P2P",
      "state": "SUCCESSFUL",
      "amount": 150000,
      "currency": "VND",
      "createdAt": "2025-07-11T10:00:00Z",
      "completedAt": "2025-07-11T10:00:01Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 45,
  "totalPages": 3
}
```

---

### POST `/v1/orders/{id}/confirm`
Merchant confirm payment thủ công.
Chỉ applicable khi `merchantConfirmationFlag = true`
và order ở state `WAITING_MERCHANT_UPDATE`.

**Request Header:**
```
X-Api-Key: {merchant_api_key}
X-Signature: hmac-sha256={signature}
```

**Xử lý nội bộ:**
1. Validate order ở state `WAITING_MERCHANT_UPDATE`
2. Update state → `ADVICE_PENDING`
3. Trả `202 Accepted` về merchant ngay
4. Async: gọi six-clearing-service `POST /finp/v1/advice`
5. Update state `ADVICE_PENDING → SUCCESSFUL` hoặc `FAILED_AFTER_CONFIRMATION`
6. Write outbox → publish Kafka event

**Response 202 Accepted:**
```json
{
  "orderId": "order-uuid",
  "state": "ADVICE_PENDING",
  "message": "Confirmation received, processing payment capture",
  "acceptedAt": "2025-07-11T10:00:30Z"
}
```

**Response 409 Conflict — sai state:**
```json
{
  "type": "https://api.twint.scheme/errors/invalid-order-state",
  "title": "Invalid Order State",
  "status": 409,
  "detail": "Order must be in WAITING_MERCHANT_UPDATE state to confirm, current state: ADVICE_PENDING",
  "traceId": "4bf92f3577b34da6a3ce929d0e0e4736"
}
```

---

### POST `/v1/orders/{id}/cancel`
Huỷ order.
Customer hoặc Merchant đều có thể huỷ ở các state cho phép.

**Request Header:**
```
Authorization: Bearer {jwt}
Content-Type: application/json
```

**Request Body:**
```json
{
  "reason": "CUSTOMER_CHANGED_MIND"
}
```

**Xử lý nội bộ:**
1. Validate state có thể huỷ (`PAIRED`, `WAITING_MERCHANT_UPDATE`)
2. Nếu có reservation (authCode không null) → gọi six-clearing-service `POST /finp/v1/cancellation`
3. Update state → `CANCELLED_BY_APP` hoặc `CANCELLED_BY_MERCHANT`
4. Gọi pairing-service `POST /v1/pairings/{pairingId}/cancel`
5. Write outbox → publish event

**Response 200 OK:**
```json
{
  "orderId": "order-uuid",
  "state": "CANCELLED_BY_APP",
  "cancelReason": "CUSTOMER_CHANGED_MIND",
  "cancelledAt": "2025-07-11T10:00:25Z"
}
```

**Response 409 Conflict — không thể huỷ:**
```json
{
  "type": "https://api.twint.scheme/errors/cannot-cancel-order",
  "title": "Cannot Cancel Order",
  "status": 409,
  "detail": "Order in SUCCESSFUL state cannot be cancelled",
  "traceId": "4bf92f3577b34da6a3ce929d0e0e4736"
}
```

---

### Kafka Events published

| Event Type | Trigger | Payload |
|---|---|---|
| `order.received` | Order tạo thành công | orderId, type, amount, merchantId |
| `order.paired` | App scan QR thành công | orderId, customerId, pairingId |
| `order.successful` | Payment captured | orderId, type, amount, senderAccountId, receiverAccountId |
| `order.failed` | Auth denied / transfer failed | orderId, reasonCode |
| `order.cancelled` | Customer/Merchant huỷ | orderId, cancelledBy, reason |
| `order.timeout` | Merchant không confirm đúng hạn | orderId, expiredAt |
| `order.failed_after_confirmation` | Advice fail sau khi reserve | orderId, authCode |

**Event payload mẫu:**
```json
{
  "eventType": "order.successful",
  "orderId": "order-uuid",
  "type": "PAYMENT_IMMEDIATE",
  "amount": 150000,
  "currency": "VND",
  "senderAccountId": "fa-sender-uuid",
  "receiverAccountId": "fa-receiver-uuid",
  "merchantId": "merchant-uuid",
  "txnRef": "TXN-REF-001",
  "timestamp": "2025-07-11T10:00:45Z",
  "traceId": "4bf92f3577b34da6a3ce929d0e0e4736"
}
```

---

### State machine transitions

```
RECEIVED              → PAIRED                   : APP_SCAN
PAIRED                → WAITING_BANK_AUTH         : IMPLICIT_CONFIRM
WAITING_BANK_AUTH     → WAITING_MERCHANT_UPDATE   : AUTH_OK + FLAG_TRUE
WAITING_BANK_AUTH     → ADVICE_PENDING            : AUTH_OK + FLAG_FALSE
WAITING_MERCHANT_UPDATE → ADVICE_PENDING          : MERCHANT_CONFIRM
WAITING_MERCHANT_UPDATE → TIMEOUT                 : SCHEDULER_TIMEOUT
ADVICE_PENDING        → SUCCESSFUL                : CAPTURED_OK
ADVICE_PENDING        → FAILED_AFTER_CONFIRMATION : ADVICE_REJECTED

RECEIVED              → CANCELLED_BY_MERCHANT     : MERCHANT_CANCEL
PAIRED                → CANCELLED_BY_APP          : CUSTOMER_CANCEL
PAIRED                → CANCELLED_BY_MERCHANT     : MERCHANT_CANCEL
WAITING_MERCHANT_UPDATE → CANCELLED_BY_MERCHANT   : MERCHANT_CANCEL

WAITING_BANK_AUTH     → FAILED                   : AUTH_DENIED / FINP_ERROR
PENDING (P2P)         → SUCCESSFUL               : TRANSFER_OK
PENDING (P2P)         → FAILED                   : TRANSFER_FAILED
```

---

### Error codes tổng hợp

| HTTP Status | Error Type | Mô tả |
|---|---|---|
| 400 | `bad-request` | Request không hợp lệ |
| 401 | `unauthorized` | JWT hoặc API Key invalid |
| 403 | `forbidden` | Không có quyền xem order này |
| 404 | `order-not-found` | Order không tồn tại |
| 404 | `recipient-not-found` | Receiver P2P không tồn tại |
| 409 | `duplicate-request` | Idempotency key đã tồn tại |
| 409 | `invalid-order-state` | State transition không hợp lệ |
| 409 | `cannot-cancel-order` | Order không thể huỷ ở state này |
| 422 | `merchant-suspended` | Merchant bị suspend |
| 422 | `transfer-failed` | Transfer thất bại |
| 422 | `sender-not-eligible` | Sender không đủ điều kiện |
| 503 | `service-unavailable` | Circuit breaker open |
| 500 | `internal-error` | Lỗi hệ thống |
