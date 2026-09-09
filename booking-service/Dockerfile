FROM maven:3-eclipse-temurin-25 AS build
WORKDIR /workspace
COPY . .
RUN mvn -q -pl booking-service -am package -DskipTests
FROM eclipse-temurin:25-jre-ubi10-minimal
WORKDIR /app
COPY --from=build /workspace/booking-service/target/booking-service-*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
