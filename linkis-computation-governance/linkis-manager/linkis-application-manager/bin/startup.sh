#!/bin/bash

export MANAGER_PORT=9101
export SERVER_CLASS=com.webank.wedatasphere.linkis.manager.am.LinkisManagerApplication

cd `dirname $0`
cd ..
HOME=`pwd`

export SERVER_CONF_PATH=$HOME/conf
export SERVER_LIB=$HOME/lib
export SERVER_LOG_PATH=$HOME/logs

if [ ! -w "$SERVER_LOG_PATH" ] ; then
  mkdir -p "$SERVER_LOG_PATH"
fi

if test -z "$SERVER_HEAP_SIZE"
then
  export SERVER_HEAP_SIZE="512M"
fi

if test -z "$SERVER_JAVA_OPTS"
then
  export SERVER_JAVA_OPTS=" -Xmx$SERVER_HEAP_SIZE -XX:+UseG1GC -Xloggc:$SERVER_LOG_PATH/linkis-gc.log"
fi

export SERVER_CLASS_PATH=$SERVER_CONF_PATH:$SERVER_LIB/*

## set spring args
if [ "$MANAGER_PORT" != "" ]; then
  SPRING_ARGS="--server.port=$MANAGER_PORT"
fi

if [ "$EUREKA_URL" != "" ]; then
  SPRING_ARGS="$SPRING_ARGS --eureka.client.serviceUrl.defaultZone=$EUREKA_URL"
fi

java $SERVER_JAVA_OPTS -cp $SERVER_CLASS_PATH $SERVER_CLASS $SPRING_ARGS 2>&1 > $SERVER_LOG_PATH/linkis.out