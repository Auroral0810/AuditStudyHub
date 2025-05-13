#!/bin/bash

# 设置颜色输出
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m' # 无颜色

# =============================== LOGO ===============================
echo -e "${CYAN}"
echo -e "     _             _ _ _    ____  _             _         _  _       _     "
echo -e "    / \\  _   _  __| (_) |_ / ___|| |_ _   _  __| |_   _  | || | _   _| |__  "
echo -e "   / _ \\| | | |/ _\` | | __|\\___ \\| __| | | |/ _\` | | | | | || || | | | '_ \\ "
echo -e "  / ___ \\ |_| | (_| | | |_  ___) | |_| |_| | (_| | |_| | |__   | |_| | |_) |"
echo -e " /_/   \\_\\__,_|\\__,_|_|\\__|____/ \\__|\\__,_|\\__,_|\\__, |    |_| \\__,_|_.__/ "
echo -e "                                                  |___/                      "
echo -e "${NC}"
echo -e "${YELLOW}=======================================================================${NC}"
echo -e "${PURPLE}${BOLD}               欢迎使用AuditStudyHub微服务部署工具               ${NC}"
echo -e "${YELLOW}=======================================================================${NC}"
echo -e "\n"

# =============================== 清理容器 ===============================
echo -e "${YELLOW}${BOLD}=== 开始清理现有容器 ===${NC}"
CONTAINERS=$(docker ps -a | grep -E 'mysql|nacos|elasticsearch|kibana|prometheus|grafana|rabbitmq|redis' | awk '{print $1 " (" $2 ")"}')

if [ -n "$CONTAINERS" ]; then
  echo -e "${RED}将删除以下容器:${NC}"
  docker ps -a | grep -E 'mysql|nacos|elasticsearch|kibana|prometheus|grafana|rabbitmq|redis' | awk '{print "  - " $NF " (" $2 ")"}'
  docker ps -a | grep -E 'mysql|nacos|elasticsearch|kibana|prometheus|grafana|rabbitmq|redis' | awk '{print $1}' | xargs -r docker rm -f
  echo -e "${GREEN}✅ 已清理所有相关容器${NC}"
else
  echo -e "${GREEN}✅ 没有需要清理的容器${NC}"
fi

# =============================== 创建目录结构 ===============================
echo -e "\n${YELLOW}${BOLD}=== 创建必要的目录结构 ===${NC}"

DIRECTORIES=(
  "./nacos/logs" "./nacos/conf" "./nacos/data"
  "./elasticsearch/data" "./elasticsearch/config"
  "./mysql/data" "./mysql/init"
  "./redis/data"
  "./rabbitmq/data"
  "./prometheus/data"
  "./grafana/data"
  "./kibana/data"
  "./DEFAULT_GROUP"
  "./jars"
)

for DIR in "${DIRECTORIES[@]}"; do
  if [ ! -d "$DIR" ]; then
    mkdir -p "$DIR"
    echo -e "${GREEN}✅ 创建目录: $DIR${NC}"
  else
    echo -e "${BLUE}ℹ️ 目录已存在: $DIR${NC}"
  fi
done

# 复制配置文件
echo -e "\n${BLUE}复制配置文件...${NC}"
cp elasticsearch.yml ./elasticsearch/config/ && echo -e "${GREEN}✅ 已复制 elasticsearch.yml${NC}"
cp prometheus.yml ./prometheus/ && echo -e "${GREEN}✅ 已复制 prometheus.yml${NC}"
cp application.properties ./nacos/conf/ && echo -e "${GREEN}✅ 已复制 application.properties${NC}"

echo -e "\n${BLUE}准备配置文件...${NC}"
cp application-backend.yml ./DEFAULT_GROUP/ && echo -e "${GREEN}✅ 已复制 application-backend.yml${NC}"
cp application-backend-dev.yml ./DEFAULT_GROUP/ && echo -e "${GREEN}✅ 已复制 application-backend-dev.yml${NC}"
cp .env.dev ./DEFAULT_GROUP/ && echo -e "${GREEN}✅ 已复制 .env.dev${NC}"

echo -e "\n${BLUE}创建 Nacos 配置压缩包...${NC}"
find ./DEFAULT_GROUP -name ".DS_Store" -delete 2>/dev/null
# 确保在上一级目录创建zip，保留DEFAULT_GROUP目录结构
(cd . && zip -r --exclude='*.DS_Store' -q DEFAULT_GROUP.zip DEFAULT_GROUP)
echo -e "${GREEN}✅ 已创建 DEFAULT_GROUP.zip 文件${NC}"

# =============================== 创建网络 ===============================
echo -e "\n${YELLOW}${BOLD}=== 创建Docker网络 ===${NC}"
if ! docker network inspect audit-net >/dev/null 2>&1; then
  docker network create audit-net
  echo -e "${GREEN}✅ 已创建网络: audit-net${NC}"
else
  echo -e "${BLUE}ℹ️ 网络已存在: audit-net${NC}"
fi

# =============================== 启动服务 ===============================
echo -e "\n${YELLOW}${BOLD}===== 开始启动微服务 =====${NC}"

# 1. 启动MySQL服务
echo -e "\n${CYAN}${BOLD}>>> 正在启动 MySQL 服务...${NC}"
echo -e "${BLUE}初始化数据库存储${NC}"

docker run -d \
  --name mysql \
  --network audit-net \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=audit_study_hub \
  -e MYSQL_USER=audit_user \
  -e MYSQL_PASSWORD=audit_password \
  -v $(pwd)/mysql/data:/var/lib/mysql \
  -v $(pwd)/mysql/init:/docker-entrypoint-initdb.d \
  -p 13306:3306 \
  mysql:8.0

if [ $? -eq 0 ]; then
  echo -e "${GREEN}✅ MySQL 服务启动成功!${NC}"
else
  echo -e "${RED}❌ MySQL 服务启动失败!${NC}"
  exit 1
fi

echo -e "${YELLOW}⏳ 等待MySQL启动并初始化数据库 (约30秒)...${NC}"
sleep 30

echo -e "${CYAN}>>> 配置Nacos数据库...${NC}"
docker exec -i mysql mysql -uroot -proot -e "CREATE DATABASE IF NOT EXISTS nacos_config CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;" && echo -e "${GREEN}✅ 已创建Nacos数据库${NC}"

echo -e "${CYAN}>>> 导入Nacos数据库表结构...${NC}"
docker cp nacos-mysql.sql mysql:/tmp/ && echo -e "${GREEN}✅ 已复制SQL脚本到容器${NC}"
docker exec -i mysql sh -c "mysql -uroot -proot nacos_config < /tmp/nacos-mysql.sql" && echo -e "${GREEN}✅ 已导入Nacos表结构${NC}"

echo -e "${CYAN}>>> 验证Nacos数据库...${NC}"
docker exec -i mysql mysql -uroot -proot -e "SHOW DATABASES; SHOW TABLES FROM nacos_config;" && echo -e "${GREEN}✅ Nacos数据库验证成功${NC}"

# 2. 启动Redis
echo -e "\n${CYAN}${BOLD}>>> 正在启动 Redis 服务...${NC}"
echo -e "${BLUE}内存数据缓存${NC}"

docker run -d \
  --name redis \
  --network audit-net \
  -v $(pwd)/redis/data:/data \
  -p 16379:6379 \
  redis:7.0

if [ $? -eq 0 ]; then
  echo -e "${GREEN}✅ Redis 服务启动成功!${NC}"
else
  echo -e "${RED}❌ Redis 服务启动失败!${NC}"
  exit 1
fi

# 3. 启动RabbitMQ
echo -e "\n${CYAN}${BOLD}>>> 正在启动 RabbitMQ 服务...${NC}"
echo -e "${BLUE}消息队列${NC}"

docker run -d \
  --name rabbitmq \
  --network audit-net \
  -e RABBITMQ_DEFAULT_USER=guest \
  -e RABBITMQ_DEFAULT_PASS=guest \
  -v $(pwd)/rabbitmq/data:/var/lib/rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  rabbitmq:3.12-management

if [ $? -eq 0 ]; then
  echo -e "${GREEN}✅ RabbitMQ 服务启动成功!${NC}"
else
  echo -e "${RED}❌ RabbitMQ 服务启动失败!${NC}"
  exit 1
fi

# 4. 启动Nacos
echo -e "\n${CYAN}${BOLD}>>> 正在启动 Nacos 服务...${NC}"
echo -e "${BLUE}服务注册与配置中心${NC}"

docker run -d \
  --name nacos-standalone \
  --network audit-net \
  -e MODE=standalone \
  -e PREFER_HOST_MODE=hostname \
  -e JVM_XMS=512m \
  -e JVM_XMX=512m \
  -e SPRING_DATASOURCE_PLATFORM=mysql \
  -e MYSQL_SERVICE_HOST=mysql \
  -e MYSQL_SERVICE_PORT=3306 \
  -e MYSQL_SERVICE_USER=root \
  -e MYSQL_SERVICE_PASSWORD=root \
  -e MYSQL_SERVICE_DB_NAME=nacos_config \
  -e NACOS_AUTH_ENABLE=true \
  -e MYSQL_DATABASE_NUM=1 \
  -v $(pwd)/nacos/logs:/home/nacos/logs \
  -v $(pwd)/nacos/conf/application.properties:/home/nacos/conf/application.properties \
  -v $(pwd)/nacos/data:/home/nacos/data \
  -p 8848:8848 \
  -p 9848:9848 \
  -p 9849:9849 \
  nacos/nacos-server:v2.2.0

if [ $? -eq 0 ]; then
  echo -e "${GREEN}✅ Nacos 服务启动成功!${NC}"
else
  echo -e "${RED}❌ Nacos 服务启动失败!${NC}"
  exit 1
fi

echo -e "${YELLOW}⏳ 等待Nacos启动...${NC}"
sleep 10
docker logs nacos-standalone | tail -n 20
echo -e "${BLUE}ℹ️ 显示Nacos启动日志片段${NC}"

echo -e "\n${YELLOW}${BOLD}=== 检查Nacos服务状态 ===${NC}"
echo -e "${BLUE}ℹ️ 尝试连接到Nacos控制台...${NC}"

for i in {1..12}; do
  if curl -s http://localhost:8848/nacos/ > /dev/null; then
    echo -e "${GREEN}✅ Nacos已成功启动! (尝试 $i/12)${NC}"
    break
  fi
  if [ $i -eq 12 ]; then
    echo -e "${RED}❌ Nacos启动失败，请检查日志...${NC}"
    docker logs nacos-standalone
    exit 1
  fi
  echo -e "${YELLOW}⏳ 等待Nacos启动 (尝试 $i/12)...${NC}"
  sleep 10
done

# =============================== Nacos配置提示 ===============================
echo -e "\n${YELLOW}${BOLD}=== Nacos配置说明 ===${NC}"
echo -e "${BLUE}Nacos配置文件已准备好，请按以下步骤导入:${NC}"
echo -e "${YELLOW}----------------------------------------------------------------------------------------${NC}"
echo -e " 1. ${CYAN}登录Nacos控制台:${NC} http://localhost:8848/nacos/ (用户名/密码: nacos/nacos)"
echo -e " 2. ${CYAN}导入配置:${NC}"
echo -e "    a) 在Nacos控制台点击 ${BOLD}配置管理->配置列表${NC}"
echo -e "    b) 点击右上角的 ${BOLD}导入配置${NC} 按钮"
echo -e "    c) 在弹出窗口中选择以下文件并上传:"
echo -e "       ${BOLD}$(pwd)/DEFAULT_GROUP.zip${NC}"
echo -e "    d) 上传成功后，刷新页面确认三个配置文件都已导入"
echo -e " 3. ${RED}重要:${NC} 请在导入配置前修改敏感信息:"
echo -e "    a) 编辑 ${BOLD}DEFAULT_GROUP/.env.dev${NC} 文件"
echo -e "    b) 替换以下敏感信息:"
echo -e "       - JWT密钥: ${BOLD}JWT_SECRET_KEY${NC} (使用安全的随机字符串)"
echo -e "       - 阿里云OSS配置: ${BOLD}OSS_ACCESS_KEY_ID, OSS_ACCESS_KEY_SECRET, OSS_BUCKET_NAME, OSS_URL_PREFIX${NC}"
echo -e "       - 邮箱配置: ${BOLD}MAIL_USERNAME, MAIL_PASSWORD${NC} (使用邮箱授权码)"
echo -e "    c) 修改 ${BOLD}DEFAULT_GROUP/application-backend.yml${NC} 中的敏感信息"
echo -e "    d) 重新创建ZIP压缩包后上传"
echo -e "${YELLOW}----------------------------------------------------------------------------------------${NC}"

# 5. 启动Elasticsearch
echo -e "\n${CYAN}${BOLD}>>> 正在启动 Elasticsearch 服务...${NC}"
echo -e "${BLUE}全文搜索引擎${NC}"

docker run -d \
  --name elasticsearch \
  --network audit-net \
  -e node.name=es01 \
  -e cluster.name=audit-es-cluster \
  -e discovery.type=single-node \
  -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
  -e xpack.security.enabled=false \
  -e cluster.routing.allocation.disk.threshold_enabled=false \
  -e cluster.auto_shrink_voting_configuration=true \
  -v $(pwd)/elasticsearch/data:/usr/share/elasticsearch/data \
  -v $(pwd)/elasticsearch/config/elasticsearch.yml:/usr/share/elasticsearch/config/elasticsearch.yml \
  -p 9201:9200 \
  -p 9301:9300 \
  elasticsearch:8.10.4

if [ $? -eq 0 ]; then
  echo -e "${GREEN}✅ Elasticsearch 服务启动成功!${NC}"
else
  echo -e "${RED}❌ Elasticsearch 服务启动失败!${NC}"
  exit 1
fi

echo -e "\n${CYAN}${BOLD}>>> 等待 Elasticsearch 完全启动...${NC}"
echo -e "${BLUE}⏳ 等待Elasticsearch启动 (约45秒)...${NC}"
sleep 45

echo -e "${BLUE}检查Elasticsearch服务状态...${NC}"
for i in {1..5}; do
  if curl -s "http://localhost:9201/_cluster/health" | grep -q '"status":"yellow"'; then
    echo -e "${GREEN}✅ Elasticsearch已完全启动并正常运行!${NC}"
    break
  elif [ $i -eq 5 ]; then
    echo -e "${YELLOW}⚠️ Elasticsearch可能未完全启动，但将继续安装IK分词插件...${NC}"
  else
    echo -e "${YELLOW}⏳ 继续等待Elasticsearch启动 (尝试 $i/5)...${NC}"
    sleep 15
  fi
done

echo -e "${BLUE}⏳ 正在安装IK分词插件...${NC}"
docker exec -it elasticsearch /bin/bash -c "echo 'y' | bin/elasticsearch-plugin install https://get.infini.cloud/elasticsearch/analysis-ik/8.10.4"

echo -e "${YELLOW}⏳ 重启Elasticsearch应用插件...${NC}"
docker restart elasticsearch
echo -e "${GREEN}✅ IK分词插件安装成功并已重启Elasticsearch!${NC}"

echo -e "${BLUE}⏳ 等待Elasticsearch重启完成 (约30秒)...${NC}"
sleep 30

echo -e "${BLUE}检查Elasticsearch重启后状态...${NC}"
for i in {1..5}; do
  if curl -s "http://localhost:9201/_cluster/health" | grep -q '"status":"yellow"'; then
    echo -e "${GREEN}✅ Elasticsearch重启成功，服务状态正常!${NC}"
    break
  elif [ $i -eq 5 ]; then
    echo -e "${YELLOW}⚠️ Elasticsearch状态检查未通过，但将继续部署其他服务...${NC}"
  else
    echo -e "${YELLOW}⏳ 继续等待Elasticsearch启动 (尝试 $i/5)...${NC}"
    sleep 10
  fi
done

# 6. 启动Kibana
echo -e "\n${CYAN}${BOLD}>>> 正在启动 Kibana 服务...${NC}"
echo -e "${BLUE}ES数据可视化${NC}"

docker run -d \
  --name kibana \
  --network audit-net \
  -e ELASTICSEARCH_HOSTS=http://elasticsearch:9200 \
  -e ELASTICSEARCH_REQUESTTIMEOUT=90000 \
  -e STATUS_ALLOWANYYELLOW=true \
  -e KBN_OPTIMIZATION_USEILYALOADER=false \
  -v $(pwd)/kibana/data:/usr/share/kibana/data \
  -p 5601:5601 \
  kibana:8.10.4

if [ $? -eq 0 ]; then
  echo -e "${GREEN}✅ Kibana 服务启动成功!${NC}"
else
  echo -e "${RED}❌ Kibana 服务启动失败!${NC}"
  exit 1
fi

# 7. 启动Prometheus
echo -e "\n${CYAN}${BOLD}>>> 正在启动 Prometheus 服务...${NC}"
echo -e "${BLUE}监控系统${NC}"

docker run -d \
  --name prometheus \
  --network audit-net \
  -v $(pwd)/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml \
  -v $(pwd)/prometheus/data:/prometheus \
  -p 9090:9090 \
  prom/prometheus:latest

if [ $? -eq 0 ]; then
  echo -e "${GREEN}✅ Prometheus 服务启动成功!${NC}"
else
  echo -e "${RED}❌ Prometheus 服务启动失败!${NC}"
  exit 1
fi

# 8. 启动Grafana
echo -e "\n${CYAN}${BOLD}>>> 正在启动 Grafana 服务...${NC}"
echo -e "${BLUE}监控仪表盘${NC}"

docker run -d \
  --name grafana \
  --network audit-net \
  -e GF_SECURITY_ADMIN_USER=admin \
  -e GF_SECURITY_ADMIN_PASSWORD=admin \
  -v $(pwd)/grafana/data:/var/lib/grafana \
  -p 13000:3000 \
  grafana/grafana:latest

if [ $? -eq 0 ]; then
  echo -e "${GREEN}✅ Grafana 服务启动成功!${NC}"
else
  echo -e "${RED}❌ Grafana 服务启动失败!${NC}"
  exit 1
fi

# =============================== 编译Spring Boot服务 ===============================
echo -e "\n${YELLOW}${BOLD}===== 编译Spring Boot微服务 =====${NC}"

DEPLOYMENT_DIR=$(pwd)
echo -e "${BLUE}部署目录: ${DEPLOYMENT_DIR}${NC}"

PROJECT_ROOT=$(cd .. && pwd)
CLOUD_DIR="${PROJECT_ROOT}/audit-study-hub-cloud"

echo -e "${BLUE}项目根目录: ${PROJECT_ROOT}${NC}"
echo -e "${BLUE}Cloud模块目录: ${CLOUD_DIR}${NC}"

# 编译注册中心
echo -e "\n${CYAN}${BOLD}>>> 编译服务注册中心...${NC}"
if [ -d "${CLOUD_DIR}/audit-study-hub-registry" ]; then
  cd "${CLOUD_DIR}/audit-study-hub-registry"
  echo -e "${BLUE}⏳ 执行: mvn clean package -DskipTests${NC}"
  mvn clean package -DskipTests
  
  if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ 服务注册中心编译成功${NC}"
    # 复制JAR包
    cp target/audit-study-hub-registry-*.jar "${DEPLOYMENT_DIR}/jars/"
    echo -e "${GREEN}✅ 已复制服务注册中心JAR包到 ${DEPLOYMENT_DIR}/jars/${NC}"
  else
    echo -e "${RED}❌ 服务注册中心编译失败${NC}"
  fi
else
  echo -e "${RED}❌ 找不到服务注册中心目录${NC}"
fi

# 编译配置中心
echo -e "\n${CYAN}${BOLD}>>> 编译配置中心...${NC}"
if [ -d "${CLOUD_DIR}/audit-study-hub-config" ]; then
  cd "${CLOUD_DIR}/audit-study-hub-config"
  echo -e "${BLUE}⏳ 执行: mvn clean package -DskipTests${NC}"
  mvn clean package -DskipTests
  
  if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ 配置中心编译成功${NC}"
    # 复制JAR包
    cp target/audit-study-hub-config-*.jar "${DEPLOYMENT_DIR}/jars/"
    echo -e "${GREEN}✅ 已复制配置中心JAR包到 ${DEPLOYMENT_DIR}/jars/${NC}"
  else
    echo -e "${RED}❌ 配置中心编译失败${NC}"
  fi
else
  echo -e "${RED}❌ 找不到配置中心目录${NC}"
fi

# 编译后端服务
echo -e "\n${CYAN}${BOLD}>>> 编译后端服务...${NC}"
if [ -d "${CLOUD_DIR}/audit-study-hub-backend" ]; then
  cd "${CLOUD_DIR}/audit-study-hub-backend"
  echo -e "${BLUE}⏳ 执行: mvn clean package -DskipTests${NC}"
  mvn clean package -DskipTests
  
  if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ 后端服务编译成功${NC}"
    # 复制JAR包
    cp target/audit-study-hub-backend-*.jar "${DEPLOYMENT_DIR}/jars/"
    echo -e "${GREEN}✅ 已复制后端服务JAR包到 ${DEPLOYMENT_DIR}/jars/${NC}"
  else
    echo -e "${RED}❌ 后端服务编译失败${NC}"
  fi
else
  echo -e "${RED}❌ 找不到后端服务目录${NC}"
fi

# 返回部署目录
cd "${DEPLOYMENT_DIR}"

# =============================== 总结信息 ===============================
echo -e "\n${GREEN}${BOLD}===== 所有服务已启动并编译完成! =====${NC}"
echo -e "${YELLOW}${BOLD}微服务基础设施访问地址:${NC}"

SERVICE_INFO=(
  "Nacos服务注册中心: ${CYAN}http://localhost:8848/nacos${NC}  [用户名/密码: nacos/nacos]"
  "Elasticsearch: ${CYAN}http://localhost:9201${NC}             [全文搜索引擎]"
  "Kibana: ${CYAN}http://localhost:5601${NC}                    [ES数据可视化]"
  "Prometheus: ${CYAN}http://localhost:9090${NC}                [监控数据]"
  "Grafana: ${CYAN}http://localhost:13000${NC}                  [监控仪表盘] [用户名/密码: admin/admin]"
  "RabbitMQ管理界面: ${CYAN}http://localhost:15672${NC}         [消息队列] [用户名/密码: guest/guest]"
  "MySQL数据库: ${CYAN}localhost:13306${NC}                     [用户名/密码: root/root]"
  "Redis服务: ${CYAN}localhost:16379${NC}                       [缓存服务]"
)

echo -e "\n${PURPLE}${BOLD}服务访问信息:${NC}"
echo -e "${YELLOW}----------------------------------------------------------------------------------------${NC}"
for info in "${SERVICE_INFO[@]}"; do
  echo -e " 🚀 $info"
done
echo -e "${YELLOW}----------------------------------------------------------------------------------------${NC}"

echo -e "\n${PURPLE}${BOLD}当前运行的容器:${NC}"
echo -e "${YELLOW}----------------------------------------------------------------------------------------${NC}"
docker ps --format "table | {{.Names}}\t| {{.Image}}\t| {{.Status}}\t| {{.Ports}}"
echo -e "${YELLOW}----------------------------------------------------------------------------------------${NC}"

echo -e "\n${PURPLE}${BOLD}编译好的服务JAR包:${NC}"
echo -e "${YELLOW}----------------------------------------------------------------------------------------${NC}"
ls -la ./jars/
echo -e "${YELLOW}----------------------------------------------------------------------------------------${NC}"

echo -e "\n${GREEN}${BOLD}✨ AuditStudyHub基础设施已成功部署! ✨${NC}"
echo -e "${YELLOW}=======================================================================${NC}"

echo -e "\n${PURPLE}${BOLD}服务启动指南:${NC}"
echo -e "${YELLOW}----------------------------------------------------------------------------------------${NC}"
echo -e " 请先确保已在Nacos中创建了所需的配置文件，然后进入jars目录并按以下顺序启动服务:"
echo -e ""
echo -e " ${BLUE}cd ./jars${NC}"
echo -e ""
echo -e " 1. ${CYAN}服务注册中心:${NC}"
echo -e "    java -jar audit-study-hub-registry-0.0.1-SNAPSHOT.jar"
echo -e ""
echo -e " 2. ${CYAN}配置中心:${NC}"
echo -e "    java -jar audit-study-hub-config-0.0.1-SNAPSHOT.jar"
echo -e ""
echo -e " 3. ${CYAN}后端服务:${NC}"
echo -e "    java -jar audit-study-hub-backend-0.0.1-SNAPSHOT.jar"
echo -e ""
echo -e " 说明: 每启动一个服务后，请等待其完全启动后再启动下一个服务。"
echo -e " 启动后端服务前，请确保前两个服务已成功注册到Nacos并正常运行。"
echo -e "${YELLOW}----------------------------------------------------------------------------------------${NC}"

echo -e "\n${GREEN}${BOLD}部署完成! 祝您使用愉快!${NC}"
echo -e "${YELLOW}=======================================================================${NC}"

echo -e "\n${PURPLE}${BOLD}前端应用启动指南:${NC}"
echo -e "${YELLOW}----------------------------------------------------------------------------------------${NC}"
echo -e " 后端服务启动后，您还需要启动前端应用:"
echo -e ""
echo -e " 1. ${CYAN}管理端 (Admin):${NC}"
echo -e "    ${BLUE}cd ../audit-study-hub-admin${NC}"
echo -e "    ${BLUE}# 重要: 确保使用 Node.js 21 版本${NC}"
echo -e "    ${BLUE}unset NODE_OPTIONS${NC}                     ${YELLOW}# 清除可能影响启动的环境变量${NC}"
echo -e "    ${BLUE}nvm install 21${NC}                         ${YELLOW}# 安装 Node.js 21 (如已安装可跳过)${NC}"
echo -e "    ${BLUE}nvm use 21${NC}                             ${YELLOW}# 切换到 Node.js 21${NC}"
echo -e "    ${BLUE}pnpm install${NC}"
echo -e "    ${BLUE}pnpm dev:ele${NC}"
echo -e "    访问地址: ${CYAN}http://localhost:5777${NC}"
echo -e ""
echo -e " 2. ${CYAN}用户端 (Frontend):${NC}"
echo -e "    ${BLUE}cd ../audit-study-hub-frontend${NC}"
echo -e "    ${BLUE}npm install${NC}"
echo -e "    ${BLUE}npm run dev${NC}"
echo -e "    访问地址: ${CYAN}http://localhost:3000${NC}"
echo -e "${YELLOW}----------------------------------------------------------------------------------------${NC}" 