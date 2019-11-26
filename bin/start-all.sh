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
source ~/.bash_profile

workDir=`dirname "${BASH_SOURCE-$0}"`
workDir=`cd "$workDir"; pwd`


CONF_DIR="${workDir}"/../conf
CONF_FILE=${CONF_DIR}/config.sh

function isSuccess(){
if [ $? -ne 0 ]; then
    echo "ERROR:  " + $1
    exit 1
else
    echo "INFO:" + $1
fi
}

sudo yum -y install dos2unix > /dev/null 2>&1


local_host="`hostname --fqdn`"

#if there is no LINKIS_INSTALL_HOME，we need to source config again
if [ -z ${LINKIS_INSTALL_HOME} ];then
    echo "Warning: LINKIS_INSTALL_HOME does not exist, we will source config"
    if [ ! -f "${CONF_FILE}" ];then
        echo "Error: can not find config file, start applications failed"
        exit 1
    else
        source ${CONF_FILE}
    fi
fi
APP_PREFIX="linkis-"

function startApp(){
echo "<-------------------------------->"
echo "Begin to start $SERVER_NAME"
SERVER_PATH=${APP_PREFIX}${SERVER_NAME}
SERVER_BIN=${LINKIS_INSTALL_HOME}/${SERVER_PATH}/bin
SERVER_START_CMD="source ~/.bash_profile;cd ${SERVER_BIN}; dos2unix ./* > /dev/null 2>&1; dos2unix ../conf/* > /dev/null 2>&1; sh start-${SERVER_NAME}.sh > /dev/null 2>&1"
if [ -n "${SERVER_IP}"  ];then
    ssh -p $SSH_PORT ${SERVER_IP} "${SERVER_START_CMD}"
else
    ssh -p $SSH_PORT ${local_host} "${SERVER_START_CMD}"
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

#bml
SERVER_NAME="bml"
SERVER_IP=$BML_INSTALL_IP
startApp


#metadata
SERVER_NAME="metadata"
SERVER_IP=$METADATA_INSTALL_IP
startApp

#resourcemanager
SERVER_NAME="resourcemanager"
SERVER_IP=$RESOURCEMANAGER_INSTALL_IP
startApp
echo "sleep 15 seconds to wait RM to be ready"
sleep 15

APP_PREFIX="linkis-ujes-"
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

#python-entrance
SERVER_NAME="python-entrance"
SERVER_IP=$PYTHON_INSTALL_IP
startApp

#python-enginemanager
SERVER_NAME="python-enginemanager"
SERVER_IP=$PYTHON_INSTALL_IP
startApp



#JDBCEntrance
SERVER_NAME="jdbc-entrance"
SERVER_IP=$JDBC_INSTALL_IP
startApp


#mlsql-entrance
SERVER_NAME="mlsql-entrance"
SERVER_IP=$MLSQL_INSTALL_IP
startApp

echo "start-all shell script executed completely"
