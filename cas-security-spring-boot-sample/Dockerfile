FROM maven:3.5-jdk-8-alpine
WORKDIR /src
COPY . /src
RUN mvn clean install

FROM openjdk:8-jre-alpine
WORKDIR /app
COPY --from=0 /src/cas-security-spring-boot-sample/target/cas-security-spring-boot-sample-0.5.0.jar /app
ENV JAVA_OPTS=""
CMD [ "sh", "-c", "java $JAVA_OPTS -jar /app/cas-security-spring-boot-sample-0.5.0.jar" ]
