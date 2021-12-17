#!/bin/bash

export SERVER_NAME=linkis-ps-cs

export LINKIS_COMMONS_LIB=/opt/linkis/public-module
export SERVER_CONF_PATH=/opt/linkis/linkis-ps-cs/conf
export SERVER_LIB=/opt/linkis/linkis-ps-cs/lib
export LINKIS_LOG_DIR=/opt/linkis/linkis-ps-cs/logs

if [ ! -w "$LINKIS_LOG_DIR" ] ; then
  mkdir -p "$LINKIS_LOG_DIR"
fi

export SERVER_JAVA_OPTS=" -DserviceName=$SERVER_NAME -Xmx512M -XX:+UseG1GC -Xloggc:$LINKIS_LOG_DIR/${SERVER_NAME}-gc.log $DEBUG_CMD"

export SERVER_CLASS=org.apache.linkis.cs.server.LinkisCSApplication

export SERVER_CLASS_PATH=$SERVER_CONF_PATH:$LINKIS_COMMONS_LIB/*:$SERVER_LIB/*

exec java $SERVER_JAVA_OPTS -cp $SERVER_CLASS_PATH $SERVER_CLASS