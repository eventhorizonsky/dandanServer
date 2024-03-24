FROM  openjdk:8
#VOLUME /tmp
ADD dandanWeb-rest/target/dandanWeb-rest.jar app.jar
ENV LANG=C.UTF-8
ENV LANGUAGE C.UTF-8
ENV LC_ALL=C.UTF-8
EXPOSE 8081
#RUN bash -c 'touch /app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]

