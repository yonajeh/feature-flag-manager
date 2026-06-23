# Multi-stage build: Angular UI + Quarkus backend in one container

# --- Stage 1: Build Angular admin UI ---
FROM node:20-alpine AS web
WORKDIR /build
COPY apps/frontend/package*.json ./
RUN npm ci
COPY apps/frontend/ ./
RUN npm run build -- --configuration production

# --- Stage 2: Build Quarkus application ---
FROM maven:3.9-eclipse-temurin-21 AS app
WORKDIR /build
COPY apps/backend/pom.xml apps/backend/
COPY .mvn .mvn
COPY apps/backend/src apps/backend/src
COPY api/openapi.yaml api/openapi.yaml
# Copy Angular dist into Quarkus static resources
COPY --from=web /build/dist/frontend/ apps/backend/src/main/resources/META-INF/resources/
RUN mvn -f apps/backend/pom.xml -B package -DskipTests

# --- Stage 3: Minimal runtime ---
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app
RUN addgroup -S ffm && adduser -S ffm -G ffm
RUN mkdir -p /data && chown ffm:ffm /data
COPY --from=app /build/apps/backend/target/quarkus-app/ ./
USER ffm
EXPOSE 8080
ENV QUARKUS_PROFILE=prod
ENV FF_DB_PATH=/data/ffm.db
VOLUME /data
ENTRYPOINT ["java", "-jar", "quarkus-run.jar"]
