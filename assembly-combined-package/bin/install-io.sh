#!/bin/sh
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

#Actively load user env

workDir=$1
if [ "$workDir" == "" ];then
  echo "workDir is empty using shell dir"
  shellDir=`dirname $0`
  workDir=`cd ${shellDir}/..;pwd`
fi
echo "workDir is $workDir"

source ${workDir}/bin/common.sh
## copy io-entrance lib
export LINKIS_CONFIG_PATH=${LINKIS_CONFIG_PATH:-"${workDir}/conf/config.sh"}
source ${LINKIS_CONFIG_PATH}

rm -rf $workDir/share/linkis/ujes/io/linkis-ujes-io-entrance-bak;mv -f $workDir/share/linkis/ujes/io/linkis-ujes-io-entrance $workDir/share/linkis/ujes/io/linkis-ujes-io-entrance-bak
cd $workDir/share/linkis/ujes/io/;unzip -o $workDir/share/linkis/ujes/io/linkis-ujes-io-entrance.zip > /dev/null
isSuccess "unzip  linkis-ujes-io-entrance.zip"
cd -

function copyEntrance(){
   copyFile "$SERVER_IP" "${workDir}/share/linkis/ujes/io/linkis-ujes-io-entrance/lib/linkis-io-entrance-${LINKIS_VERSION}.jar"  "$SERVER_HOME/$SERVER_NAME/lib/"
   copyFile "$SERVER_IP" "${workDir}/share/linkis/ujes/io/linkis-ujes-io-entrance/lib/linkis-entrance-client-${LINKIS_VERSION}.jar"  "$SERVER_HOME/$SERVER_NAME/lib/"
   isSuccess "copy io-entrance to $SERVER_NAME "
}

SERVER_HOME=$LINKIS_INSTALL_HOME


SERVER_NAME=linkis-publicservice
SERVER_IP=$PUBLICSERVICE_INSTALL_IP
copyEntrance

SERVER_NAME=linkis-ujes-python-entrance
SERVER_IP=$PYTHON_INSTALL_IP
copyEntrance

SERVER_NAME=linkis-ujes-hive-entrance
SERVER_IP=$HIVE_INSTALL_IP
copyEntrance

SERVER_NAME=linkis-ujes-spark-entrance
SERVER_IP=$SPARK_INSTALL_IP
copyEntrance


SERVER_NAME=linkis-ujes-shell-entrance
SERVER_IP=$SHELL_INSTALL_IP
copyEntrance

SERVER_NAME=linkis-ujes-jdbc-entrance
SERVER_IP=$JDBC_INSTALL_IP
copyEntrance