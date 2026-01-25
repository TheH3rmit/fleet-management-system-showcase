# ---------- build stage ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# copy build
COPY pom.xml .
COPY src ./src

# build jar
RUN mvn -q -Dmaven.test.skip=true package

# ---------- runtime stage ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# copy jar
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
