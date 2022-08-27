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

## Challenges
- how to deal with cases where event was stored but not published, due to e.g. network error?
  - we can have functionality that will re-drive events stored in ES but not published, but how will consumers deal with it?

## Type of Events

### Notification
These are lightweight events that generally are used to only notify downstream consumers that something happened.
For example when user update their address, event will just contain information about user ID.
If downstream application is interested in more information, then additional network calls need to be made to User
service to get that information. 
The benefit of this pattern is that events are very small thus don't take much network bandwidth.
On the downside, the need of calling upstream service for more that couples both services and upstream services needs
to be designed to handle larger traffic.

### Delta Events

These events will contain more information that Notification event, but not as mach as Fat Events.
For example when User updates address, the event will contain only the bits of data that changed.

### Fat Events 

Fat Events generally contain way more information than Notification Event. 
The goal is to be able to provide all sufficient information to consumers so that they don't have to make additional calls to other services.
For example, when user updates their address, Fat Event may include full previous address and new address.
Consumer may not need to make additional network call to upstream service since all the data is available as part of event.
Contrasting this with Notification event, the downside is that we need to send more data over the network, 
and on the upside, no additional calls need to be made to upstream which means service does not have to be designed to handle larger traffic.
Fat events are often used in Event Carried State Transfer

### Event Carried State Transfer

Technique to construct and maintain local state of some domain data solely based on external events.
For example, shipping service instead of making call to User service to get address, can listen to 
UserCreated and UserUpdated events, and persist this information in its local database.
Later when receiving event about payment completed for some order with user ID, shipping already knows where to ship product.
There are some challenges with this approach:
1) since local state is constructed from events, eventual consistency is introduced
2) another problem is with re-building local state, in case of either data loss, or even when standing up new service - state has to be re-built first.

For 2) various techniques can be used:

* Re-consume an available stream of persisted stateful events. 

This can only work if streaming technology used has all the events available and can replay all of them.

* A data migration from the “source of truth” microservice(s)

This can be accomplished by executing batch jobs that do daily state aggregations. 
When standing up new service, database state can be built from the aggregated data, and then final state
can be created by re-consuming events from last N hours. 

* A transition period in which the consumers build state over time. 

In this approach when state does not exist for some ID, application will make a call (only once per ID) to upstream service to retrieve state.
The downside here is that, we are introducing runtime dependency and coupling between two services.

# Kafka Setup
https://www.oreilly.com/library/view/kafka-the-definitive/9781491936153/ch04.html
https://kafka.apache.org/quickstart

`https://github.com/conduktor/kafka-stack-docker-compose`

Cluster on port `localhost:9092`

## CLI

`curl -sL --http1.1 https://cnfl.io/cli | sh -s -- latest`

## Docker

`docker-compose -f zk-single-kafka-single.yml up`