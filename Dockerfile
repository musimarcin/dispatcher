# Stage 1: Build
FROM maven:3.9.0-eclipse-temurin-17 AS build
WORKDIR /app

# Copy everything
COPY pom.xml .
COPY api ./api
COPY service ./service
COPY persistence ./persistence
COPY security ./security

# Build api module and dependencies
RUN mvn clean package -pl api -am -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jdk
WORKDIR /app

COPY --from=build /app/api/target/api-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
