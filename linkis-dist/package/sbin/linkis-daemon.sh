#!/usr/bin/env bash
#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
# http://www.apache.org/licenses/LICENSE-2.0
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# description:  Starts and stops Server
#
# @name:        linkis-demo
#
# Modified for Linkis 1.0.0


cd `dirname $0`
cd ..
INSTALL_HOME=`pwd`


function print_usage(){
  echo "Usage: linkis-daemon [start | stop | restart | status] [serverName] [debug-port] [jvm-num] "
  echo " serverName            The service name of the operation"
  echo "debug-port  Specify the service to open the debug port. Not required"
  echo "jvm-num  Specifies the JVM service memory. Not necessary"
  echo "Most commands print help when invoked w/o parameters."
}

if [ $# -gt 4 ]; then
  print_usage
  exit 2
fi

#set init debug_port and  jvm-num
export debug_port=""
export jvm_num=""

if [ $# -eq 4 ]; then
     if [[ ${3} == *"debug"* ]]; then
            export debug_port=${3#*-}
            export jvm_num=${4#*-}
     else
            export jvm_num=${3#*-}
            export debug_port=${4#*-}
     fi
fi

if [ $# -eq 3 ]; then
     if [[ ${3} == *"debug"* ]]; then
            export debug_port=${3#*-}
     else
            export jvm_num=${3#*-}
     fi
fi

#set debug-port
function setport()
{
  if [ "$debug_port" !=  "" ]; then
    pid=`lsof -i :$debug_port | grep -v "PID"`
    if [ "$pid" != "" ];then
      echo "$debug_port already used"
      echo "The port is already in use, please check before installing"
      exit 1
    fi
    sed -i "s/#export DEBUG_PORT=/export DEBUG_PORT=${debug_port}/g" $SERVER_START_BIN
    echo "$debug_port"
  fi
}

#set jvm
function setjvm()
{
  if [ "$jvm_num" !=  "" ]; then
    sed -i "s/#export DYNAMIC_JVM=/export DYNAMIC_JVM=${jvm_num}/g" $SERVER_START_COMMON
  fi
}

# set LINKIS_HOME
if [ "$LINKIS_HOME" = "" ]; then
  export LINKIS_HOME=$INSTALL_HOME
fi

# set LINKIS_CONF_DIR
if [ "$LINKIS_CONF_DIR" = "" ]; then
  export LINKIS_CONF_DIR=$LINKIS_HOME/conf
fi


# get pid directory
if [ "$LINKIS_PID_DIR" = "" ]; then
  export LINKIS_PID_DIR="$LINKIS_HOME/pid"
fi
if [ ! -w "$LINKIS_PID_DIR" ] ; then
  mkdir -p "$LINKIS_PID_DIR"
fi

function start()
{
  echo "Start to check whether the $SERVER_NAME is running"
  if [[ -f "${SERVER_PID}" ]]; then
      pid=$(cat ${SERVER_PID})
      if kill -0 ${pid} >/dev/null 2>&1; then
        echo "$SERVER_NAME is already running."
        exit 1
      fi
  fi
  export SERVER_START_BIN=$LINKIS_HOME/sbin/ext/linkis-$SERVER_NAME
  export SERVER_START_COMMON=$LINKIS_HOME/sbin/ext/linkis-common-start
  if [[ ! -f "${SERVER_START_BIN}" ]]; then
      echo "The $SERVER_NAME is wrong or the corresponding startup script does not exist: "
      echo "$SERVER_START_BIN"
      exit 1
  else
      echo "Start server, startup script:  $SERVER_START_BIN"
      export SERVER_NAME=linkis-$SERVER_NAME
      sed -i "/.*export DYNAMIC_JVM*/c\#export DYNAMIC_JVM=" $SERVER_START_COMMON
      sed -i "/.*export DEBUG_PORT*/c\#export DEBUG_PORT=" $SERVER_START_BIN
      setjvm
      setport
      sh  $SERVER_START_BIN
  fi
}

function wait_for_server_to_die() {
  local pid
  local count
  pid=$1
  timeout=$2
  count=0
  timeoutTime=$(date "+%s")
  let "timeoutTime+=$timeout"
  currentTime=$(date "+%s")
  forceKill=1

  while [[ $currentTime -lt $timeoutTime ]]; do
    $(kill ${pid} > /dev/null 2> /dev/null)
    if kill -0 ${pid} > /dev/null 2>&1; then
      sleep 3
    else
      forceKill=0
      break
    fi
    currentTime=$(date "+%s")
  done

  if [[ forceKill -ne 0 ]]; then
    $(kill -9 ${pid} > /dev/null 2> /dev/null)
  fi
}


function stop()
{
  if [[ ! -f "${SERVER_PID}" ]]; then
      echo "server $SERVER_NAME is not running"
  else
      pid=$(cat ${SERVER_PID})
      if [[ -z "${pid}" ]]; then
        echo "server $SERVER_NAME is not running"
      else
        wait_for_server_to_die $pid 40
        $(rm -f ${SERVER_PID})
        echo "server $SERVER_NAME is stopped."
      fi
  fi
}

function restart()
{
    stop
    sleep 2
    start
}

status()
{
  if [[ ! -f "${SERVER_PID}" ]]; then
      echo "server $SERVER_NAME is stopped"
      exit 1
  else
      pid=$(cat ${SERVER_PID})
      if [[ -z "${pid}" ]]; then
        echo "server $SERVER_NAME is not running"
        exit 1
      fi
      ps -ax | awk '{ print $1 }' | grep -e "^${pid}$"
      flag=$?
      if [ $flag != 0 ]; then
        echo "server $SERVER_NAME is not running"
        exit 1
      fi
      echo "server $SERVER_NAME is running."
  fi

}

COMMAND=$1
export SERVER_NAME=$2
case $SERVER_NAME in
  "cg-engineconnmanager"|"linkis-cg-engineconnmanager"|"engineconnmanager")
    export SERVER_NAME="cg-engineconnmanager"
    ;;
  "cg-entrance"|"linkis-cg-entrance"|"entrance")
    export SERVER_NAME="cg-entrance"
    ;;
  "cg-linkismanager"|"linkis-cg-linkismanager"|"linkismanager")
    export SERVER_NAME="cg-linkismanager"
    ;;
  "mg-eureka"|"linkis-mg-eureka"|"eureka")
    export SERVER_NAME="mg-eureka"
    ;;
  "mg-gateway"|"linkis-mg-gateway"|"gateway")
    export SERVER_NAME="mg-gateway"
    ;;
  "ps-publicservice"|"linkis-ps-publicservice"|"publicservice")
    export SERVER_NAME="ps-publicservice"
    ;;
  *)
    ;;
esac

export SERVER_PID=$LINKIS_PID_DIR/linkis_$SERVER_NAME.pid
case $COMMAND in
  start|stop|restart|status)
    $COMMAND $SERVER_NAME
    ;;
  *)
    print_usage
    exit 2
    ;;
esac
