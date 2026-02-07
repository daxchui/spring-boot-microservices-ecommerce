#### Transfer Operations
```bash
# Create a new transfer
POST /api/transfers
Headers: Idempotency-Key: <unique-key>
Body: {
  "fromAccountId": 2,
  "toAccountId": 1,
  "amount": 100.00,
  "currency": "AUD",
  "reference": "Order payment #123",
  "orderId": 123
}

# Get transfer status
GET /api/transfers/{transferId}
```

#### Refund Operations
```bash
# Create a refund
POST /api/refunds
Headers: Idempotency-Key: <unique-key>
Body: {
"originalTransferId": 1,
"reason": "Customer cancellation",
"orderId": 123
}
```