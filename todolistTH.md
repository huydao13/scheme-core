# 📋 Payment System — Improvement Checklist

Dựa trên Series 2 (Payment Gateway) và Series 3 Part 4 (Ghost Payment),
đây là những gì hệ thống cần cải thiện.

---

## 1. Idempotency Layer

| # | Tính năng | Trạng thái | Ghi chú |
|---|---|---|---|
| 1.1 | `idempotency_record` table tách riêng khỏi `order` | 🔲 Chưa có | Hiện đang để `idempotency_key` trong bảng `order` |
| 1.2 | `UNIQUE constraint` trên `idempotency_key` | 🔲 Chưa có | Có cột nhưng chưa có constraint |
| 1.3 | `payload_hash` để detect malicious retry | 🔲 Chưa có | Cùng key nhưng đổi amount → phải reject |
| 1.4 | Cache response (`response_body`, `response_status`) để replay y chang | 🔲 Chưa có | Retry phải nhận đúng response gốc |
| 1.5 | `operation_type` phân biệt PAYMENT / REFUND / REVERSAL | 🔲 Chưa có | Cần khi có nhiều flow |
| 1.6 | Redis `SET NX` làm outer shield trước khi vào DB | 🔲 Chưa có | Giảm tải DB khi retry storm |
| 1.7 | TTL policy theo từng loại terminal state | 🔲 Chưa có | SUCCESSFUL 24h, FAILED 1h, FAILED_AFTER_CONFIRMATION 72h |

---

## 2. Schema & Database

| # | Tính năng | Trạng thái | Ghi chú |
|---|---|---|---|
| 2.1 | Bảng `refund_order` riêng cho Refund flow | 🔲 Chưa có | Không dùng chung bảng `order` |
| 2.2 | `reversal_state` + `reversal_txn_ref` trong bảng `order` | 🔲 Chưa có | Track reversal ngay trong order |
| 2.3 | `message_id UUID UNIQUE` trong `order_outbox` | 🔲 Chưa có | Kafka consumer deduplicate |
| 2.4 | UNIQUE constraint trên `(merchant_id, terminal_id, order_ref)` | 🔲 Chưa có | Business-level fingerprint |
| 2.5 | CHECK constraint hoặc enum trên `pairing.state` | 🔲 Chưa có | Tránh invalid state |

---

## 3. State Machine & Order Flow

| # | Tính năng | Trạng thái | Ghi chú |
|---|---|---|---|
| 3.1 | State transition và idempotency record trong cùng 1 `@Transactional` | 🔲 Chưa có | All-or-nothing, không crash giữa chừng |
| 3.2 | Validate-Lock-Commit — không gọi bank bên trong transaction | 🔲 Chưa có | Gọi SIX Mock xong mới open transaction |
| 3.3 | Recovery Sweeper cho order kẹt ở `IN_PROGRESS` / `ADVICE_PENDING` quá TTL | 🔲 Chưa có | Scan định kỳ, resume hoặc compensate |
| 3.4 | State `SETTLED` sau khi reconcile với bank | 🔲 Chưa có | SUCCESSFUL ≠ SETTLED |
| 3.5 | State `CHARGEBACK` và `DISPUTE` | 🔲 Chưa có | Post-settlement conflicts |

---

## 4. Saga & Compensation

| # | Tính năng | Trạng thái | Ghi chú |
|---|---|---|---|
| 4.1 | Compensating transaction cho `FAILED_AFTER_CONFIRMATION` | 🔲 Chưa có | Tự động gửi reversal, không manual |
| 4.2 | Reversal idempotency key = `Hash(authCode + "REVERSAL")` | 🔲 Chưa có | Sweeper retry reversal không bị double |
| 4.3 | Refund flow với idempotency key riêng | 🔲 Chưa có | `Hash(orderId + "FULL_REFUND")` |
| 4.4 | `refund_intent` persist trước khi gọi PSP | 🔲 Chưa có | Crash-safe, retry được |

---

## 5. Ghost Payment — Webhook Handling

| # | Tính năng | Trạng thái | Ghi chú |
|---|---|---|---|
| 5.1 | `webhook_events` table — ingest trước, process sau | 🔲 Chưa có | SIX Mock callback → INSERT raw → 200 OK ngay |
| 5.2 | Async Worker (Saga Orchestrator) poll `webhook_events` | 🔲 Chưa có | Tách ingest khỏi process |
| 5.3 | Conditional update khi webhook đến muộn | 🔲 Chưa có | `UPDATE WHERE status='PENDING' AND expires_at >= NOW()` |
| 5.4 | Auto detect divergence → trigger refund | 🔲 Chưa có | `RowsAffected = 0` → CANCELLED_BY_SWEEPER → refund |
| 5.5 | Optimistic lease pattern cho webhook worker | 🔲 Chưa có | Tránh zombie worker giữ event mãi |
| 5.6 | `payment_idempotency_log` table riêng | 🔲 Chưa có | Audit trail vượt qua PSP key expiry |

---

## 6. Ledger & Financial Correctness

| # | Tính năng | Trạng thái | Ghi chú |
|---|---|---|---|
| 6.1 | Immutable journal (append-only, không UPDATE balance) | 🔲 Chưa có | Double-entry trong Ledger Service |
| 6.2 | Balance là derived state — tính từ journal | 🔲 Chưa có | Không lưu balance trực tiếp |
| 6.3 | Snapshot table để bound balance read | 🔲 Chưa có | Tránh SUM toàn bộ history |
| 6.4 | Pessimistic lock cho merchant wallet (high contention) | 🔲 Chưa có | Flash sale nhiều payment cùng lúc |
| 6.5 | Optimistic lock cho customer wallet (low contention) | 🔲 Chưa có | Ít tranh chấp hơn |

---

## 7. Reconciliation

| # | Tính năng | Trạng thái | Ghi chú |
|---|---|---|---|
| 7.1 | Scheduled polling job check pending status với SIX Mock | 🔲 Chưa có | Không chỉ rely vào webhook |
| 7.2 | Nightly reconciliation engine so sánh internal vs SIX Mock report | 🔲 Chưa có | FULL OUTER JOIN theo `txn_ref` |
| 7.3 | Classify discrepancy: INTERNAL_ONLY / EXTERNAL_ONLY / AMOUNT_MISMATCH | 🔲 Chưa có | |
| 7.4 | Dead Letter Queue cho settlement file lỗi | 🔲 Chưa có | Cô lập poison pill |
| 7.5 | Circuit Breaker khi gọi SIX Mock | 🔲 Chưa có | Tránh cascade failure |

---

## 8. Observability

| # | Tính năng | Trạng thái | Ghi chú |
|---|---|---|---|
| 8.1 | `traceId` xuyên suốt toàn bộ flow | ✅ Có trong API contract | Cần verify implement đúng |
| 8.2 | Alert khi order kẹt state > TTL | 🔲 Chưa có | PagerDuty hoặc tương đương |
| 8.3 | SLI: Time to Compensate (TtC) < 1 giờ p99 | 🔲 Chưa có | Bounded bởi chargeback window 120 ngày |
| 8.4 | Alert khi refund PENDING > 24 giờ | 🔲 Chưa có | Chargeback liability |
| 8.5 | Kafka event đầy đủ cho mọi state transition | ✅ Có trong API contract | Cần verify outbox implement đúng |

---

## 🎯 Priority — Nên làm trước

### 🔴 Must Have — trước khi go-live
1.1  idempotency_record table tách riêng
1.2  UNIQUE constraint trên idempotency_key
1.3  payload_hash detect malicious retry
2.1  refund_order table
2.3  message_id UNIQUE trong order_outbox
3.1  State transition + idempotency trong cùng @Transactional
3.2  Validate-Lock-Commit (không gọi bank trong transaction)
4.1  Auto compensating transaction cho FAILED_AFTER_CONFIRMATION
5.1  webhook_events table
5.2  Async Worker poll webhook_events

### 🟡 Should Have — sprint đầu sau launch
1.6  Redis SET NX outer shield
1.7  TTL policy theo terminal state
3.3  Recovery Sweeper cho order kẹt
4.2  Reversal idempotency key
4.3  Refund idempotency key
4.4  refund_intent persist trước khi gọi PSP
5.3  Conditional update khi webhook đến muộn
5.4  Auto detect divergence → trigger refund
6.1  Immutable journal append-only
6.2  Balance là derived state
7.5  Circuit Breaker khi gọi SIX Mock

### 🟢 Nice to Have — khi scale
1.4  Cache response để replay y chang
1.5  operation_type cho nhiều flow
2.4  UNIQUE constraint business fingerprint
2.5  CHECK constraint pairing.state
3.4  State SETTLED sau reconcile
3.5  State CHARGEBACK và DISPUTE
5.5  Optimistic lease pattern webhook worker
5.6  payment_idempotency_log table
6.3  Snapshot table bound balance read
6.4  Pessimistic lock merchant wallet
6.5  Optimistic lock customer wallet
7.1  Scheduled polling check pending
7.2  Nightly reconciliation engine
7.3  Classify discrepancy
7.4  Dead Letter Queue
8.2  Alert order kẹt > TTL
8.3  SLI Time to Compensate
8.4  Alert refund PENDING > 24

---

## 📌 Legend

| Icon | Ý nghĩa |
|---|---|
| ✅ | Đã có |
| 🔲 | Chưa có |
| 🚧 | Đang làm |
| ⏸️ | Tạm hoãn |
