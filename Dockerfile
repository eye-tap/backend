# Build application
FROM alpine AS build

RUN apk add openjdk21-jdk maven
RUN mvn package


# Production image
FROM alpine AS prod
LABEL maintainer="eyetap"
LABEL version="2.0.0"

RUN apk add openjdk21-jre-headless

COPY --from=build /target/eyetap-backend.jar /eyetap-backend.jar

CMD ["java", "/eyetap-backend.jar"]
