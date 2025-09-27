FROM eclipse-temurin:21-jdk AS build
WORKDIR /src
COPY . .
RUN ./mvnw -q -DskipTests package

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
USER app
WORKDIR /app
COPY --from=build /src/customer-service/target/customer-service-*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -jar app.jar"]