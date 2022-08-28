# Transactions Processing Service

## Use cases:
- get a list of recent transactions for customer, including payments
- get a summary, account balance

```
curl -XPOST -H "Content-Type: application/json" http://localhost:8080/process-transaction -d \
'{"account": 123, "card": 123456789, "amount": 2099, "merchantCode": "WLMRT", "zip": "80126", "countryCode": 0}'
```

Transaction Payload

```json
{"payload": {"cardNumber": 12345678, "amount": 4000, "merchantCode": "ABC", "zipOrPostal": "80126", "countryCode": 1}, "eventId": "123", "eventName": "preDecisionedTransactionRequest", "eventTimestamp": 100}
```

Payment Approved Payload

```json
{"payload": {"accountId": 123, "paymentId": "123", "amount": 200, "recordedTimestamp": 1658108329}, "eventId": "123", "eventName": "paymentSubmitted", "eventTimestamp": 1658108328}
```

Payment Returned Payload

```json
{"payload": {"accountId": 123, "paymentId": "123", "amount": 200, "recordedTimestamp": 1658108329}, "eventId": "123", "eventName": "paymentReturned", "eventTimestamp": 1658108328}
```

## TODO
- recover application state to point in time