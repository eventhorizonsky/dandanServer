FROM  openjdk:8
#VOLUME /tmp
ADD dandanWeb-rest/target/dandanWeb-rest.jar app.jar
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US:en
ENV LC_ALL en_US.UTF-8
EXPOSE 8081
#RUN bash -c 'touch /app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]

