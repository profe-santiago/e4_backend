FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN --mount=type=cache,id=gradle-event,target=/root/.gradle \
    sed -i 's/\r$//' gradlew && chmod +x gradlew && ./gradlew bootJar -x test --no-daemon
RUN find build/libs -name "*.jar" ! -name "*-plain.jar" -exec mv {} build/libs/app.jar \;

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/app.jar app.jar
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-Xmx512m", "-jar", "app.jar"]
