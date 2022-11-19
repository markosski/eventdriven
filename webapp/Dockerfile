FROM adoptopenjdk/openjdk11:jre-11.0.16.1_1-alpine
WORKDIR /app

ARG JAVA_OPTS
ENV JAVA_OPTS=$JAVA_OPTS
COPY target/scala-2.13/webapp-assembly-* /app/app.jar
EXPOSE 9000
ENTRYPOINT exec java -XX:+UnlockExperimentalVMOptions $JAVA_OPTS -jar /app/app.jar