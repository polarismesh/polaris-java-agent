FROM eclipse-temurin:17-jre
ARG file_name
WORKDIR /app
COPY target/${file_name} /app/main.jar
ENTRYPOINT ["java", "-jar", "/app/main.jar"]
