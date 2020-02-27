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



# Stop all linkis applications
info="We will stop all linkis applications, it will take some time, please wait"
echo ${info}

#Actively load user env
source /etc/profile
source ~/.bash_profile

workDir=`dirname "${BASH_SOURCE-$0}"`
workDir=`cd "$workDir"; pwd`


CONF_DIR="${workDir}"/../conf
export LINKIS_DSS_CONF_FILE=${LINKIS_DSS_CONF_FILE:-"${CONF_DIR}/config.sh"}
export DISTRIBUTION=${DISTRIBUTION:-"${CONF_DIR}/config.sh"}
source ${DISTRIBUTION}
function isSuccess(){
if [ $? -ne 0 ]; then
    echo "ERROR:  " + $1
    exit 1
else
    echo "INFO:" + $1
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
    echo "Warning: LINKIS_INSTALL_HOME does not exist, we will source config"
    if [ ! -f "${LINKIS_DSS_CONF_FILE}" ];then
        echo "Error: can not find config file, stop applications failed"
        exit 1
    else
        source ${LINKIS_DSS_CONF_FILE}
    fi
fi
APP_PREFIX="linkis-"

function stopApp(){
echo "<-------------------------------->"
echo "Begin to stop $SERVER_NAME"
SERVER_PATH=${APP_PREFIX}${SERVER_NAME}
SERVER_BIN=${LINKIS_INSTALL_HOME}/${SERVER_PATH}/bin
SERVER_LOCAL_STOP_CMD="sh ${SERVER_BIN}/stop-${SERVER_NAME}.sh"
SERVER_REMOTE_STOP_CMD="source /etc/profile;source ~/.bash_profile;cd ${SERVER_BIN}; sh stop-${SERVER_NAME}.sh "
if test -z "$SERVER_IP"
then
  SERVER_IP=$local_host
fi

if ! executeCMD $SERVER_IP "test -e $SERVER_BIN"; then
  echo "$SERVER_NAME is not installed,the stopApp steps will be skipped"
  return
fi

isLocal $SERVER_IP
flag=$?
echo "Is local "$flag
if [ $flag == "0" ];then
   eval $SERVER_LOCAL_STOP_CMD
else
   ssh -p $SSH_PORT $SERVER_IP $SERVER_REMOTE_STOP_CMD
fi
echo "<-------------------------------->"
sleep 3
}


#eureka
SERVER_NAME="eureka"
APP_PREFIX=""
SERVER_IP=$EUREKA_INSTALL_IP
stopApp


APP_PREFIX="linkis-"
#gateway
SERVER_NAME="gateway"
SERVER_IP=$GATEWAY_INSTALL_IP
stopApp

#publicservice
SERVER_NAME="publicservice"
SERVER_IP=$PUBLICSERVICE_INSTALL_IP
stopApp

#bml
SERVER_NAME="bml"
SERVER_IP=$BML_INSTALL_IP
stopApp


#metadata
SERVER_NAME="metadata"
SERVER_IP=$METADATA_INSTALL_IP
stopApp


APP_PREFIX="linkis-ujes-"

#python-entrance
SERVER_NAME="python-entrance"
SERVER_IP=$PYTHON_INSTALL_IP
stopApp

#python-enginemanager
SERVER_NAME="python-enginemanager"
SERVER_IP=$PYTHON_INSTALL_IP
stopApp

#shell-entrance
SERVER_NAME="shell-entrance"
SERVER_IP=$SHELL_INSTALL_IP
stopApp

#shell-enginemanager
SERVER_NAME="shell-enginemanager"
SERVER_IP=$SHELL_INSTALL_IP
stopApp

#spark-entrance
SERVER_NAME="spark-entrance"
SERVER_IP=$SPARK_INSTALL_IP
stopApp

#spark-enginemanager
SERVER_NAME="spark-enginemanager"
SERVER_IP=$SPARK_INSTALL_IP
stopApp

#hive-entrance
SERVER_NAME="hive-entrance"
SERVER_IP=$HIVE_INSTALL_IP
stopApp


#hive-enginemanager
SERVER_NAME="hive-enginemanager"
SERVER_IP=$HIVE_INSTALL_IP
stopApp



#JDBCEntrance
SERVER_NAME="jdbc-entrance"
SERVER_IP=$JDBC_INSTALL_IP
stopApp



APP_PREFIX="linkis-"
#resourcemanager
SERVER_NAME="resourcemanager"
SERVER_IP=$RESOURCEMANAGER_INSTALL_IP
stopApp

echo "stop-all shell script executed completely"
