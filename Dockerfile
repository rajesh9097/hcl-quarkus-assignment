FROM eclipse-temurin:8-jre
EXPOSE 8080
ADD target/java-code-assignment-1.0.0-SNAPSHOT.jar java-code-assignment-1.0.0-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/java-code-assignment-1.0.0-SNAPSHOT.jar.jar"]