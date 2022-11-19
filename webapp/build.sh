#!/bin/bash
docker build -t eventdriven-webapp .
docker tag eventdriven-webapp:latest public.ecr.aws/t6m3e8b6/eventdriven-webapp:latest