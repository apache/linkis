#!/bin/bash

cd `dirname $0`
cd ..
HOME=`pwd`
export DWS_ENGINE_MANAGER_HOME=$HOME

export DWS_ENGINE_MANAGER_PID=$HOME/bin/linkis.pid

if [[ -f "${DWS_ENGINE_MANAGER_PID}" ]]; then
    pid=$(cat ${DWS_ENGINE_MANAGER_PID})
    if kill -0 ${pid} >/dev/null 2>&1; then
      echo "Hive Engine Manager is already running."
      return 0;
    fi
fi

export DWS_ENGINE_MANAGER_LOG_PATH=$HOME/logs
export DWS_ENGINE_MANAGER_HEAP_SIZE="1G"
export DWS_ENGINE_MANAGER_JAVA_OPTS="-Xms$DWS_ENGINE_MANAGER_HEAP_SIZE -Xmx$DWS_ENGINE_MANAGER_HEAP_SIZE -XX:+UseG1GC -XX:MaxPermSize=500m"

nohup java $DWS_ENGINE_MANAGER_JAVA_OPTS -cp $HOME/conf:$HOME/lib/* com.webank.wedatasphere.linkis.DataWorkCloudApplication 2>&1 > $DWS_ENGINE_MANAGER_LOG_PATH/linkis.out &
pid=$!
if [[ -z "${pid}" ]]; then
    echo "Hive Engine Manager start failed!"
    exit 1
else
    echo "Hive Engine Manager start succeeded!"
    echo $pid > $DWS_ENGINE_MANAGER_PID
    sleep 1
fi