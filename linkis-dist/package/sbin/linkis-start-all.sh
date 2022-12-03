#!/bin/bash
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

# description:  Start all Server
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

# Start all linkis applications
info="We will start all linkis applications, it will take some time, please wait"
echo ${info}


source ${LINKIS_HOME}/sbin/common.sh

# set LINKIS_CONF_DIR
if [ "$LINKIS_CONF_DIR" = "" ]; then
  export LINKIS_CONF_DIR=$LINKIS_HOME/conf
fi
source $LINKIS_CONF_DIR/linkis-env.sh


function startApp(){
echo "<-------------------------------->"
echo "Begin to start $SERVER_NAME"
SERVER_START_CMD="sh $LINKIS_HOME/sbin/linkis-daemon.sh restart $SERVER_NAME"
if test -z "$SERVER_IP"
then
  SERVER_IP=$local_host
fi

executeCMD $SERVER_IP "$SERVER_START_CMD"

isSuccess "start $SERVER_NAME"
echo "<-------------------------------->"
sleep 3
}


#linkis-mg-eureka
export SERVER_NAME="mg-eureka"
SERVER_IP=$EUREKA_INSTALL_IP
startApp


#linkis-mg-gateway
SERVER_NAME="mg-gateway"
SERVER_IP=$GATEWAY_INSTALL_IP
startApp

#linkis-ps-publicservice
SERVER_NAME="ps-publicservice"
SERVER_IP=$PUBLICSERVICE_INSTALL_IP
startApp

echo "sleeping 15s before start cg-linkismanager, please be patient"
sleep 15

#linkis-cg-linkismanage
SERVER_NAME="cg-linkismanager"
SERVER_IP=$MANAGER_INSTALL_IP
startApp

echo "sleeping 20s before start cg-entrance, please be patient"
sleep 20

#linkis-cg-entrance
SERVER_NAME="cg-entrance"
SERVER_IP=$ENTRANCE_INSTALL_IP
startApp

#linkis-cg-engineconnmanager(ecm)
SERVER_NAME="cg-engineconnmanager"
SERVER_IP=$ENGINECONNMANAGER_INSTALL_IP
startApp

echo "start-all shell script executed completely"

echo "Start to check all linkis microservice"

function checkServer() {
echo "<-------------------------------->"
echo "Begin to check $SERVER_NAME"
SERVER_CHECK_CMD="sh $LINKIS_HOME/sbin/linkis-daemon.sh status $SERVER_NAME"
if test -z "$SERVER_IP"
then
  SERVER_IP=$local_host
fi

executeCMD $SERVER_IP "$SERVER_CHECK_CMD"

if [ $? -ne 0 ]; then
      ALL_SERVER_NAME=linkis-$SERVER_NAME
      LOG_PATH=$LINKIS_HOME/logs/$ALL_SERVER_NAME.log
      echo "ERROR: your $ALL_SERVER_NAME microservice does not start successful !!! ERROR logs as follows :"
      echo "Please check detail log, log path :$LOG_PATH"
      echo '<---------------------------------------------------->'
      executeCMD $ALL_SERVER_NAME "tail -n 50 $LOG_PATH"
      echo '<---------------------------------------------------->'
      echo "Please check detail log, log path :$LOG_PATH"
      exit 1
fi
echo "<-------------------------------->"
sleep 3
}

#linkis-mg-eureka
export SERVER_NAME="mg-eureka"
SERVER_IP=$EUREKA_INSTALL_IP
checkServer


#linkis-mg-gateway
SERVER_NAME="mg-gateway"
SERVER_IP=$GATEWAY_INSTALL_IP
checkServer

#linkis-ps-publicservice
SERVER_NAME="ps-publicservice"
SERVER_IP=$PUBLICSERVICE_INSTALL_IP
checkServer

#linkis-cg-linkismanager
SERVER_NAME="cg-linkismanager"
SERVER_IP=$MANAGER_INSTALL_IP
checkServer


#linkis-cg-entrance
SERVER_NAME="cg-entrance"
SERVER_IP=$ENTRANCE_INSTALL_IP
checkServer

#linkis-cg-engineconnmanager(ecm)
SERVER_NAME="cg-engineconnmanager"
SERVER_IP=$ENGINECONNMANAGER_INSTALL_IP
checkServer

echo "Apache Linkis started successfully"
