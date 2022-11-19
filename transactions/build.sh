#!/bin/bash
docker build -t eventdriven-transactions .
docker tag eventdriven-transactions:latest public.ecr.aws/t6m3e8b6/eventdriven-transactions:latest