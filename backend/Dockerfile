
FROM gradle:8.5-jdk21 AS builder

WORKDIR /app


COPY build.gradle settings.gradle ./
COPY gradle ./gradle

RUN gradle dependencies --no-daemon || true

COPY src ./src
RUN gradle bootJar --no-daemon -x test


FROM eclipse-temurin:21-jre-alpine

WORKDIR /app


COPY --from=builder /app/build/libs/*.jar app.jar


EXPOSE 8080


ENTRYPOINT ["java", \
  "-Dspring.profiles.active=prod", \
  "-Dspring.datasource.url=${SPRING_DATASOURCE_URL}", \
  "-Dspring.datasource.username=${SPRING_DATASOURCE_USERNAME}", \
  "-Dspring.datasource.password=${SPRING_DATASOURCE_PASSWORD}", \
  "-Dspring.data.mongodb.uri=${MONGODB_URL}", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]