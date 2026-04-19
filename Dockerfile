# Stage 1
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml ./

RUN --mount=type=secret,id=github_token mkdir -p /root/.m2 && \
    echo "<settings><servers><server><id>github</id><username>x</username>\
<password>$(cat /run/secrets/github_token)</password></server></servers></settings>" \
    > /root/.m2/settings.xml &&  mvn dependency:go-offline -B &&  rm -f /root/.m2/settings.xml

COPY src ./src
RUN mvn package -DskipTests && rm -f /root/.m2/settings.xml

# Stage 2
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /app/target/*.jar app.jar
USER app
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8084/actuator/health || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
