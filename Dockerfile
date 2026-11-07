FROM eclipse-temurin:21-jre-jammy

# Configuramos el directorio de trabajo
WORKDIR /deployments

# Copiamos la carpeta que generó el comando package
COPY target/quarkus-app/lib/ /deployments/lib/
COPY target/quarkus-app/*.jar /deployments/
COPY target/quarkus-app/app/ /deployments/app/
COPY target/quarkus-app/quarkus/ /deployments/quarkus/

# Railway usa el puerto 8080 por defecto, Quarkus también.
EXPOSE 8090
USER 185
ENV JAVA_OPTS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENTRYPOINT [ "java", "-Dquarkus.http.host=0.0.0.0", "-jar", "/deployments/quarkus-run.jar" ]