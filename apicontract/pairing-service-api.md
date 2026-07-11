## API Contract — pairing-service (:8082)

Base URL: `http://localhost:8082`
Package: `com.twint.scheme.pairing`

---

### Tổng quan endpoints

| Method | Path | Mô tả | Auth |
|---|---|---|---|
| POST | `/v1/pairings` | Tạo pairing session + QR | MERCHANT |
| GET | `/v1/pairings/{id}` | Lấy state pairing hiện tại | MERCHANT, INTERNAL |
| POST | `/v1/pairings/scan` | App scan QR — validate token | CUSTOMER |
| POST | `/v1/pairings/{id}/confirm` | Attach order vào pairing | INTERNAL |
| POST | `/v1/pairings/{id}/cancel` | Huỷ pairing | CUSTOMER, MERCHANT |
| POST | `/v1/pairings/{id}/complete` | Complete pairing sau payment | INTERNAL |
| GET | `/v1/pairings/find-by-token` | Tìm pairing theo token | INTERNAL |

---

### POST `/v1/pairings`
Tạo pairing session mới, sinh token và QR code.
Được gọi bởi order-service khi merchant startOrder.

**Request Header:**
```
X-Internal-Service: order-service
Content-Type: application/json
```

**Request Body:**
```json
{
  "merchantId": "merchant-uuid",
  "terminalId": "terminal-uuid"
}
```

**Response 201 Created:**
```json
{
  "id": "pairing-uuid",
  "token": "TKN-A1B2C3D4E5F6",
  "qrCode": "data:image/png;base64,iVBORw0KGgoAAAANS...",
  "merchantId": "merchant-uuid",
  "terminalId": "terminal-uuid",
  "state": "CREATED",
  "expiresAt": "2025-07-11T10:00:30Z",
  "createdAt": "2025-07-11T10:00:00Z"
}
```

**Response 422 Unprocessable Entity:**
```json
{
  "type": "https://api.twint.scheme/errors/terminal-inactive",
  "title": "Terminal Inactive",
  "status": 422,
  "detail": "Terminal terminal-uuid is not active",
  "traceId": "4bf92f3577b34da6a3ce929d0e0e4736"
}
```

---

### GET `/v1/pairings/{id}`
Lấy state hiện tại của pairing.
Merchant dùng để polling sau khi hiển thị QR.

**Request Header:**
```
Authorization: Bearer {merchant_jwt}
```

**Response 200 OK:**
```json
{
  "id": "pairing-uuid",
  "state": "SCANNED",
  "merchantId": "merchant-uuid",
  "terminalId": "terminal-uuid",
  "customerId": "customer-uuid",
  "expiresAt": "2025-07-11T10:00:30Z",
  "createdAt": "2025-07-11T10:00:00Z",
  "updatedAt": "2025-07-11T10:00:15Z"
}
```

**Pairing states:**
| State | Mô tả |
|---|---|
| `CREATED` | Mới tạo, chờ app scan |
| `SCANNED` | App đã scan, customer attached |
| `CONFIRMED` | Order đã attach vào pairing |
| `COMPLETED` | Payment done, token invalidated |
| `EXPIRED` | Hết TTL không có ai scan |
| `CANCELLED` | Bị huỷ bởi customer hoặc merchant |

---

### POST `/v1/pairings/scan`
App scan QR và gửi token lên để validate.
Đây là bước customer xác nhận muốn thanh toán tại terminal này.

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

**Response 200 OK:**
```json
{
  "pairingId": "pairing-uuid",
  "merchantId": "merchant-uuid",
  "merchantName": "Coffee Shop ABC",
  "terminalId": "terminal-uuid",
  "terminalCode": "TRM-001",
  "state": "SCANNED",
  "customerId": "customer-uuid"
}
```

**Response 409 Conflict — token đã dùng:**
```json
{
  "type": "https://api.twint.scheme/errors/token-already-used",
  "title": "Token Already Used",
  "status": 409,
  "detail": "This token has already been scanned",
  "traceId": "4bf92f3577b34da6a3ce929d0e0e4736"
}
```

**Response 410 Gone — token hết hạn:**
```json
{
  "type": "https://api.twint.scheme/errors/token-expired",
  "title": "Token Expired",
  "status": 410,
  "detail": "Token has expired, please ask merchant to generate a new QR",
  "traceId": "4bf92f3577b34da6a3ce929d0e0e4736"
}
```

**Response 404 Not Found:**
```json
{
  "type": "https://api.twint.scheme/errors/token-not-found",
  "title": "Token Not Found",
  "status": 404,
  "detail": "Token TKN-A1B2C3D4E5F6 does not exist",
  "traceId": "4bf92f3577b34da6a3ce929d0e0e4736"
}
```

---

### POST `/v1/pairings/{id}/confirm`
Attach order vào pairing sau khi order được tạo.
Được gọi bởi order-service (INTERNAL).

**Request Header:**
```
X-Internal-Service: order-service
Content-Type: application/json
```

**Request Body:**
```json
{
  "orderId": "order-uuid"
}
```

**Response 200 OK:**
```json
{
  "id": "pairing-uuid",
  "state": "CONFIRMED",
  "orderId": "order-uuid",
  "updatedAt": "2025-07-11T10:00:20Z"
}
```

**Response 409 Conflict — pairing không ở state hợp lệ:**
```json
{
  "type": "https://api.twint.scheme/errors/invalid-pairing-state",
  "title": "Invalid Pairing State",
  "status": 409,
  "detail": "Pairing must be in SCANNED state to confirm, current state: EXPIRED",
  "traceId": "4bf92f3577b34da6a3ce929d0e0e4736"
}
```

---

### POST `/v1/pairings/{id}/cancel`
Huỷ pairing. Customer hoặc Merchant đều có thể huỷ.

**Request Header:**
```
Authorization: Bearer {jwt}
Content-Type: application/json
```

**Request Body:**
```json
{
  "reason": "CUSTOMER_CANCELLED",
  "cancelledBy": "CUSTOMER"
}
```

**cancelledBy có thể có:**
| Value | Mô tả |
|---|---|
| `CUSTOMER` | Customer chủ động huỷ trên app |
| `MERCHANT` | Merchant huỷ tại terminal |
| `SYSTEM` | Timeout handler huỷ tự động |

**Response 200 OK:**
```json
{
  "id": "pairing-uuid",
  "state": "CANCELLED",
  "cancelledBy": "CUSTOMER",
  "reason": "CUSTOMER_CANCELLED",
  "updatedAt": "2025-07-11T10:00:25Z"
}
```

**Response 409 Conflict — không thể huỷ:**
```json
{
  "type": "https://api.twint.scheme/errors/cannot-cancel-pairing",
  "title": "Cannot Cancel Pairing",
  "status": 409,
  "detail": "Pairing in COMPLETED state cannot be cancelled",
  "traceId": "4bf92f3577b34da6a3ce929d0e0e4736"
}
```

---

### POST `/v1/pairings/{id}/complete`
Đánh dấu pairing hoàn tất sau khi payment SUCCESSFUL.
Token bị blacklist ngay lập tức.
Được gọi bởi order-service (INTERNAL).

**Request Header:**
```
X-Internal-Service: order-service
Content-Type: application/json
```

**Request Body:**
```json
{
  "orderId": "order-uuid"
}
```

**Response 200 OK:**
```json
{
  "id": "pairing-uuid",
  "state": "COMPLETED",
  "orderId": "order-uuid",
  "tokenBlacklisted": true,
  "updatedAt": "2025-07-11T10:00:45Z"
}
```

---

### GET `/v1/pairings/find-by-token?token={token}`
Tìm pairing theo token.
Được gọi bởi order-service để validate và lấy pairingId (INTERNAL).

**Request Header:**
```
X-Internal-Service: order-service
```

**Response 200 OK:**
```json
{
  "id": "pairing-uuid",
  "state": "CREATED",
  "merchantId": "merchant-uuid",
  "terminalId": "terminal-uuid",
  "customerId": null,
  "expiresAt": "2025-07-11T10:00:30Z"
}
```

---

### State transition rules

```
CREATED     → SCANNED      : app scan token hợp lệ
CREATED     → EXPIRED      : scheduler TTL expired
CREATED     → CANCELLED    : merchant cancel
SCANNED     → CONFIRMED    : order-service attach orderId
SCANNED     → CANCELLED    : customer cancel / merchant cancel
CONFIRMED   → COMPLETED    : payment SUCCESSFUL
CONFIRMED   → CANCELLED    : payment FAILED / TIMEOUT
```

Mọi transition không hợp lệ trả `409 INVALID_PAIRING_STATE`.

---

### Redis keys liên quan

| Key | TTL | Mô tả |
|---|---|---|
| `pairing:token:{token}` | 30s | Lưu pairingId, check tồn tại và còn hạn |
| `pairing:used:{token}` | 1h | Blacklist token sau khi dùng |

---

### Error codes tổng hợp

| HTTP Status | Error Type | Mô tả |
|---|---|---|
| 400 | `bad-request` | Request không hợp lệ |
| 401 | `unauthorized` | JWT missing hoặc invalid |
| 403 | `forbidden` | Không đủ quyền |
| 404 | `token-not-found` | Token không tồn tại |
| 404 | `pairing-not-found` | Pairing không tồn tại |
| 409 | `token-already-used` | Token đã được scan |
| 409 | `invalid-pairing-state` | State transition không hợp lệ |
| 409 | `cannot-cancel-pairing` | Pairing không thể huỷ ở state hiện tại |
| 410 | `token-expired` | Token đã hết hạn |
| 422 | `terminal-inactive` | Terminal không active |
| 500 | `internal-error` | Lỗi hệ thống |
