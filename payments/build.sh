#!/bin/bash
docker build -t eventdriven-payments .
docker tag eventdriven-payments:latest public.ecr.aws/t6m3e8b6/eventdriven-payments:latest