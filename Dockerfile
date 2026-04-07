FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN apt-get update && apt-get install -y maven && \
    mvn clean package -DskipTests

FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=build /app/target/vk_java_internship-1.0-SNAPSHOT.jar app.jar

ENV GRPC_PORT=8090

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]