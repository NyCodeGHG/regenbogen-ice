FROM gradle:7.6-jdk19-alpine as builder
WORKDIR /usr/app
COPY . .
RUN gradle --no-daemon installBotArchive

FROM eclipse-temurin:19-jre-alpine
WORKDIR /usr/app
COPY --from=builder /usr/app/bot/build/installBot .

LABEL org.opencontainers.image.source = "https://github.com/mikbot/regenbogen-ice"

ENTRYPOINT ["/usr/app/bin/mikmusic"]
