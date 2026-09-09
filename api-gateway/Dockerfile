FROM maven:3-eclipse-temurin-25 AS build
WORKDIR /workspace
COPY . .
RUN mvn -q -pl api-gateway -am package -DskipTests
FROM eclipse-temurin:25-jre-ubi10-minimal
WORKDIR /app
COPY --from=build /workspace/api-gateway/target/api-gateway-*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
