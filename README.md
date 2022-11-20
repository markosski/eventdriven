# Transactions Processing Service

Purpose of this project is exploration of various event driven techniques such as Event Choreography, Event Carried State Transfer and Event Sourcing.
This project exemplifies simplified card transaction processing system. [Read more about these techniques here](docs/event_driven.md).

**Note: this is a POC, this code should not be used as example of production implementation**

Requirements driving design decisions are:

- system needs to handle high volume of transaction thus network calls to any other internal systems should be limited to minimum 
- transactions need to be stored in a way that would allow auditing

# Capabilities:

## Payments services
- responsible for managing payments
  - normally payment service would perform some rules to verify payment (see below), for simplicity reasons, service will store payment and publish event as a good payment.
    - if payment is low risk, publish payment-submitted event internally for processing
    - if payment ends up bouncing, publish payment-returned event internally for processing

## Account services
- booking new accounts
- maintaining changes to accounts (e.g. credit limit change, personal information etc.)
- change to credit limit will result in publishing state change event

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

## Start All Services Using Containers

This should start all applications and kafka cluster. Once logs stop printing it means all services are connected. Web UI will be availabe at `localhost:9000`.

`docker-compose -f zk-single-kafka-single.yml -f all-apps.yml up`

`docker-compose -f zk-single-kafka-single.yml -f all-apps.yml down`

![alt text](docs/app.png)

## Kafka Related

https://www.oreilly.com/library/view/kafka-the-definitive/9781491936153/ch04.html

https://kafka.apache.org/quickstart

https://github.com/conduktor/kafka-stack-docker-compose

