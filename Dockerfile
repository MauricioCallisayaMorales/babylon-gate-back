# Etapa de compilación
FROM registry.access.redhat.com/ubi8/openjdk-17:1.14 as builder
WORKDIR /app
COPY . .
RUN ./mvnw package -DskipTests

# Etapa de ejecución
FROM registry.access.redhat.com/ubi8/openjdk-17:1.14
ENV LANGUAGE='en_US:en'
COPY --from=builder /app/target/quarkus-app/lib/ /deployments/lib/
COPY --from=builder /app/target/quarkus-app/*.jar /deployments/
COPY --from=builder /app/target/quarkus-app/app/ /deployments/app/
COPY --from=builder /app/target/quarkus-app/quarkus/ /deployments/quarkus/
EXPOSE 8080
USER 185
ENV JAVA_OPTS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"