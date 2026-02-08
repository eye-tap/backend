# Build application
FROM alpine AS build

RUN apk add openjdk21-jdk maven

COPY . /build
WORKDIR /build
RUN mvn package -DskipTests


# Production image
FROM alpine AS prod
LABEL maintainer="eyetap"
LABEL version="2.0.0"

RUN apk add openjdk21-jre-headless

COPY --from=build /build/target/eyetap-backend.jar /app/eyetap-backend.jar
WORKDIR /app
RUN ls

# TODO: Env vars

CMD ["java", "-jar", "eyetap-backend.jar"]
