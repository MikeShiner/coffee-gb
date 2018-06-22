FROM openjdk:8-jdk-alpine

MAINTAINER Mike Shiner <mike.shiner@hotmail.com>

LABEL description="GBC"

# The config volume allows an application.properties or ROMS file to be passed in
VOLUME /config

RUN mkdir -p /gbc
# Creates a symlink directory appearing at /gbc/config
RUN ln -s /config /gbc

# Prepare the service itself
COPY *.jar /gbc/gbc.jar
WORKDIR /gbc
CMD ["java","-jar","gbc.jar", "--headless", "config/rom.gbc"]
