#!/bin/zsh

KAFKA_HOST=localhost:19092
docker exec broker kafka-topics \
  --bootstrap-server $KAFKA_HOST \
  --create \
  --topic transactionDecisioned
docker exec broker kafka-topics \
  --bootstrap-server $KAFKA_HOST \
  --create \
  --topic processTransaction
docker exec broker kafka-topics \
  --bootstrap-server $KAFKA_HOST \
  --create \
  --topic paymentReturned
docker exec broker kafka-topics \
  --bootstrap-server $KAFKA_HOST \
  --create \
  --topic accountCreated
docker exec broker kafka-topics \
  --bootstrap-server $KAFKA_HOST \
  --create \
  --topic accountUpdatedAddress
docker exec broker kafka-topics \
  --bootstrap-server $KAFKA_HOST \
  --create \
  --topic accountUpdatedCreditLimit
