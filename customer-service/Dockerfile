FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY target/customer-service-*.jar app.jar

ENV APP_PORT=8084 \
    SPRING_PROFILES_ACTIVE=local \
    JAVA_OPTS=""

EXPOSE 8084

ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -Dserver.port=${APP_PORT} -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE} -jar app.jar"]