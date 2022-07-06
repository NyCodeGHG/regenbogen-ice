FROM gradle:jdk18 as builder
WORKDIR /usr/app
COPY . .
RUN gradle --no-daemon installBotArchive

FROM eclipse-temurin:18-jre-alpine
WORKDIR /usr/app
COPY --from=builder /usr/app/build/installBot .

LABEL org.opencontainers.image.source = "https://github.com/mikbot/regenbogen-ice"

ENTRYPOINT ["/usr/app/bin/mikmusic"]
