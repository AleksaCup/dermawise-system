FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY model/pom.xml model/pom.xml
COPY kjar/pom.xml kjar/pom.xml
COPY service/pom.xml service/pom.xml
COPY model/src model/src
COPY kjar/src kjar/src
COPY service/src service/src
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -q -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/service/target/service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
