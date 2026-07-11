## API Contract — merchant-service (:8084)

Base URL: `http://localhost:8084`
Package: `com.twint.scheme.merchant`

---

### Tổng quan endpoints

| Method | Path | Mô tả | Auth |
|---|---|---|---|
| POST | `/v1/merchants` | Tạo merchant | ADMIN |
| GET | `/v1/merchants/{id}` | Lấy thông tin merchant | MERCHANT, ADMIN |
| PUT | `/v1/merchants/{id}/status` | Activate/suspend merchant | ADMIN |
| PUT | `/v1/merchants/{id}/confirmation-flag` | Bật/tắt confirmation flag | MERCHANT, ADMIN |
| GET | `/v1/merchants/resolve` | Resolve merchant theo terminalCode | INTERNAL |
| POST | `/v1/merchants/{id}/terminals` | Thêm terminal | MERCHANT, ADMIN |
| GET | `/v1/merchants/{id}/terminals/{terminalId}` | Lấy thông tin terminal | MERCHANT, ADMIN |
| PUT | `/v1/merchants/{id}/terminals/{terminalId}/status` | Activate/deactivate terminal | MERCHANT, ADMIN |

---

### POST `/v1/merchants`
Tạo mới merchant.

**Request Header:**
```
Authorization: Bearer {admin_jwt}
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Coffee Shop ABC",
  "mcc": "5812",
  "confirmationFlag": false
}
```

**Response 201 Created:**
```json
{
  "id": "merchant-uuid",
  "name": "Coffee Shop ABC",
  "mcc": "5812",
  "confirmationFlag": false,
  "status": "ACTIVE",
  "createdAt": "2025-07-11T10:00:00Z"
}
```

**Response 400 Bad Request:**
```json
{
  "type": "https://api.twint.scheme/errors/bad-request",
  "title": "Bad Request",
  "status": 400,
  "detail": "mcc must be a 4-digit numeric string",
  "traceId": "4bf92f3577b34da6a3ce929d0e0e4736"
}
```

---

### GET `/v1/merchants/{id}`
Lấy thông tin merchant kèm danh sách terminal.

**Request Header:**
```
Authorization: Bearer {jwt}
```

**Response 200 OK:**
```json
{
  "id": "merchant-uuid",
  "name": "Coffee Shop ABC",
  "mcc": "5812",
  "confirmationFlag": false,
  "status": "ACTIVE",
  "createdAt": "2025-07-11T10:00:00Z",
  "updatedAt": "2025-07-11T10:00:00Z",
  "terminals": [
    {
      "id": "terminal-uuid",
      "terminalCode": "TRM-001",
      "type": "EFTPOS",
      "status": "ACTIVE",
      "createdAt": "2025-07-11T10:00:00Z"
    }
  ]
}
```

**Response 404 Not Found:**
```json
{
  "type": "https://api.twint.scheme/errors/merchant-not-found",
  "title": "Merchant Not Found",
  "status": 404,
  "detail": "Merchant merchant-uuid not found",
  "traceId": "4bf92f3577b34da6a3ce929d0e0e4736"
}
```

---

### PUT `/v1/merchants/{id}/status`
Activate hoặc suspend merchant. Chỉ ADMIN.

**Request Body:**
```json
{
  "status": "SUSPENDED",
  "reason": "Violation of terms"
}
```

**Response 200 OK:**
```json
{
  "id": "merchant-uuid",
  "status": "SUSPENDED",
  "updatedAt": "2025-07-11T10:00:00Z"
}
```

**status có thể có:**
| Value | Mô tả |
|---|---|
| `ACTIVE` | Merchant đang hoạt động |
| `SUSPENDED` | Tạm ngưng |
| `INACTIVE` | Ngưng hoạt động vĩnh viễn |

---

### PUT `/v1/merchants/{id}/confirmation-flag`
Bật hoặc tắt yêu cầu merchant confirm thủ công.
Có hiệu lực với order tiếp theo, không ảnh hưởng order đang xử lý.

**Request Body:**
```json
{
  "confirmationFlag": true
}
```

**Response 200 OK:**
```json
{
  "id": "merchant-uuid",
  "confirmationFlag": true,
  "updatedAt": "2025-07-11T10:00:00Z"
}
```

---

### GET `/v1/merchants/resolve?terminalCode={terminalCode}`
Resolve merchant và terminal theo terminalCode.
Được gọi bởi pairing-service và order-service (INTERNAL).

**Request Header:**
```
X-Internal-Service: order-service
```

**Response 200 OK:**
```json
{
  "merchantId": "merchant-uuid",
  "merchantName": "Coffee Shop ABC",
  "merchantStatus": "ACTIVE",
  "confirmationFlag": false,
  "terminalId": "terminal-uuid",
  "terminalCode": "TRM-001",
  "terminalType": "EFTPOS",
  "terminalStatus": "ACTIVE"
}
```

**Response 404 Not Found:**
```json
{
  "type": "https://api.twint.scheme/errors/terminal-not-found",
  "title": "Terminal Not Found",
  "status": 404,
  "detail": "No active terminal with code TRM-001",
  "traceId": "4bf92f3577b34da6a3ce929d0e0e4736"
}
```

**Response 422 Unprocessable Entity:**
```json
{
  "type": "https://api.twint.scheme/errors/merchant-suspended",
  "title": "Merchant Suspended",
  "status": 422,
  "detail": "Merchant is currently suspended and cannot accept payments",
  "traceId": "4bf92f3577b34da6a3ce929d0e0e4736"
}
```

---

### POST `/v1/merchants/{id}/terminals`
Thêm terminal mới cho merchant.

**Request Body:**
```json
{
  "terminalCode": "TRM-001",
  "type": "EFTPOS"
}
```

**type có thể có:**
| Value | Mô tả |
|---|---|
| `EFTPOS` | Terminal vật lý tại quầy |
| `ESHOP` | Terminal online |
| `MOBILE` | Terminal mobile app |

**Response 201 Created:**
```json
{
  "id": "terminal-uuid",
  "merchantId": "merchant-uuid",
  "terminalCode": "TRM-001",
  "type": "EFTPOS",
  "status": "ACTIVE",
  "createdAt": "2025-07-11T10:00:00Z"
}
```

**Response 409 Conflict:**
```json
{
  "type": "https://api.twint.scheme/errors/duplicate-terminal-code",
  "title": "Terminal Code Already Exists",
  "status": 409,
  "detail": "Terminal code TRM-001 is already registered in the system",
  "traceId": "4bf92f3577b34da6a3ce929d0e0e4736"
}
```

---

### GET `/v1/merchants/{id}/terminals/{terminalId}`
Lấy thông tin chi tiết terminal.

**Response 200 OK:**
```json
{
  "id": "terminal-uuid",
  "merchantId": "merchant-uuid",
  "terminalCode": "TRM-001",
  "type": "EFTPOS",
  "status": "ACTIVE",
  "createdAt": "2025-07-11T10:00:00Z",
  "updatedAt": "2025-07-11T10:00:00Z"
}
```

---

### PUT `/v1/merchants/{id}/terminals/{terminalId}/status`
Activate hoặc deactivate terminal.

**Request Body:**
```json
{
  "status": "INACTIVE"
}
```

**Response 200 OK:**
```json
{
  "id": "terminal-uuid",
  "status": "INACTIVE",
  "updatedAt": "2025-07-11T10:00:00Z"
}
```

---

### Error codes tổng hợp

| HTTP Status | Error Type | Mô tả |
|---|---|---|
| 400 | `bad-request` | Request body không hợp lệ |
| 401 | `unauthorized` | JWT hoặc API Key missing/invalid |
| 403 | `forbidden` | Không đủ quyền |
| 404 | `merchant-not-found` | Merchant không tồn tại |
| 404 | `terminal-not-found` | Terminal không tồn tại |
| 409 | `duplicate-terminal-code` | Terminal code đã tồn tại |
| 422 | `merchant-suspended` | Merchant bị suspend không nhận thanh toán |
| 500 | `internal-error` | Lỗi hệ thống |
