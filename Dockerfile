#FROM maven:3.8.4-openjdk-17 as builder
#WORKDIR /app
#COPY . /app/.
#это не совсем правильно/хорошо
#COPY resources ./app/resources/
#RUN mvn -f /app/pom.xml claen install -Dmaven.test.skip=true
#RUN #mvn claen package -DskipTests

#FROM openjdk:17
#WORKDIR /app
#COPY --from=builder /app/target/*.jar /app/*.jar
#
#ENTRYPOINT ["java", "-jar", "/JiraRush-1.0.jar"]

#FROM openjdk:18
#COPY target/JiraRush-1.0.jar /JiraRush-1.0.jar
#CMD ["java", "-jar", "/JiraRush-1.0.jar"]


FROM openjdk:17-buster
ARG JAR_FILE=target/*.jar
COPY target/resources ./resources
COPY ${JAR_FILE} jira-1.0.jar
ENTRYPOINT ["java","-jar","/jira-1.0.jar", "--spring.profiles.active=prod"]