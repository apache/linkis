#!/bin/bash
#
# Copyright 2019 WeBank
#
# Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# description:  Start all Server
#
# @name:        start-all
# @created:     01.16.2021
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





source ${LINKIS_HOME}/bin/common.sh

local_host="`hostname --fqdn`"

ipaddr=$(ip addr | awk '/^[0-9]+: / {}; /inet.*global/ {print gensub(/(.*)\/(.*)/, "\\1", "g", $2)}')



function startApp(){
echo "<-------------------------------->"
echo "Begin to start $SERVER_NAME"
SERVER_START_CMD="sh $LINKIS_HOME/sbin/linkis-daemon.sh restart $SERVER_NAME"
if test -z "$SERVER_IP"
then
  SERVER_IP=$local_host
fi

isLocal $SERVER_IP
flag=$?
echo "Is local "$flag
if [ $flag == "0" ];then
   eval $SERVER_START_CMD
else
   ssh -p $SSH_PORT $SERVER_IP $SERVER_START_CMD
fi
isSuccess "End to start $SERVER_NAME"
echo "<-------------------------------->"
sleep 3
}


#eureka
export SERVER_NAME="mg-eureka"
SERVER_IP=$EUREKA_INSTALL_IP
startApp


#gateway
SERVER_NAME="mg-gateway"
SERVER_IP=$GATEWAY_INSTALL_IP
startApp

#publicservice
SERVER_NAME="ps-publicservice"
SERVER_IP=$PUBLICSERVICE_INSTALL_IP
startApp


#metadata
SERVER_NAME="ps-datasource"
SERVER_IP=$DATASOURCE_INSTALL_IP
startApp

#bml
SERVER_NAME="ps-bml"
SERVER_IP=$BML_INSTALL_IP
startApp

#cs-server
SERVER_NAME="ps-cs"
SERVER_IP=$CS_INSTALL_IP
startApp


#manager
SERVER_NAME="cg-linkismanager"
SERVER_IP=$MANAGER_INSTALL_IP
startApp





#entrnace
SERVER_NAME="cg-entrance"
SERVER_IP=$ENTRANCE_INSTALL_IP
startApp

#ecm
SERVER_NAME="cg-engineconnmanager"
SERVER_IP=$ENGINECONNMANAGER_INSTALL_IP
startApp

#ecp
SERVER_NAME="cg-engineplugin"
SERVER_IP=$ENGINECONN_PLUGIN_SERVER_INSTALL_IP
startApp

echo "start-all shell script executed completely"

echo "Start to check all dss microservice"

function checkServer(){
echo "<-------------------------------->"
echo "Begin to check $SERVER_NAME"
if test -z "$SERVER_IP"
then
  SERVER_IP=$local_host
fi

SERVER_BIN=${LINKIS_HOME}/$SERVER_NAME/bin

if ! executeCMD $SERVER_IP "test -e $SERVER_BIN"; then
  echo "$SERVER_NAME is not installed,the checkServer steps will be skipped"
  return
fi

sh $workDir/bin/checkServices.sh $SERVER_NAME $SERVER_IP $SERVER_PORT
isSuccess "start $SERVER_NAME "
echo "<-------------------------------->"
sleep 5
}

echo "Linkis started successfully"
