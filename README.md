# Transactions Processing Service

Purpose of this project is exploration of various event driven techniques such as Event Choreography, Event Carried State Transfer and Event Sourcing.
This project exemplifies simplified purchase transaction processing system. [Read more about these techniques here](docs/event_driven.md).

**Note: this is a POC, this code should not be used as example of production implementation**

Requirements driving design decisions are:

- system needs to handle high volume of transaction; network calls to any other internal systems should be limited to minimum 
- processed transactions need to be stored in a way that would allow auditing

# Capabilities:

## Payments services (not implemented)
- responsible for managing payments
- if payment is low risk, publish payment-submitted event internally for processing
- if payment ends up bouncing, publish payment-returned event internally for processing

## Account services (not implemented)
- booking new accounts
- maintaining changes to accounts (e.g. credit limit change, personal information etc.)
- change to account state is published as change event

## Transaction processing platform
- implemented as event sourced system
  - purchases and payments are stored as events and used to reconstruct account state
  - every command that results in creation of event is also published back to message bus for other parties to consume
- exposes API endpoint for external service to request process transaction (underwriting)
- exposes API endpoint for external service to request current state of account balance
- listens to payments platform and applies payment events (payments submitted and returned)
- listens to account change events to maintain local state of accounts (account credit limit update)
  - reason to maintain local state is to avoid expensive calls to Account service 
- after transaction was decisioned, event is published with decision results; same information is used in response to API call

## Not in scope / other considerations
- snapshots for transaction events, it is common to snapshot events in EventStore to improve performance when rebuilding states from events
- ability to rebuild state of local account information stored in Transactions service
- ensure processing events is idempotent - prevent processing event twice

## System Design Diagram

![alt text](docs/system_diagram.png)

## Kafka Setup
https://www.oreilly.com/library/view/kafka-the-definitive/9781491936153/ch04.html
https://kafka.apache.org/quickstart

https://github.com/conduktor/kafka-stack-docker-compose

`docker-compose -f zk-single-kafka-single.yml up`

## Publishing events to Kafka

You can either install Conduktor or download Kafka package which contains consumer/producer bash scripts. Use payloads below to publish events.

## Usage

Start Transactions service `sbt "project transactions; run"` 

Start Kafka server `docker-compose -f zk-single-kafka-single.yml up`

Get account state

```bash
curl -XGET http://localhost:8080/account-summary/123
```

Submit transaction for processing

```bash
curl -XPOST -H "Content-Type: application/json" http://localhost:8080/process-purchase-transaction -d \
'{"cardNumber": 12345678, "transactionId": 4, "amount": 40000, "merchantCode": "ABC", "zipOrPostal": "80126", "countryCode": 1}'
```

Publish PaymentSubmitted event to topic `paymentSubmitted`

```json
{"payload": {"accountId": 123, "paymentId": "123", "amount": 200, "recordedTimestamp": 1658108329}, "eventId": "123", "eventTimestamp": 1658108328}
```

Publish PaymentReturned event to topic `paymentReturned`

```json
{"payload": {"accountId": 123, "paymentId": "123", "amount": 200, "reason": "no sufficient funds", "recordedTimestamp": 1658108329}, "eventId": "123", "eventTimestamp": 1658108328}
```

Publish AccountCreditLimitUpdated event to topic `accountCreditLimitUpdates`

```json
{"payload": {"accountId": 123, "oldCreditLimit": 50000, "newCreditLimit": 60000, "recordedTimestamp": 1658108329}, "eventId": "123", "eventTimestamp": 1658108328}
```
