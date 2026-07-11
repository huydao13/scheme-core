## API Contract — smartphone-service (:8181)

Base URL: `http://localhost:8181`
Package: `com.twint.scheme.smartphone`

---

### Tổng quan endpoints

| Method | Path | Mô tả | Auth |
|---|---|---|---|
| POST | `/service/v1/orders/p2p/send` | Gửi P2P transfer | CUSTOMER |
| POST | `/service/v1/pairings/scan` | Scan QR code | CUSTOMER |
| GET | `/service/v1/orders/{id}` | Lấy thông tin order | CUSTOMER |
| GET | `/service/v1/orders` | Lịch sử order | CUSTOMER |
| POST | `/service/v1/orders/{id}/cancel` | Huỷ order | CUSTOMER |

---

### POST `/service/v1/orders/p2p/send`
BFF endpoint cho mobile app gửi P2P.
Validate + idempotency check trước khi forward xuống order-service.

**Request Header:**
```
Authorization: Bearer {customer_jwt}
X-Idempotency-Key: {uuid}
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

**Xử lý nội bộ:**
1. Validate request (amount > 0, phone format hợp lệ)
2. Check `resendOrderUuid` — nếu không null → trả cached response
3. Check Redis `idempotency:{X-Idempotency-Key}`:
    - `PROCESSING` → 409 `DUPLICATE_REQUEST`
    - Response cached → trả lại cached response
    - Không tồn tại → set `PROCESSING`, forward xuống order-service
4. Nhận response từ order-service → cache vào Redis TTL 24h
5. Trả response về app

**Response 200 OK:**
```json
{
  "orderId": "order-uuid",
  "type": "P2P",
  "state": "SUCCESSFUL",
  "amount": 150000,
  "currency": "VND",
  "receiverAlias": "Tran Thi B",
  "txnRef": "TXN-REF-001",
  "createdAt": "2025-07-11T10:00:00Z",
  "completedAt": "2025-07-11T10:00:01Z"
}
```

**Response 409 Conflict — đang xử lý:**
```json
{
  "type": "https://api.twint.scheme/errors/duplicate-request",
  "title": "Duplicate Request",
  "status": 409,
  "detail": "Request with this idempotency key is already being processed",
  "traceId": "4bf92f3577b34da6a3ce929d0e0e4736"
}
```

---

### POST `/service/v1/pairings/scan`
App scan QR code tại terminal.

**Request Header:**
```
Authorization: Bearer {customer_jwt}
Content-Type: application/json
```

**Request Body:**
```json
{
  "token": "TKN-A1B2C3D4E5F6"
}
```

**Xử lý nội bộ:**
1. Validate JWT — lấy customerId từ `X-User-Id` header
2. Forward xuống pairing-service `POST /v1/pairings/scan`
3. Trả response về app

**Response 200 OK:**
```json
{
  "pairingId": "pairing-uuid",
  "merchantName": "Coffee Shop ABC",
  "terminalCode": "TRM-001",
  "state": "SCANNED"
}
```

---

### GET `/service/v1/orders/{id}`
Lấy thông tin order của customer.

**Response 200 OK:**
```json
{
  "orderId": "order-uuid",
  "type": "P2P",
  "state": "SUCCESSFUL",
  "amount": 150000,
  "currency": "VND",
  "txnRef": "TXN-REF-001",
  "createdAt": "2025-07-11T10:00:00Z",
  "completedAt": "2025-07-11T10:00:01Z"
}
```

---

### GET `/service/v1/orders`
Lịch sử order của customer đang login.

**Query Parameters:**
| Param | Type | Required | Mô tả |
|---|---|---|---|
| `type` | string | false | `P2P`, `PAYMENT_IMMEDIATE` |
| `state` | string | false | Filter theo state |
| `page` | int | false | Default 0 |
| `size` | int | false | Default 20 |

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
      "createdAt": "2025-07-11T10:00:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 10,
  "totalPages": 1
}
```

---

### POST `/service/v1/orders/{id}/cancel`
Customer huỷ order.

**Request Body:**
```json
{
  "reason": "CUSTOMER_CHANGED_MIND"
}
```

**Response 200 OK:**
```json
{
  "orderId": "order-uuid",
  "state": "CANCELLED_BY_APP",
  "cancelledAt": "2025-07-11T10:00:25Z"
}
```

---

### Error codes tổng hợp

| HTTP Status | Error Type | Mô tả |
|---|---|---|
| 400 | `bad-request` | Request không hợp lệ |
| 401 | `unauthorized` | JWT invalid hoặc expired |
| 403 | `forbidden` | Không có quyền |
| 404 | `order-not-found` | Order không tồn tại |
| 409 | `duplicate-request` | Idempotency key đang xử lý |
| 422 | `transfer-failed` | Transfer thất bại |
| 503 | `service-unavailable` | order-service không khả dụng |
| 500 | `internal-error` | Lỗi hệ thống |

---

## API Contract — notification-service (:8182)

Base URL: `http://localhost:8182`
Package: `com.twint.scheme.notification`

---

### Tổng quan endpoints

| Method | Path | Mô tả | Auth |
|---|---|---|---|
| GET | `/v1/notifications` | Lấy danh sách notification | CUSTOMER, MERCHANT |
| PUT | `/v1/notifications/{id}/read` | Đánh dấu đã đọc | CUSTOMER, MERCHANT |
| PUT | `/v1/notifications/read-all` | Đánh dấu tất cả đã đọc | CUSTOMER, MERCHANT |
| GET | `/v1/notifications/unread-count` | Đếm số chưa đọc | CUSTOMER, MERCHANT |

---

### Kafka Consumers

**Topic:** `order.events`
**Consumer Group:** `notification-service`

| Event Type | Hành động |
|---|---|
| `order.successful` | Notify sender "Thanh toán / chuyển tiền thành công" + notify receiver/merchant "Nhận được thanh toán" |
| `order.failed` | Notify sender "Thanh toán thất bại" |
| `order.cancelled` | Notify customer + merchant "Đơn hàng đã huỷ" |
| `order.timeout` | Notify merchant "Đơn hàng hết hạn" + notify customer "Phiên thanh toán đã hết hạn" |
| `order.failed_after_confirmation` | Notify merchant + customer "Lỗi hệ thống, vui lòng liên hệ support" |

---

### GET `/v1/notifications`
Lấy danh sách notification của recipient.

**Request Header:**
```
Authorization: Bearer {jwt}
```

**Query Parameters:**
| Param | Type | Required | Mô tả |
|---|---|---|---|
| `isRead` | boolean | false | Filter chưa đọc: `false` |
| `type` | string | false | Filter theo type |
| `page` | int | false | Default 0 |
| `size` | int | false | Default 20 |

**Response 200 OK:**
```json
{
  "content": [
    {
      "id": "notif-uuid",
      "type": "PAYMENT_SUCCESS",
      "channel": "PUSH",
      "payload": {
        "title": "Thanh toán thành công",
        "body": "Bạn đã thanh toán 150.000 VND tại Coffee Shop ABC",
        "orderId": "order-uuid"
      },
      "isRead": false,
      "sentAt": "2025-07-11T10:00:46Z",
      "createdAt": "2025-07-11T10:00:45Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 5,
  "totalPages": 1
}
```

---

### PUT `/v1/notifications/{id}/read`
Đánh dấu notification đã đọc.

**Response 200 OK:**
```json
{
  "id": "notif-uuid",
  "isRead": true,
  "readAt": "2025-07-11T10:01:00Z"
}
```

---

### PUT `/v1/notifications/read-all`
Đánh dấu tất cả notification của recipient là đã đọc.

**Response 200 OK:**
```json
{
  "updatedCount": 5
}
```

---

### GET `/v1/notifications/unread-count`
Đếm số notification chưa đọc.

**Response 200 OK:**
```json
{
  "unreadCount": 3
}
```

---

### Notification types

| Type | Mô tả |
|---|---|
| `PAYMENT_SUCCESS` | Thanh toán merchant thành công |
| `PAYMENT_FAILED` | Thanh toán thất bại |
| `P2P_SENT` | Gửi tiền P2P thành công |
| `P2P_RECEIVED` | Nhận tiền P2P |
| `ORDER_CANCELLED` | Order bị huỷ |
| `ORDER_TIMEOUT` | Order hết hạn |
| `SYSTEM_ERROR` | Lỗi hệ thống cần liên hệ support |

---

### Error codes tổng hợp

| HTTP Status | Error Type | Mô tả |
|---|---|---|
| 401 | `unauthorized` | JWT invalid |
| 403 | `forbidden` | Không có quyền xem notification này |
| 404 | `notification-not-found` | Notification không tồn tại |
| 500 | `internal-error` | Lỗi hệ thống |

---

## API Contract — six-clearing-service (:9081)

Base URL: `http://localhost:9081`
Package: `com.twint.scheme.external.clearing`

---

### Tổng quan endpoints

| Method | Path | Mô tả |
|---|---|---|
| POST | `/finp/v1/reserve` | Authorization — giữ tiền |
| POST | `/finp/v1/advice` | Capture — chuyển tiền thật |
| POST | `/finp/v1/cancellation` | Release reservation |
| GET | `/mock/config` | Xem config hiện tại |
| PUT | `/mock/config/scenario` | Set global scenario |
| PUT | `/mock/config/flaky` | Set flaky fail percent |
| GET | `/mock/txn-log` | Xem audit log tất cả calls |
| POST | `/mock/reset` | Reset toàn bộ state |

---

### POST `/finp/v1/reserve`
Authorization — yêu cầu giữ tiền trong tài khoản customer.

**Request Header:**
```
X-Api-Key: {six_api_key}
X-Mock-Scenario: HAPPY
Content-Type: application/json
```

**Request Body:**
```json
{
  "orderId": "order-uuid",
  "amount": 150000,
  "currency": "VND",
  "accountId": "fa-uuid",
  "idempotencyKey": "idem-uuid"
}
```

**Response 200 OK — authorized:**
```json
{
  "status": "AUTHORISED",
  "authCode": "AUTH-A1B2C3D4",
  "reservedAmount": 150000,
  "timestamp": "2025-07-11T10:00:20Z"
}
```

**Response 200 OK — auth denied:**
```json
{
  "status": "AUTHORISATION_DENIED",
  "reasonCode": "INSUFFICIENT_FUNDS",
  "timestamp": "2025-07-11T10:00:20Z"
}
```

**reasonCode có thể có:**
| reasonCode | Mô tả |
|---|---|
| `INSUFFICIENT_FUNDS` | Số dư không đủ |
| `AUTHORISATION_DENIED` | Bank từ chối |
| `LIMIT_EXCEEDED` | Vượt hạn mức |
| `ACCOUNT_BLOCKED_SANCTION` | Tài khoản bị block |
| `FRAUD_DETECTED` | Phát hiện gian lận |

**Response 4xx/5xx — FINP rejected:**
```json
{
  "errorCode": "BAD_REQUEST",
  "message": "Invalid reserve request format",
  "timestamp": "2025-07-11T10:00:20Z"
}
```

---

### POST `/finp/v1/advice`
Capture — thực hiện chuyển tiền thật sau khi authorized.

**Request Body:**
```json
{
  "orderId": "order-uuid",
  "authCode": "AUTH-A1B2C3D4",
  "amount": 150000,
  "currency": "VND"
}
```

**Response 200 OK:**
```json
{
  "status": "CAPTURED",
  "authCode": "AUTH-A1B2C3D4",
  "capturedAt": "2025-07-11T10:00:40Z"
}
```

**Response 4xx/5xx:**
```json
{
  "errorCode": "ADVICE_REJECTED",
  "message": "authCode invalid or expired",
  "timestamp": "2025-07-11T10:00:40Z"
}
```

---

### POST `/finp/v1/cancellation`
Release reservation — huỷ giữ tiền.

**Request Body:**
```json
{
  "orderId": "order-uuid",
  "authCode": "AUTH-A1B2C3D4",
  "reason": "MERCHANT_TIMEOUT"
}
```

**Response 200 OK:**
```json
{
  "status": "CANCELLED",
  "cancelledAt": "2025-07-11T10:05:00Z"
}
```

**Response 500 — cancel rejected (money stuck):**
```json
{
  "errorCode": "CANCEL_REJECTED",
  "message": "SIX rejected cancellation — reservation may be stuck. Manual review required.",
  "timestamp": "2025-07-11T10:05:00Z"
}
```

---

### Mock control endpoints

**PUT `/mock/config/scenario`**
```json
{ "scenario": "INSUFFICIENT_FUNDS" }
```

**Available scenarios:**
| Scenario | Mô tả |
|---|---|
| `HAPPY` | Flow thành công |
| `INSUFFICIENT_FUNDS` | Thiếu tiền |
| `AUTH_DENIED` | Bank từ chối |
| `LIMIT_EXCEEDED` | Vượt hạn mức |
| `ACCOUNT_BLOCKED` | Tài khoản bị block |
| `FRAUD_DETECTED` | Phát hiện gian lận |
| `FINP_400` | FINP reject 400 |
| `FINP_500` | FINP internal error |
| `FINP_503` | FINP unavailable |
| `TIMEOUT:{ms}` | Delay ms rồi timeout |
| `NETWORK_DROP` | Mất kết nối sau reserve |
| `ADVICE_FAIL_4XX` | Advice bị reject |
| `ADVICE_TIMEOUT` | Advice timeout |
| `CANCEL_FAIL` | Cancellation bị reject |
| `FLAKY` | Random fail theo % |
| `CLEAR` | Xoá global scenario |

**GET `/mock/config`** — Xem config và available scenarios

**PUT `/mock/config/flaky`**
```json
{ "failPercent": 30 }
```

**GET `/mock/txn-log`** — Audit log tất cả calls
```json
[
  {
    "ts": "2025-07-11T10:00:20Z",
    "operation": "RESERVE",
    "orderId": "order-uuid",
    "result": "AUTHORISED",
    "ref": "AUTH-A1B2C3D4"
  }
]
```

**POST `/mock/reset`** — Reset toàn bộ state và log

---

## API Contract — bank-core-service (:9082)

Base URL: `http://localhost:9082`
Package: `com.twint.scheme.external.bank`

---

### Tổng quan endpoints

| Method | Path | Mô tả |
|---|---|---|
| POST | `/portfolios/{uuid}/transfer` | Transfer tiền (merchant capture) |
| POST | `/portfolios/{uuid}/p2pMoney/reserveAndTransfer` | Atomic P2P transfer |
| GET | `/portfolios/{uuid}/balance` | Lấy balance tài khoản |
| PUT | `/mock/balance/{uuid}` | Set balance (mock control) |
| GET | `/mock/config` | Xem config |
| PUT | `/mock/config/scenario` | Set global scenario |
| GET | `/mock/txn-log` | Audit log |
| POST | `/mock/reset` | Reset state |

---

### POST `/portfolios/{uuid}/transfer`
Transfer tiền từ tài khoản customer sang merchant.
Dùng trong merchant payment flow sau advice.

**Request Header:**
```
X-Api-Key: {bank_api_key}
X-Mock-Scenario: HAPPY
Content-Type: application/json
```

**Path Variable:** `uuid` = sender financialAccount id

**Request Body:**
```json
{
  "toAccountId": "fa-merchant-uuid",
  "amount": 150000,
  "currency": "VND",
  "authCode": "AUTH-A1B2C3D4",
  "idempotencyKey": "idem-uuid",
  "reference": "order-uuid"
}
```

**Response 200 OK:**
```json
{
  "txnRef": "TXN-REF-001",
  "fromAccountId": "fa-sender-uuid",
  "toAccountId": "fa-merchant-uuid",
  "amount": 150000,
  "currency": "VND",
  "status": "COMPLETED",
  "executedAt": "2025-07-11T10:00:40Z"
}
```

**Response 422 — authCode invalid:**
```json
{
  "errorCode": "INVALID_AUTH_CODE",
  "message": "authCode expired or already used",
  "timestamp": "2025-07-11T10:00:40Z"
}
```

---

### POST `/portfolios/{uuid}/p2pMoney/reserveAndTransfer`
Atomic debit sender + credit receiver trong 1 operation.
Dùng trong P2P flow.

**Path Variable:** `uuid` = sender financialAccount id

**Request Body:**
```json
{
  "toAccountId": "fa-receiver-uuid",
  "amount": 150000,
  "currency": "VND",
  "idempotencyKey": "idem-uuid",
  "reference": "order-uuid"
}
```

**Response 200 OK:**
```json
{
  "txnRef": "TXN-REF-002",
  "fromAccountId": "fa-sender-uuid",
  "toAccountId": "fa-receiver-uuid",
  "amount": 150000,
  "currency": "VND",
  "status": "COMPLETED",
  "executedAt": "2025-07-11T10:00:01Z"
}
```

**Response 422 — insufficient funds:**
```json
{
  "errorCode": "INSUFFICIENT_FUNDS",
  "message": "Sender account balance is insufficient",
  "availableBalance": 50000,
  "requestedAmount": 150000,
  "timestamp": "2025-07-11T10:00:01Z"
}
```

**Response 404 — receiver not found:**
```json
{
  "errorCode": "ACCOUNT_NOT_FOUND",
  "message": "Receiver account fa-receiver-uuid not found",
  "timestamp": "2025-07-11T10:00:01Z"
}
```

**Response 500 — partial fail (debit ok, credit fail):**
```json
{
  "errorCode": "PARTIAL_FAILURE",
  "message": "Debit succeeded but credit failed — transaction rolled back",
  "timestamp": "2025-07-11T10:00:01Z"
}
```

---

### GET `/portfolios/{uuid}/balance`
Lấy balance hiện tại của tài khoản.

**Response 200 OK:**
```json
{
  "accountId": "fa-uuid",
  "balance": 2500000,
  "currency": "VND",
  "asOf": "2025-07-11T10:00:00Z"
}
```

---

### Mock control endpoints

**PUT `/mock/balance/{uuid}`** — Set balance cho tài khoản
```json
{
  "balance": 5000000,
  "currency": "VND"
}
```

**Response 200 OK:**
```json
{
  "accountId": "fa-uuid",
  "balance": 5000000,
  "currency": "VND"
}
```

**PUT `/mock/config/scenario`**
```json
{ "scenario": "INSUFFICIENT_FUNDS" }
```

**Available scenarios:**
| Scenario | Mô tả |
|---|---|
| `HAPPY` | Transfer thành công |
| `INSUFFICIENT_FUNDS` | Số dư không đủ |
| `ACCOUNT_NOT_FOUND` | Receiver không tồn tại |
| `PARTIAL_FAILURE` | Debit ok credit fail → rollback |
| `DUPLICATE` | Idempotency — trả lại txnRef cũ |
| `TIMEOUT:{ms}` | Delay rồi timeout |
| `SERVER_ERROR` | 500 internal error |
| `FLAKY` | Random fail |
| `CLEAR` | Xoá global scenario |

**GET `/mock/txn-log`** — Audit log
```json
[
  {
    "ts": "2025-07-11T10:00:01Z",
    "operation": "RESERVE_AND_TRANSFER",
    "fromAccountId": "fa-sender-uuid",
    "toAccountId": "fa-receiver-uuid",
    "amount": 150000,
    "result": "COMPLETED",
    "txnRef": "TXN-REF-002"
  }
]
```

**POST `/mock/reset`** — Reset balance store, idempotency store, txn log

---

### Error codes tổng hợp (bank-core-service)

| HTTP Status | Error Code | Mô tả |
|---|---|---|
| 200 | — | Transfer thành công |
| 404 | `ACCOUNT_NOT_FOUND` | Tài khoản không tồn tại |
| 422 | `INSUFFICIENT_FUNDS` | Số dư không đủ |
| 422 | `INVALID_AUTH_CODE` | authCode không hợp lệ |
| 500 | `PARTIAL_FAILURE` | Atomic fail — đã rollback |
| 500 | `SERVER_ERROR` | Lỗi hệ thống |
| 504 | `TIMEOUT` | Không phản hồi trong thời gian quy định |
