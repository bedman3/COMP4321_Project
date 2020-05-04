FROM maven:3.6.3-jdk-8
EXPOSE 8080
COPY . /backend
WORKDIR /backend
RUN mvn package
ENTRYPOINT ["java","-jar","/backend/target/searchEngine-0.0.1-SNAPSHOT.jar"]
