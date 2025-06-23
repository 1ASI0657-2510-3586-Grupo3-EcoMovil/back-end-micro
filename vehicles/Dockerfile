# Multi-stage build for Vehicles Service
FROM maven:3.9.8-eclipse-temurin-21 AS build
# Set working directory
WORKDIR /app
# Copy pom.xml first for dependency caching
COPY pom.xml .
# Download dependencies
RUN mvn dependency:go-offline -B
# Copy source code
COPY src ./src
# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre
# Create app user for security
RUN addgroup --system spring && adduser --system spring --ingroup spring
# Set working directory
WORKDIR /app
# Copy the built jar from build stage
COPY --from=build /app/target/*.jar app.jar
# Change ownership to spring user
RUN chown -R spring:spring /app
USER spring
# Expose port
EXPOSE 8083
# Add health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8083/actuator/health || exit 1
# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
