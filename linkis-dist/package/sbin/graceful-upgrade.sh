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

function print_usage(){
  echo "Usage: graceful-upgrade [serverName] [ip]"
  echo " serverName            The service name [ps-publicservice,cg-linkismanager,ps-cs,cg-engineconnmanager,cg-entrance,cg-engineplugin,ps-data-source-manager,ps-metadatamanager] of the operation"
  echo " ip                    The service instance's ip"
  echo "Most commands print help when invoked w/o parameters."
}

function startApp(){
  echo "<-------------------------------->"
  echo "Begin to start $SERVER_NAME"
  SERVER_START_CMD="sh $LINKIS_HOME/sbin/linkis-daemon.sh restart $SERVER_NAME"
  if test -z "$SERVER_IP"
  then
    SERVER_IP=$local_host
  fi

  executeCMD $SERVER_IP "$SERVER_START_CMD"

  isSuccess "End to start $SERVER_NAME"
  echo "<-------------------------------->"
  sleep 3
}

function getPort(){
  case $SERVER_NAME in
    "ps-publicservice")
      export SERVER_PORT=$PUBLICSERVICE_PORT
      ;;
    "cg-linkismanager")
      export SERVER_PORT=$MANAGER_PORT
      ;;
    "cg-engineconnmanager")
      export SERVER_PORT=$ENGINECONNMANAGER_PORT
      ;;
    "cg-entrance")
      export SERVER_PORT=$ENTRANCE_PORT
      ;;
    "cg-engineplugin")
      export SERVER_PORT=$ENGINECONN_PLUGIN_SERVER_PORT
      ;;
  esac
}

if [ $# != 2 ]; then
  print_usage
  exit 2
fi

source /etc/profile
source ~/.bash_profile

cd `dirname $0`
cd ..
INSTALL_HOME=`pwd`

# set LINKIS_HOME
if [ "$LINKIS_HOME" = "" ]; then
  export LINKIS_HOME=$INSTALL_HOME
fi
echo $LINKIS_HOME

if [ "$LINKIS_CONF_DIR" = "" ]; then
  export LINKIS_CONF_DIR=$LINKIS_HOME/conf
fi
source $LINKIS_CONF_DIR/linkis-env.sh

export SERVER_NAME=$1
startApp

IP=$2
getPort
echo $SERVER_PORT

if [ "$SERVER_NAME" = "cg-entrance" ]; then
  # mark entrance as offline
  markOfflineUrl=$IP:$SERVER_PORT/api/rest_j/v1/entrance/operation/label/markoffline
  curl "$markOfflineUrl"

  # waiting the running tasks to finish
  while :
  do
    metricUrl=$IP:$SERVER_PORT/api/rest_j/v1/entrance/operation/metrics/runningtask
    res=`curl $metricUrl`
    runningTaksNum=`jq ".data.runningTaskNumber" res`
    if [ "$runningTaksNum" = "0" ]; then
      break
    fi
    sleep 30s
  done
fi

offlineUrl=$IP:$SERVER_PORT/api/rest_j/v1/offline
echo "Begin to offline $SERVER_NAME with curl $offlineUrl"
curl "$offlineUrl"

