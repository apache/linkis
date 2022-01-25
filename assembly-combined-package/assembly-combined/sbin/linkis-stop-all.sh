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



#gateway
SERVER_NAME="mg-gateway"
SERVER_IP=$GATEWAY_INSTALL_IP
stopApp

#cs-server
SERVER_NAME="ps-cs"
SERVER_IP=$CS_INSTALL_IP
stopApp

#ecm
SERVER_NAME="cg-engineconnmanager"
SERVER_IP=$ENGINECONNMANAGER_INSTALL_IP
stopApp


#entrnace
SERVER_NAME="cg-entrance"
SERVER_IP=$ENTRANCE_INSTALL_IP
stopApp

#ecp
SERVER_NAME="cg-engineplugin"
SERVER_IP=$ENGINECONN_PLUGIN_SERVER_INSTALL_IP
stopApp

#publicservice
SERVER_NAME="ps-publicservice"
SERVER_IP=$PUBLICSERVICE_INSTALL_IP
stopApp

#manager
SERVER_NAME="cg-linkismanager"
SERVER_IP=$MANAGER_INSTALL_IP
stopApp

#eureka
export SERVER_NAME="mg-eureka"
SERVER_IP=$EUREKA_INSTALL_IP
stopApp

echo "stop-all shell script executed completely"
