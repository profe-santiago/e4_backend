FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew && ./gradlew bootJar -x test --no-daemon
RUN find build/libs -name "*.jar" ! -name "*-plain.jar" -exec mv {} build/libs/app.jar \;

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
WORKDIR /app
COPY --from=builder /app/build/libs/app.jar app.jar
HEALTHCHECK --interval=15s --timeout=5s --start-period=150s --retries=5 \
  CMD wget --no-verbose --tries=1 --spider http://127.0.0.1:8084/actuator/health || exit 1
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-Xmx512m", "-jar", "app.jar"]
