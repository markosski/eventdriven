java -javaagent:opentelemetry-javaagent.jar \
    -Dotel.resource.attributes=service.name=transactions \
    -Dotel.traces.exporter=zipkin \
    -jar transactions/target/scala-2.13/transactions-assembly-*