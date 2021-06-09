FROM adoptopenjdk/openjdk11:alpine-jre
MAINTAINER Gerasimov Alexey
COPY build/libs/TestTask.jar TestTask.jar
ENTRYPOINT ["java","-jar","/TestTask.jar"]