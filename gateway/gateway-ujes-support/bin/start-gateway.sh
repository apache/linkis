#!/bin/bash

cd `dirname $0`
cd ..
HOME=`pwd`

export EUREKA_SERVER_PID=$HOME/bin/linkis.pid

if [[ -f "${EUREKA_SERVER_PID}" ]]; then
    pid=$(cat ${EUREKA_SERVER_PID})
    if kill -0 ${pid} >/dev/null 2>&1; then
      echo "Gateway Server is already running."
      return 0;
    fi
fi

export EUREKA_SERVER_LOG_PATH=$HOME/logs
export EUREKA_SERVER_HEAP_SIZE="2G"
export EUREKA_SERVER_CLASS=${EUREKA_SERVER_CLASS:-com.webank.wedatasphere.linkis.DataWorkCloudApplication}


if [ $1 ];then
        type=$1
fi

export EUREKA_SERVER_JAVA_OPTS="-Xms$EUREKA_SERVER_HEAP_SIZE -Xmx$EUREKA_SERVER_HEAP_SIZE -XX:+UseG1GC -XX:MaxPermSize=500m -Xloggc:$HOME/logs/gateway-gc.log"

java $EUREKA_SERVER_JAVA_OPTS -cp $HOME/conf:$HOME/lib/* $EUREKA_SERVER_CLASS  2>&1 > $EUREKA_SERVER_LOG_PATH/linkis.out &
pid=$!
sleep 2
if [[ -z "${pid}" ]]; then
    echo "Gateway SERVER start failed!"
    exit 1
else
    echo "Gateway SERVER start succeed!"
    echo $pid > $EUREKA_SERVER_PID
fi
