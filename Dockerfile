# ── Stage 1: dependency cache ─────────────────────────────────────────────
FROM maven:3.9.12-eclipse-temurin-21 AS deps
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline

# ── Stage 2: production build ─────────────────────────────────────────────
FROM deps AS build
COPY src ./src
RUN mvn -B package -DskipTests

# ── Stage 3: production image (lean JRE) ──────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS production
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=build /app/target/*.jar app.jar

RUN chown appuser:appgroup app.jar

USER appuser

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

# ── Stage 4: development image (hot reload via spring-boot:run) ───────────
FROM deps AS development
COPY src ./src
EXPOSE 8080
ENTRYPOINT ["mvn", "spring-boot:run", "-Dspring-boot.run.jvmArguments=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"]