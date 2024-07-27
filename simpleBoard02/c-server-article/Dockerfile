FROM openjdk:19-ea-jdk-slim
RUN apt update && apt install -y curl
COPY build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]