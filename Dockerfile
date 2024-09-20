FROM amazoncorretto:17

WORKDIR /app

COPY /build/libs/book-exchange-service-*.jar app.jar

CMD ["java", "-jar", "app.jar"]
