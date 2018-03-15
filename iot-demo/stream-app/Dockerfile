FROM openjdk:8-jre-alpine

ADD target/stream-app.jar /

CMD ["java", "-Dvertx.cacheDirBase=/tmp", "-jar", "stream-app.jar"]