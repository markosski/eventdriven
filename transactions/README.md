# Transactions Processing Service

## Use cases:
- get a list of recent transactions for customer, including payments
- get a summary, account balance

```
curl -XPOST -H "Content-Type: application/json" http://localhost:8080/process-transaction -d \
'{"account": 123, "card": 123456789, "amount": 2099, "merchantCode": "WLMRT", "zip": "80126", "countryCode": 0}'
```

## TODO
- recover application state to point in time