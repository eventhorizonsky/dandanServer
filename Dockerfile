FROM  openjdk:8
RUN mkdir -p /config
RUN mkdir -p /media
# 设置环境变量，用于用户指定媒体目录和配置目录
ENV MEDIA_DIR /media
ENV CONFIG_DIR /config
ADD dandanWeb-rest/target/dandanWeb-rest.jar app.jar
ENV LANG=C.UTF-8
ENV LANGUAGE C.UTF-8
ENV LC_ALL=C.UTF-8
EXPOSE 8081
#RUN bash -c 'touch /app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]

