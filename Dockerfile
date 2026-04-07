# IdleStables (Railway) — build + run the Kotlin Ktor backend in server-ktor/

FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copy only the Ktor backend (keeps build context smaller)
COPY server-ktor ./server-ktor

# Fix potential CRLF issues on Windows checkouts (breaks Linux shebang)
RUN sed -i 's/\r$//' server-ktor/gradlew && chmod +x server-ktor/gradlew

WORKDIR /app/server-ktor
RUN ./gradlew --no-daemon installDist


FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/server-ktor/build/install/server-ktor ./server-ktor
WORKDIR /app/server-ktor

# Railway injects PORT, but we keep a default
ENV PORT=8080
EXPOSE 8080

CMD ["./bin/server-ktor"]
