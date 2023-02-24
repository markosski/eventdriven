#!/bin/bash
docker build -t eventdriven-accounts .
docker tag eventdriven-accounts:latest public.ecr.aws/t6m3e8b6/eventdriven-accounts:latest