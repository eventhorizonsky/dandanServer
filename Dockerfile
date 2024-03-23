FROM  openjdk:8
#VOLUME /tmp
ADD dandanWeb-rest/target/dandanWeb-rest.jar app.jar
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US:en
ENV LC_ALL en_US.UTF-8
EXPOSE 8081
# 将初始化脚本复制到容器中
COPY init_db.sh init_db.sh
# 设置脚本执行权限
RUN chmod +x init_db.sh
CMD ["./init_db.sh"]
#RUN bash -c 'touch /app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]

