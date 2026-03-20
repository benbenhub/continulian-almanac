FROM maven:3.9.9-eclipse-temurin-8 AS build
WORKDIR /workspace

COPY pom.xml pom.xml
COPY tyme4j/pom.xml tyme4j/pom.xml
COPY almanac/pom.xml almanac/pom.xml
COPY tyme4j/src tyme4j/src
COPY almanac/src almanac/src

RUN mvn -B -pl almanac -am clean package -DskipTests

FROM eclipse-temurin:8-jre
WORKDIR /app
ENV TZ=Asia/Shanghai

COPY --from=build /workspace/almanac/target/almanac-*.jar /app/almanac.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/almanac.jar"]
