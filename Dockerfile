FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy gradle wrapper and build files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Copy source code
COPY src src

# Make gradlew executable
RUN chmod +x ./gradlew

# Build the application
RUN ./gradlew build -x test

# Expose port
EXPOSE 10000

# Set environment variable for port
ENV PORT=10000

# Run the application
CMD ["java", "-jar", "build/libs/personal-finance-manager-0.0.1-SNAPSHOT.jar"]