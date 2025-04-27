FROM maven:3.9.9-eclipse-temurin-17-alpine as builder
WORKDIR /app
COPY . /app/.
RUN mvn -f /app/pom.xml clean package -Dmaven.test.skip=true

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar /app/*.jar
COPY --from=builder /app/src/main/resources/ /app/

ENTRYPOINT ["java", "-jar", "/app/*.jar"]