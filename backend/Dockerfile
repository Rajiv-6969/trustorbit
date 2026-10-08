# Build context is the REPO ROOT (so the image can include data/samples).
#   docker build -f backend/Dockerfile -t trustorbit-backend .

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY data/stations.csv data/stations.csv
COPY backend/pom.xml backend/pom.xml
RUN mvn -B -q -f backend/pom.xml dependency:go-offline
COPY backend/src backend/src
RUN mvn -B -q -f backend/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 1001 orbit
COPY --from=build /src/backend/target/trustorbit-backend.jar app.jar
COPY data /app/data
USER orbit
ENV TRUSTORBIT_DATA_DIR=/app/data \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
