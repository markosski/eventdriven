java \
-javaagent:opentelemetry-javaagent.jar \
-Dotel.resource.attributes=service.name=payments \
-Dotel.traces.exporter=zipkin \
-jar payments/target/scala-2.13/payments-assembly-*