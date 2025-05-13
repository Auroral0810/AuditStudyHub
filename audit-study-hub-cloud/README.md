# 审计学习中心微服务架构

本项目是使用Spring Cloud Alibaba构建的微服务架构，提供审计学习相关的功能。

## 架构设计

本项目采用以下技术栈：

- **Spring Boot/Spring Cloud**: 微服务框架
- **Nacos**: 服务注册与发现、配置中心
- **Spring Boot Admin**: 应用监控与管理
- **Ribbon**: 负载均衡
- **Prometheus + Grafana**: 性能监控
- **Docker/Kubernetes**: 容器化与编排

### 模块说明

- **audit-study-hub-registry**: 服务注册中心，集成Spring Boot Admin
- **audit-study-hub-config**: 配置中心
- **audit-study-hub-backend**: 后端业务服务

## 项目部署

### Docker部署

使用docker-compose一键部署所有服务:

```bash
cd audit-study-hub-cloud
docker-compose up -d
```

服务访问地址:

- **Nacos控制台**: http://localhost:8848/nacos/
- **Spring Boot Admin**: http://localhost:8088
- **后端API服务**: http://localhost:8080
- **Grafana监控**: http://localhost:3000

### Kubernetes部署

项目支持Kubernetes部署，配置文件位于`k8s`目录下:

```bash
# 创建命名空间和部署应用
kubectl apply -k audit-study-hub-cloud/k8s/
```

## 监控系统

本项目集成了完整的监控系统:

1. **应用监控**: Spring Boot Admin提供应用状态监控
2. **性能监控**: Prometheus收集性能指标
3. **可视化**: Grafana展示监控数据

## 开发指南

### 本地开发环境搭建

1. 启动本地服务
```bash
# 启动Nacos
docker-compose up -d nacos

# 启动监控服务
docker-compose up -d prometheus grafana
```

2. 启动后端服务
```bash
cd audit-study-hub-backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```
