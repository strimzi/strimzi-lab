FROM openjdk:8-jre-alpine

ADD target/device-app.jar /

CMD ["java", "-Dvertx.cacheDirBase=/tmp", "-jar", "device-app.jar"]