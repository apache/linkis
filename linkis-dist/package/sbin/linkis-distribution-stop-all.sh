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
info="We will stop all linkis applications, it will take some time, please wait"
echo ${info}


source ${LINKIS_HOME}/sbin/common.sh

# set LINKIS_CONF_DIR
if [ "$LINKIS_CONF_DIR" = "" ]; then
  export LINKIS_CONF_DIR=$LINKIS_HOME/conf
fi
source $LINKIS_CONF_DIR/linkis-env.sh

source $LINKIS_CONF_DIR/linkis-distribution-env.sh

function stopApp(){
echo "<-------------------------------->"
echo "Begin to stop $SERVER_NAME"
SERVER_START_CMD="sh $LINKIS_HOME/sbin/linkis-daemon.sh stop $SERVER_NAME"
ip_arr=(`echo $EUREKA_SERVER_IPS | tr ',' ' '`)
for current_ip in ${ip_arr[*]}
do
echo "[$current_ip]"
executeCMD $current_ip "$SERVER_START_CMD"
done

isSuccess "stop $SERVER_NAME"
echo "<-------------------------------->"
sleep 1
}

echo "<-------------------------------->"
echo "Linkis manager data is being cleared"
sh $LINKIS_HOME/sbin/clear-server.sh

#linkis-mg-eureka
export SERVER_NAME="mg-eureka"
SERVER_IP=$EUREKA_SERVER_IPS
stopApp

#linkis-mg-gateway
SERVER_NAME="mg-gateway"
SERVER_IP=$GATEWAY_SERVER_IPS
stopApp

#linkis-ps-publicservice
SERVER_NAME="ps-publicservice"
SERVER_IP=$PUBLICSERVICE_SERVER_IPS
stopApp

#linkis-cg-linkismanage
SERVER_NAME="cg-linkismanager"
SERVER_IP=$MANAGER_SERVER_IPS
stopApp

#linkis-cg-entrance
SERVER_NAME="cg-entrance"
SERVER_IP=$ENTRANCE_SERVER_IPS
stopApp

#linkis-cg-engineconnmanager(ecm)
SERVER_NAME="cg-engineconnmanager"
SERVER_IP=$ENGINECONNMANAGER_SERVER_IPS
stopApp

echo "Apache Linkis all services stopped successfully in a distributed manner."