FROM openjdk:17-jdk-alpine

COPY target/optimum-cms-0.0.1-SNAPSHOT.jar /app-service/optimum-arts.jar
WORKDIR /app-service
ENTRYPOINT [ "java", "-jar", "optimum-arts.jar" ]