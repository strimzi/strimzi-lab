FROM openjdk:8-jre-alpine

ADD target/consumer-app.jar /

CMD ["java", "-Dvertx.cacheDirBase=/tmp", "-Dvertx.disableDnsResolver=true", "-jar", "consumer-app.jar"]