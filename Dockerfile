FROM maven:3.9.9-eclipse-temurin-21

WORKDIR /app

COPY pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src

ENV PORT=8080
ENV SQLITE_DB_PATH=/data/raterr.db

EXPOSE 8080
VOLUME ["/data"]

CMD ["mvn", "-DskipTests", "compile", "exec:java"]

