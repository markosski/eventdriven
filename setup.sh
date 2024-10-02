#!/bin/bash

# Download otel java agent
curl -L -O https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar

# Download SDKMAN if needed
if ! command -v sdk 2>&1 >/dev/null
then
  curl -s "https://get.sdkman.io" | bash
fi

# Install SBT
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install sbt 1.6.2

# Build Project
sbt assembly
