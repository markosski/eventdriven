java \
-javaagent:opentelemetry-javaagent.jar \
-Dotel.resource.attributes=service.name=accounts \
-Dotel.traces.exporter=zipkin \
-jar accounts/target/scala-2.13/accounts-assembly-*