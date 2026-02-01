# Build stage
FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /build

# Copy parent pom and install it (non-recursive to skip module validation)
COPY pom.xml ./pom.xml
RUN mvn install -N -DskipTests -B

# Copy and build lms-common
COPY services/lms-common ./services/lms-common
RUN mvn install -f services/lms-common/pom.xml -DskipTests -B

# Copy and build this service
COPY services/api-gateway ./services/api-gateway
RUN mvn package -f services/api-gateway/pom.xml -DskipTests -B

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
RUN addgroup -g 1001 -S appgroup && adduser -u 1001 -S appuser -G appgroup
WORKDIR /app
COPY --from=builder /build/services/api-gateway/target/*.jar app.jar
RUN chown -R appuser:appgroup /app
USER appuser
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
