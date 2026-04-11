FROM maven:3.9.12-eclipse-temurin-21-noble AS builder
WORKDIR /app

COPY pom.xml pom.xml
COPY src src

RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    mvn -q -T 2C -DskipTests --no-transfer-progress clean package && \
    JAR="$(ls -1 target/customer-service-*.jar | head -n 1)" && \
    java -Djarmode=layertools -jar "$JAR" extract

FROM eclipse-temurin:21.0.10_7-jre-noble
WORKDIR /app

RUN useradd -r -u 10001 -g root -s /bin/false -d /app appuser && \
    mkdir -p /app/logs && \
    chown -R 10001:0 /app

LABEL org.opencontainers.image.title="customer-service-sb4" \
      org.opencontainers.image.description="Customer Service Spring Boot 4" \
      org.opencontainers.image.source="https://github.com/blueprint-platform/openapi-generics" \
      org.opencontainers.image.licenses="MIT"

COPY --from=builder --chown=10001:0 /app/dependencies/ ./
COPY --from=builder --chown=10001:0 /app/snapshot-dependencies/ ./
COPY --from=builder --chown=10001:0 /app/spring-boot-loader/ ./
COPY --from=builder --chown=10001:0 /app/application/ ./

USER 10001

ENV SPRING_PROFILES_ACTIVE=default \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseG1GC -Dfile.encoding=UTF-8"

EXPOSE 8085
ENTRYPOINT ["java","org.springframework.boot.loader.launch.JarLauncher"]