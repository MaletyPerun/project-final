FROM maven:3.8.4-openjdk-17 as builder
WORKDIR /app
COPY . /app/.
RUN mvn -f /app/pom.xml claen install -Dmaven.test.skip=true

FROM openjdk:17
WORKDIR /app
COPY --from=builder /app/target/*.jar /app/*.jar

ENTRYPOINT ["java", "-jar", "/JiraRush-1.0.jar"]

#FROM openjdk:18
#COPY target/JiraRush-1.0.jar /JiraRush-1.0.jar
#CMD ["java", "-jar", "/JiraRush-1.0.jar"]
