#!/bin/bash
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
  echo "Usage: linkis-daemon [start | stop | restart | status] [serverName]"
  echo " serverName            The service name of the operation"
  echo "Most commands print help when invoked w/o parameters."
}

if [ $# != 2 ]; then
  print_usage
  exit 2
fi

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
  if [[ ! -f "${SERVER_START_BIN}" ]]; then
      echo "The $SERVER_NAME is wrong or the corresponding startup script does not exist: "
      echo "$SERVER_START_BIN"
      exit 1
  else
      echo "Start server, startup script:  $SERVER_START_BIN"
      export SERVER_NAME=linkis-$SERVER_NAME
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
