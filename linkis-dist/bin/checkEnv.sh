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

shellDir=`dirname $0`
workDir=`cd ${shellDir}/..;pwd`
source ${workDir}/bin/common.sh
source ${workDir}/deploy-config/linkis-env.sh

say() {
    printf 'check command fail \n %s\n' "$1"
}

err() {
    say "$1" >&2
    exit 1
}

function checkPythonAndJava(){
    python --version > /dev/null 2>&1
    isSuccess "execute cmd: python --version"
    java -version > /dev/null 2>&1
    isSuccess "execute cmd: java --version"
}

function checkHdfs(){
    hadoopVersion="`hdfs version`"
    defaultHadoopVersion="3.3"
    checkversion "$hadoopVersion" $defaultHadoopVersion hadoop
}

function checkHive(){
    checkversion "$(whereis hive)" "3.1" hive
}

function checkversion(){
versionStr=$1
defaultVersion=$2
module=$3

result=$(echo $versionStr | grep "$defaultVersion")
if [ -n "$result" ]; then
    echo -e "Your [$module] version may match default support version: $defaultVersion\n"
else
   echo "WARN: Your [$module] version is not match default support version: $defaultVersion, there may be compatibility issues:"
   echo " 1: Continue installation, there may be compatibility issues"
   echo " 2: Exit installation"
   echo -e " other: exit\n"

   read -p "[Please input your choice]:"  idx
   if [[ '1' != "$idx" ]];then
    echo -e "You chose  Exit installation\n"
    exit 1
   fi
   echo ""
fi
}

function checkSpark(){
 spark-submit --version > /dev/null 2>&1
 isSuccess "execute cmd: spark-submit --version "
}

portIsOccupy=false
function check_service_port() {
    pid=`lsof -i TCP:$SERVER_PORT | fgrep LISTEN`
    if [ "$pid" != "" ];then
      echo "$SERVER_PORT already used"
      portIsOccupy=true
    fi
}

check_cmd() {
    command -v "$1" > /dev/null 2>&1
}

need_cmd() {
    if ! check_cmd "$1"; then
        err "need '$1' (your linux command not found)"
    fi
}


echo "<-----start to check used cmd---->"
echo "check yum"
need_cmd yum
echo "check java"
need_cmd java
echo "check mysql"
need_cmd mysql
echo "check telnet"
need_cmd telnet
echo "check tar"
need_cmd tar
echo "check sed"
need_cmd sed
echo "check lsof"
need_cmd lsof
echo "<-----end to check used cmd---->"

checkPythonAndJava

SERVER_PORT=$EUREKA_PORT
check_service_port

SERVER_PORT=$GATEWAY_PORT
check_service_port

SERVER_PORT=$MANAGER_PORT
check_service_port

SERVER_PORT=$ENGINECONNMANAGER_PORT
check_service_port

SERVER_PORT=$ENTRANCE_PORT
check_service_port

SERVER_PORT=$PUBLICSERVICE_PORT
check_service_port

if [ "$portIsOccupy" = true ];then
  echo "The port is already in use, please check before installing"
  exit 1
fi

if [ "$ENABLE_SPARK" == "true" ]; then
  checkSpark
fi


if [ "$ENABLE_HDFS" == "true" ]; then
  checkHdfs
fi


if [ "$ENABLE_HIVE" == "true" ]; then
  checkHive
fi
