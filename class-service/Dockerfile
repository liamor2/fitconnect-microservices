FROM maven:3-eclipse-temurin-25 AS build
WORKDIR /workspace
COPY . .
RUN mvn -q -pl class-service -am package -DskipTests
FROM eclipse-temurin:25-jre-ubi10-minimal
WORKDIR /app
COPY --from=build /workspace/class-service/target/class-service-*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
