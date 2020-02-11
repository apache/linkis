#!/usr/bin/env bash
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



# Start all linkis applications
info="We will start all linkis applications, it will take some time, please wait"
echo ${info}

#Actively load user env
source /etc/profile
source ~/.bash_profile

shellDir=`dirname $0`
workDir=`cd ${shellDir}/..;pwd`

CONF_DIR="${workDir}"/conf
export LINKIS_DSS_CONF_FILE=${LINKIS_DSS_CONF_FILE:-"${CONF_DIR}/config.sh"}
export DISTRIBUTION=${DISTRIBUTION:-"${CONF_DIR}/config.sh"}
#source $LINKIS_DSS_CONF_FILE
source ${DISTRIBUTION}
function isSuccess(){
if [ $? -ne 0 ]; then
    echo "Failed to " + $1
    exit 1
else
    echo "Succeed to" + $1
fi
}


local_host="`hostname --fqdn`"

ipaddr=$(ip addr | awk '/^[0-9]+: / {}; /inet.*global/ {print gensub(/(.*)\/(.*)/, "\\1", "g", $2)}')

function isLocal(){
    if [ "$1" == "127.0.0.1" ];then
        return 0
    elif [ $1 == "localhost" ]; then
        return 0
    elif [ $1 == $local_host ]; then
        return 0
    elif [ $1 == $ipaddr ]; then
        return 0
    fi
        return 1
}

function executeCMD(){
   isLocal $1
   flag=$?
   echo "Is local "$flag
   if [ $flag == "0" ];then
      eval $2
   else
      ssh -p $SSH_PORT $1 $2
   fi

}

#if there is no LINKIS_INSTALL_HOME，we need to source config again
if [ -z ${LINKIS_INSTALL_HOME} ];then
    echo "Info: LINKIS_INSTALL_HOME does not exist, we will source config"
    if [ ! -f "${LINKIS_DSS_CONF_FILE}" ];then
        echo "Error: can not find config file, start applications failed"
        exit 1
    else
        source ${LINKIS_DSS_CONF_FILE}
    fi
fi
APP_PREFIX="linkis-"

function startApp(){
echo "<-------------------------------->"
echo "Begin to start $SERVER_NAME"
SERVER_PATH=${APP_PREFIX}${SERVER_NAME}
SERVER_BIN=${LINKIS_INSTALL_HOME}/${SERVER_PATH}/bin
SERVER_LOCAL_START_CMD="dos2unix ${SERVER_BIN}/* > /dev/null 2>&1; dos2unix ${SERVER_BIN}/../conf/* > /dev/null 2>&1; sh ${SERVER_BIN}/start-${SERVER_NAME}.sh"
SERVER_REMOTE_START_CMD="source /etc/profile;source ~/.bash_profile;cd ${SERVER_BIN}; dos2unix ./* > /dev/null 2>&1; dos2unix ../conf/* > /dev/null 2>&1; sh start-${SERVER_NAME}.sh > /dev/null 2>&1"
if test -z "$SERVER_IP"
then
  SERVER_IP=$local_host
fi

if ! executeCMD $SERVER_IP "test -e $SERVER_BIN"; then
  echo "$SERVER_NAME is not installed,the startup steps will be skipped"
  return
fi

isLocal $SERVER_IP
flag=$?
echo "Is local "$flag
if [ $flag == "0" ];then
   eval $SERVER_LOCAL_START_CMD
else
   ssh -p $SSH_PORT $SERVER_IP $SERVER_REMOTE_START_CMD
fi
isSuccess "End to start $SERVER_NAME"
echo "<-------------------------------->"
sleep 3
}


#eureka
SERVER_NAME="eureka"
APP_PREFIX=""
SERVER_IP=$EUREKA_INSTALL_IP
startApp


APP_PREFIX="linkis-"
#gateway
SERVER_NAME="gateway"
SERVER_IP=$GATEWAY_INSTALL_IP
startApp

#publicservice
SERVER_NAME="publicservice"
SERVER_IP=$PUBLICSERVICE_INSTALL_IP
startApp


#metadata
SERVER_NAME="metadata"
SERVER_IP=$METADATA_INSTALL_IP
startApp

#bml
SERVER_NAME="bml"
SERVER_IP=$BML_INSTALL_IP
startApp

#resourcemanager
SERVER_NAME="resourcemanager"
SERVER_IP=$RESOURCEMANAGER_INSTALL_IP
startApp
echo "sleep 15 seconds to wait RM to be ready"
sleep 15

APP_PREFIX="linkis-ujes-"

#python-entrance
SERVER_NAME="python-entrance"
SERVER_IP=$PYTHON_INSTALL_IP
startApp

#python-enginemanager
SERVER_NAME="python-enginemanager"
SERVER_IP=$PYTHON_INSTALL_IP
startApp

#shell-entrance
SERVER_NAME="shell-entrance"
SERVER_IP=$SHELL_INSTALL_IP
startApp

#shell-enginemanager
SERVER_NAME="shell-enginemanager"
SERVER_IP=$SHELL_INSTALL_IP
startApp

#spark-entrance
SERVER_NAME="spark-entrance"
SERVER_IP=$SPARK_INSTALL_IP
startApp

#spark-enginemanager
SERVER_NAME="spark-enginemanager"
SERVER_IP=$SPARK_INSTALL_IP
startApp

#hive-entrance
SERVER_NAME="hive-entrance"
SERVER_IP=$HIVE_INSTALL_IP
startApp


#hive-enginemanager
SERVER_NAME="hive-enginemanager"
SERVER_IP=$HIVE_INSTALL_IP
startApp


#JDBCEntrance
SERVER_NAME="jdbc-entrance"
SERVER_IP=$JDBC_INSTALL_IP
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

SERVER_BIN=${LINKIS_INSTALL_HOME}/$SERVER_NAME/bin

if ! executeCMD $SERVER_IP "test -e $SERVER_BIN"; then
  echo "$SERVER_NAME is not installed,the checkServer steps will be skipped"
  return
fi

sh $workDir/bin/checkServices.sh $SERVER_NAME $SERVER_IP $SERVER_PORT
isSuccess "start $SERVER_NAME "
echo "<-------------------------------->"
sleep 3
}
SERVER_NAME="eureka"
SERVER_IP=$EUREKA_INSTALL_IP
SERVER_PORT=$EUREKA_PORT
checkServer

APP_PREFIX="linkis-"
SERVER_NAME=$APP_PREFIX"gateway"
SERVER_IP=$GATEWAY_INSTALL_IP
SERVER_PORT=$GATEWAY_PORT
checkServer

SERVER_NAME=$APP_PREFIX"publicservice"
SERVER_IP=$PUBLICSERVICE_INSTALL_IP
SERVER_PORT=$PUBLICSERVICE_PORT
checkServer

SERVER_NAME=$APP_PREFIX"metadata"
SERVER_IP=$METADATA_INSTALL_IP
SERVER_PORT=$METADATA_PORT
checkServer

SERVER_NAME=$APP_PREFIX"resourcemanager"
SERVER_IP=$RESOURCEMANAGER_INSTALL_IP
SERVER_PORT=$RESOURCEMANAGER_PORT
checkServer


SERVER_NAME=$APP_PREFIX"bml"
SERVER_IP=$BML_INSTALL_IP
SERVER_PORT=$BML_PORT
checkServer

APP_PREFIX="linkis-ujes-"
SERVER_NAME=$APP_PREFIX"python-entrance"
SERVER_IP=$PYTHON_INSTALL_IP
SERVER_PORT=$PYTHON_ENTRANCE_PORT
checkServer

SERVER_NAME=$APP_PREFIX"python-enginemanager"
SERVER_IP=$PYTHON_INSTALL_IP
SERVER_PORT=$PYTHON_EM_PORT
checkServer

SERVER_NAME=$APP_PREFIX"spark-entrance"
SERVER_IP=$SPARK_INSTALL_IP
SERVER_PORT=$SPARK_ENTRANCE_PORT
checkServer

SERVER_NAME=$APP_PREFIX"spark-enginemanager"
SERVER_IP=$SPARK_INSTALL_IP
SERVER_PORT=$SPARK_EM_PORT
checkServer

SERVER_NAME=$APP_PREFIX"hive-enginemanager"
SERVER_IP=$HIVE_INSTALL_IP
SERVER_PORT=$HIVE_EM_PORT
checkServer

SERVER_NAME=$APP_PREFIX"hive-entrance"
SERVER_IP=$HIVE_INSTALL_IP
SERVER_PORT=$HIVE_ENTRANCE_PORT
checkServer

SERVER_NAME=$APP_PREFIX"jdbc-entrance"
SERVER_IP=$JDBC_INSTALL_IP
SERVER_PORT=$JDBC_ENTRANCE_PORT
checkServer

echo "Linkis started successfully"
