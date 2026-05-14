FROM eclipse-temurin:25-jre-noble
WORKDIR /app
ARG JAR_FILE=target/user-manager-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} /app/app.jar
EXPOSE 8080 9090
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
