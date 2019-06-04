FROM openjdk:8-alpine

COPY target/uberjar/pon-web.jar /pon-web/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/pon-web/app.jar"]
