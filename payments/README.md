# Payments Service

This service is not implemented, however Transactions service does handle payment events that would normally be published by this service.

## Submit Events
`~/kafka_2.13-3.2.0/bin/kafka-console-producer.sh --topic paymentSubmitted --bootstrap-server localhost:19092`

### Payment Submitted

`{"accountId": 123, "paymentId": "123", "amount": 200}`

### Payment Returned

`{"accountId": 123, "paymentId": "123", "amount": 200}`
