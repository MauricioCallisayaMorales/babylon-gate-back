# Etapa 1: usar imagen oficial con Maven
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY . .
RUN mvn package -DskipTests
ENV MAVEN_OPTS="-Xmx512m -XX:MaxMetaspaceSize=256m"
# Etapa 2: runtime
FROM eclipse-temurin:21-jre-jammy
WORKDIR /deployments
COPY --from=builder /app/target/*-runner.jar /deployments/app.jar
EXPOSE 8080
CMD ["java", "-jar", "/deployments/app.jar"]