FROM maven:3-eclipse-temurin-25 AS build
WORKDIR /workspace
COPY pom.xml .
COPY eureka-server/pom.xml eureka-server/pom.xml
COPY config-server/pom.xml config-server/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY class-service/pom.xml class-service/pom.xml
COPY booking-service/pom.xml booking-service/pom.xml
COPY payment-service/pom.xml payment-service/pom.xml
COPY notification-service/pom.xml notification-service/pom.xml
COPY . .
RUN mvn -q -DskipTests package

FROM eclipse-temurin:25-jre-ubi10-minimal
ARG SERVICE
WORKDIR /app
COPY --from=build /workspace/${SERVICE}/target/${SERVICE}-*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
