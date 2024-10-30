FROM amazoncorretto:17 AS gateway
WORKDIR /app/gateway
COPY gateway/build/libs/gateway-*.jar gateway.jar
CMD ["java", "-jar", "gateway.jar"]

FROM amazoncorretto:17 AS domain-service
WORKDIR /app/domain-service
COPY domain-service/build/libs/domain-service-*.jar domain-service.jar
CMD ["java", "-jar", "domain-service.jar"]
