FROM openjdk:8-jre-alpine

WORKDIR /app

# 将应用程序jar文件复制到容器中
COPY dandanWeb-rest/target/dandanWeb-rest.jar

# 将初始化脚本复制到容器中
COPY init_db.sh init_db.sh

# 设置脚本执行权限
RUN chmod +x init_db.sh

# 设置启动命令
CMD ["./init_db.sh && java -jar my-application.jar"]

# 指定Volume
VOLUME /app/data
