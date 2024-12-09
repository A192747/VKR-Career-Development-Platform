FROM maven:3.9.0-eclipse-temurin-19 as builder
WORKDIR /app
COPY . /app/.
RUN mvn -f /app/pom.xml clean package -Dmaven.test.skip=true

FROM eclipse-temurin:19-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar /app/*.jar
COPY --from=builder /app/src/main/resources/ /app/

ENTRYPOINT ["java", "-jar", "/app/*.jar"]