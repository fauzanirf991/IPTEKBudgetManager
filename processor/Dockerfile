FROM eclipse-temurin:21-jre
WORKDIR /app
COPY processor/build/install/processor/ /app/
ENV PORT=8080
EXPOSE 8080
USER 10001
ENTRYPOINT ["/app/bin/processor"]
