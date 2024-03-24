FROM openjdk:8
FROM nginx:latest
#创建响应文件夹
RUN mkdir -p /app/config
RUN mkdir -p /app/media
# 设置环境变量，用于用户指定媒体目录和配置目录
ENV MEDIA_DIR /app/media
ENV CONFIG_DIR /app/config
ADD dandanWeb-rest/target/dandanWeb-rest.jar app.jar
ADD dandan-web/dist/ /usr/share/nginx/html
ADD dandan-web/default.conf /etc/nginx/conf.d/default.conf
ENV LANG=C.UTF-8
ENV LANGUAGE C.UTF-8
ENV LC_ALL=C.UTF-8
EXPOSE 80
#运行程序主体
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]

