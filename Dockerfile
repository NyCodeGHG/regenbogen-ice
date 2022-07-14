FROM eclipse-temurin:18-jdk-alpine as builder
WORKDIR /usr/app
COPY . .
RUN ./gradlew --no-daemon installBotArchive

FROM eclipse-temurin:18-jre-alpine
WORKDIR /usr/app
COPY --from=builder /usr/app/bot/build/installBot .

LABEL org.opencontainers.image.source = "https://github.com/mikbot/regenbogen-ice"

ENTRYPOINT ["/usr/app/bin/mikmusic"]
