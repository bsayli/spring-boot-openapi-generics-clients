FROM maven:3.9.12-eclipse-temurin-21-noble AS builder
WORKDIR /workspace

COPY pom.xml pom.xml

COPY openapi-generics-contract openapi-generics-contract
COPY openapi-generics-platform-bom openapi-generics-platform-bom
COPY openapi-generics-server-starter openapi-generics-server-starter
COPY openapi-generics-java-codegen openapi-generics-java-codegen
COPY openapi-generics-java-codegen-parent openapi-generics-java-codegen-parent

COPY samples/pom.xml samples/pom.xml
COPY samples/domain-contracts samples/domain-contracts
COPY samples/spring-boot-4 samples/spring-boot-4

RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    mvn -U -q -T 2C \
        -DskipTests \
        --no-transfer-progress \
        -pl openapi-generics-contract,\
openapi-generics-platform-bom,\
openapi-generics-server-starter,\
openapi-generics-java-codegen,\
openapi-generics-java-codegen-parent \
        -am clean install && \
    mvn -f samples/pom.xml \
        -U -q \
        -N \
        --no-transfer-progress \
        install && \
    mvn -f samples/domain-contracts/pom.xml \
        -U -q \
        -DskipTests \
        --no-transfer-progress \
        clean install && \
    mvn -f samples/spring-boot-4/pom.xml \
        -U -q -T 2C \
        -DskipTests \
        --no-transfer-progress \
        -pl customer-service \
        -am clean package && \
    JAR="$(find samples/spring-boot-4/customer-service/target \
        -name 'customer-service-*.jar' \
        ! -name '*.original' \
        | head -n 1)" && \
    cp "$JAR" /workspace/application.jar && \
    mkdir -p /layers && \
    java -Djarmode=tools \
        -jar /workspace/application.jar \
        extract \
        --layers \
        --destination /layers

FROM eclipse-temurin:21.0.10_7-jre-noble
WORKDIR /app

RUN useradd -r -u 10001 -g root -s /bin/false -d /app appuser && \
    mkdir -p /app/logs && \
    chown -R 10001:0 /app

LABEL org.opencontainers.image.title="customer-service-sb4" \
      org.opencontainers.image.description="Customer Service Spring Boot 4" \
      org.opencontainers.image.source="https://github.com/blueprint-platform/openapi-generics" \
      org.opencontainers.image.licenses="MIT"

COPY --from=builder --chown=10001:0 /layers/dependencies/ ./
COPY --from=builder --chown=10001:0 /layers/spring-boot-loader/ ./
COPY --from=builder --chown=10001:0 /layers/snapshot-dependencies/ ./
COPY --from=builder --chown=10001:0 /layers/application/ ./

USER 10001

ENV SPRING_PROFILES_ACTIVE=default
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseG1GC -Dfile.encoding=UTF-8"

EXPOSE 8094

ENTRYPOINT ["java", "-jar", "application.jar"]