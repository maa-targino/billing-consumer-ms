FROM eclipse-temurin:25-jre

WORKDIR /app
COPY target/billing-consumer-ms-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
