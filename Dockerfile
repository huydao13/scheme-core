FROM maven:3.9-eclipse-temurin-17 AS build
ARG MODULE
WORKDIR /app

# Copy parent pom + tất cả module pom
COPY pom.xml .
COPY customer-service/pom.xml customer-service/

# Copy source của module cần build
COPY ${MODULE}/src ${MODULE}/src

RUN mvn -q -pl ${MODULE} -am clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
ARG MODULE
WORKDIR /app
COPY --from=build /app/${MODULE}/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

# ENTRYPOINT ["java", \
#   "-XX:+UseZGC", \
#   "-XX:MaxRAMPercentage=75.0", \
#   "-Djava.security.egd=file:/dev/./urandom", \
#   "-jar", "app.jar"]
