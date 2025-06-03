# Use an official lightweight JDK runtime as base
FROM eclipse-temurin:21-jre

# Set working directory
WORKDIR /app

# Copy the JAR into the container
COPY target/uf-temperature-1.0.0.jar app.jar

# Set environment variables if needed (optional)
# ENV SPRING_PROFILES_ACTIVE=dev

# Run the JAR
ENTRYPOINT ["java", "-jar", "app.jar"]
