# eventdriven exploration

This repo is for the purpose of exploring various event driven solutions such as Event Choreography, Event Sourcing and Change Data Capture.

`account` module is implemented using CDC pattern with outbox pattern

`transactions` module is implemented with ES pattern

`reactor` application that handles events from apps above. 
    Essentially an example of one of the downstream apps in the system.

`ui` simple web application to interact with applications

## Commands
Commands are instructions sent by clients, it's a request to modify state.

## Events
Events are a result of state becoming successfully modified

## Aggregates
https://domaincentric.net/blog/event-sourcing-aggregates-vs-projections
https://danielwhittaker.me/2014/11/15/aggregate-root-cqrs-event-sourcing/

- aggregates contain business rules that check if command can be executed, if it can, the result will be an event.
- aggregates will store data in EventStore and only then emit an event to the world.
- should handle duplicate commands

## EventStore
Event store is a database designed and optimized to store raw event data.

## Projections
https://domaincentric.net/blog/event-sourcing-projections#:~:text=Projections%20are%20one%20of%20the%20core%20patterns%20used,so%20that%20any%20subsequent%20requests%20can%20be%20handled.

CQRS pattern to present data for querying different ways depending on the client that needs it.

## Other resources
https://engineering.teko.vn/order-management-system-part-2-a-tour-in-system-design/
https://martinfowler.com/bliki/DDD_Aggregate.html


# Kafka Setup
https://www.oreilly.com/library/view/kafka-the-definitive/9781491936153/ch04.html
https://kafka.apache.org/quickstart

`https://github.com/conduktor/kafka-stack-docker-compose`

Cluster on port `localhost:9092`

## CLI

`curl -sL --http1.1 https://cnfl.io/cli | sh -s -- latest`

## Docker

`docker-compose -f zk-single-kafka-single.yml up`