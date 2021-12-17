#!/bin/bash

export SERVER_NAME=linkis-mg-eureka

export SERVER_CONF_PATH=/opt/linkis/linkis-mg-eureka/conf
export SERVER_LIB=/opt/linkis/linkis-mg-eureka/lib
export LINKIS_LOG_DIR=/opt/linkis/linkis-mg-eureka/logs

if [ ! -w "$LINKIS_LOG_DIR" ] ; then
  mkdir -p "$LINKIS_LOG_DIR"
fi

export SERVER_JAVA_OPTS=" -DserviceName=$SERVER_NAME -Xmx512M -XX:+UseG1GC -Xloggc:$LINKIS_LOG_DIR/${SERVER_NAME}-gc.log $DEBUG_CMD "

export SERVER_CLASS=org.apache.linkis.eureka.SpringCloudEurekaApplication

export SERVER_CLASS_PATH=$SERVER_CONF_PATH:$SERVER_LIB/*

SPRING_ARGS="$SPRING_ARGS --spring.profiles.active=eureka"

exec java $SERVER_JAVA_OPTS -cp $SERVER_CLASS_PATH $SERVER_CLASS $SPRING_ARGS