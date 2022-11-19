FROM adoptopenjdk/openjdk11:jre-11.0.16.1_1-alpine
WORKDIR /app

ARG JAVA_OPTS
ENV JAVA_OPTS=$JAVA_OPTS
COPY target/scala-2.13/transactions-assembly-0.1.0-SNAPSHOT.jar /app/app.jar
EXPOSE 8080
EXPOSE 19092
ENTRYPOINT exec java -XX:+UnlockExperimentalVMOptions $JAVA_OPTS -jar /app/app.jar