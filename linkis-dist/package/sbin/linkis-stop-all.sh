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

# description:  Stop all Server
#
# Modified for Linkis 1.0.0
#Actively load user env
source /etc/profile
source ~/.bash_profile

cd `dirname $0`
cd ..
INSTALL_HOME=`pwd`

# set LINKIS_HOME
if [ "$LINKIS_HOME" = "" ]; then
  export LINKIS_HOME=$INSTALL_HOME
fi

info="We will stop all linkis applications, it will take some time, please wait"
echo ${info}

source ${LINKIS_HOME}/sbin/common.sh
# set LINKIS_CONF_DIR
if [ "$LINKIS_CONF_DIR" = "" ]; then
  export LINKIS_CONF_DIR=$LINKIS_HOME/conf
fi
source $LINKIS_CONF_DIR/linkis-env.sh

function stopApp(){
echo "<-------------------------------->"
echo "Begin to stop $SERVER_NAME"
SERVER_STOP_CMD="sh $LINKIS_HOME/sbin/linkis-daemon.sh stop $SERVER_NAME"
if test -z "$SERVER_IP"
then
  SERVER_IP=$local_host
fi

executeCMD $SERVER_IP "$SERVER_STOP_CMD"

echo "<-------------------------------->"
}

#ec
function stopEC(){
echo "<-------------------------------->"
echo "Begin to stop EC"
is_ec_service=`ps -ef | grep EngineConnServer | grep -v grep | tr -s ' ' | cut -d ' ' -f 2`
if [ "$is_ec_service" = "" ]; then
  echo "no ec service runniing."
else
  EC_STOP_CMD="ps -ef | grep EngineConnServer | grep -v grep | tr -s ' ' | cut -d ' ' -f 2 | xargs sudo kill"
  if test -z "$SERVER_IP"
  then
    SERVER_IP=$local_host
  fi
  executeCMD $SERVER_IP "$EC_STOP_CMD"
fi
echo "server ENGINECONNs is stopped"
echo "<-------------------------------->"
}


#ec
stopEC

#linkis-mg-gateway
SERVER_NAME="mg-gateway"
SERVER_IP=$GATEWAY_INSTALL_IP
stopApp

#linkis-cg-engineconnmanager(ecm)
SERVER_NAME="cg-engineconnmanager"
SERVER_IP=$ENGINECONNMANAGER_INSTALL_IP
stopApp

#linkis-cg-entrance
SERVER_NAME="cg-entrance"
SERVER_IP=$ENTRANCE_INSTALL_IP
stopApp

#linkis-ps-publicservice
SERVER_NAME="ps-publicservice"
SERVER_IP=$PUBLICSERVICE_INSTALL_IP
stopApp

#linkis-cg-linkismanager
SERVER_NAME="cg-linkismanager"
SERVER_IP=$MANAGER_INSTALL_IP
stopApp

#linkis-mg-eureka
if [ "$DISCOVERY" == "EUREKA" ]; then
  export SERVER_NAME="mg-eureka"
  SERVER_IP=$EUREKA_INSTALL_IP
  stopApp
fi

echo "stop-all shell script executed completely"
