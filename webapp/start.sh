APPLICATION_SECRET="0OXBwcIb4MA/QjAAUlmfLRviYLJ9G10opfMga7r4cG8=" \
java \
-javaagent:opentelemetry-javaagent.jar \
-Dotel.resource.attributes=service.name=web \
-Dotel.traces.exporter=zipkin \
-jar webapp/target/scala-2.13/webapp-assembly-*