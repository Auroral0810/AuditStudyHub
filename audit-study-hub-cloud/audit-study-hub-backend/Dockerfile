FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/*.jar app.jar
ENV SPRING_PROFILES_ACTIVE=prod
# 禁用监控指标
ENV MANAGEMENT_METRICS_EXPORT_SIMPLE_ENABLED=false
ENV MANAGEMENT_METRICS_ENABLE_JVM=false
ENV MANAGEMENT_METRICS_ENABLE_PROCESS=false
ENV MANAGEMENT_METRICS_ENABLE_SYSTEM=false
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
