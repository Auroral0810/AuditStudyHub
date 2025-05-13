FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/*.jar app.jar
# 合并环境变量设置，避免冲突
ENV SPRING_PROFILES_ACTIVE=native \
    SPRING_CLOUD_CONFIG_SERVER_NATIVE_SEARCH_LOCATIONS=/config \
    NACOS_USERNAME=nacos \
    NACOS_PASSWORD=nacos \
    MANAGEMENT_METRICS_EXPORT_SIMPLE_ENABLED=false \
    MANAGEMENT_METRICS_ENABLE_JVM=false \
    MANAGEMENT_METRICS_ENABLE_PROCESS=false \
    MANAGEMENT_METRICS_ENABLE_SYSTEM=false
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
