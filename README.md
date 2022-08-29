# Transactions Processing Service

Purpose of this project is exploration of various event driven techniques such as Event Choreography, Event Carried State Transfer and Event Sourcing.
This project exemplifies simplified credit card decisioning system. 
Requirements driving design decisions are:

- process if decisioning transactions need to handle high volume of requests; there cannot be any intermediate calls
  to other internal services while handling this request
- processed transactions need to be stored in a way that would allow auditing

# Capabilities:

## Payments services
- responsible for receiving payments
- if payments is low risk announce the payment
- there is chance payment may bounce, announce returned payment

## Account services
- booking new accounts
- maintaining changes to accounts (e.g. credit limit, personal information etc.)
- change to account state is published as change event

## Transaction processing platform
- implemented
- exposes API endpoint for external service to request transaction decisioning (underwriting)
- exposes API endpoint for external service to request current state of account balance
- listens to payments platform and applies payment events
- listens to account change events to maintain local state of accounts
  - reason to maintain local state is to avoid expensive calls to Account service 
- after transaction was decisioned, event is published with decision results; same information is used in response to API call

## Not in scope
- actual implementation of Payments service
- actual implementation of Accounts service
- snapshotting events in Event Store in Transactions service
- ability to rebuild state of local account information stored in Transactions service

## System Design Diagram

![alt text](docs/system_diagram.png)

## Kafka Setup
https://www.oreilly.com/library/view/kafka-the-definitive/9781491936153/ch04.html
https://kafka.apache.org/quickstart

https://github.com/conduktor/kafka-stack-docker-compose

`docker-compose -f zk-single-kafka-single.yml up`

## Publishing events to Kafka

You can either install Conduktor or download Kafka package which contains consumer/producer bash scripts.

## Usage

Start Transactions service `project transactions; run` 

Start Kafka server `docker-compose -f zk-single-kafka-single.yml up`

Get account state

```bash
curl -XGET http://localhost:8080/account-summary/123
```

Submit transaction for processing

```bash
curl -XPOST -H "Content-Type: application/json" http://localhost:8080/process-purchase-transaction -d \
'{"cardNumber": 12345678, "amount": 40000, "merchantCode": "ABC", "zipOrPostal": "80126", "countryCode": 1}'
```

Publish PaymentSubmitted event to topic `paymentSubmitted`

```json
{"payload": {"accountId": 123, "paymentId": "123", "amount": 200, "recordedTimestamp": 1658108329}, "eventId": "123", "eventTimestamp": 1658108328}
```

Publish PaymentReturned event to topic `paymentReturned`

```json
{"payload": {"accountId": 123, "paymentId": "123", "amount": 200, "recordedTimestamp": 1658108329}, "eventId": "123", "eventTimestamp": 1658108328}
```

Publish AccountCreditLimitUpdated event to topic `accountCreditLimitUpdates`

```json
{"payload": {"accountId": 123, "oldCreditLimit": 50000, "newCreditLimit": 60000, "recordedTimestamp": 1658108329}, "eventId": "123", "eventTimestamp": 1658108328}
```