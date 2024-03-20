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
source ~/.bash_profile
shellDir=`dirname $0`
workDir=`cd ${shellDir}/..;pwd`
source ${workDir}/bin/common.sh
source ${workDir}/deploy-config/linkis-env.sh
source ${workDir}/deploy-config/db.sh
source ~/.bash_profile
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

# --- 1. check command
    hdfs version > /dev/null 2>&1
    isSuccess "execute cmd: hdfs version"

# --- 2. check version
    hadoopVersion=`hdfs version`
    defaultHadoopVersion="3.3"

    checkversion "$hadoopVersion" $defaultHadoopVersion hadoop

# ---3. check service status
    hdfs dfsadmin -report > /dev/null 2>&1
    isSuccess "execute cmd: hdfs dfsadmin -report"

}

function checkHive(){

# --- 1. check command
    hive --version > /dev/null 2>&1
    isSuccess "execute cmd: hive --version"

# --- 2. check version & Parameters
    checkversion "$(whereis hive)" "3.1" hive

    hiveServer2Host=`hive -e "set hive.server2.thrift.bind.host;" | grep 'hive.server2.thrift.bind.host' | awk -F '=' '{print $2}' | sed 's/^[[:space:]]*//; s/[[:space:]]*|[[:space:]]*$//'`
    hiveServer2Port=`hive -e "set hive.server2.thrift.port;" | grep 'hive.server2.thrift.port' | awk -F '=' '{print $2}' | sed 's/^[[:space:]]*//; s/[[:space:]]*|[[:space:]]*$//'`
    hiveServer2ClientUser=`hive -e "set hive.server2.thrift.client.user;" | grep 'hive.server2.thrift.client.user' | awk -F '=' '{print $2}' | sed 's/^[[:space:]]*//; s/[[:space:]]*|[[:space:]]*$//'`
    hiveServer2ClientPassword=`hive -e "set hive.server2.thrift.client.password;" | grep 'hive.server2.thrift.client.password' | awk -F '=' '{print $2}' | sed 's/^[[:space:]]*//; s/[[:space:]]*|[[:space:]]*$//'`

    if [ -z "${hiveServer2Host}" ] || [ -z "${hiveServer2Port}" ] || [ -z "${hiveServer2ClientUser}" ] || [ -z "${hiveServer2ClientPassword}" ] ;then
      echo "Parameter [hiveServer2Host/hiveServer2Port/hiveServer2ClientUser/hiveServer2ClientPassword] are Invalid,Pls check"
      exit 2
    fi

# --- 3. check server status
    hiveServer2Url="jdbc:hive2://"${hiveServer2Host}":"${hiveServer2Port}
    beeline -u ${hiveServer2Url} -n ${hiveServer2ClientUser} -p ${hiveServer2ClientPassword} -e "show databases"
    if [ $? -eq 0 ]; then
      isSuccess "execute cmd: beeline -u${hiveServer2Url} -n${hiveServer2ClientUser} -p${hiveServer2ClientPassword} "
    else
      echo "beeline login failed, please check execute cmd: beeline -u${hiveServer2Url} -n${hiveServer2ClientUser} -p${hiveServer2ClientPassword} "
      exit 1
    fi

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

# --- 1. check command
 spark-submit --version > /dev/null 2>&1
 isSuccess "execute cmd: spark-submit --version "

# --- 2. check Parameters
  if [ -z "${SPARK_HOME}" ];then
     echo "Parameter SPARK_HOME is not valid, Please check"
     exit 2
  fi

# --- 3. check server status
 spark-submit --class org.apache.spark.examples.SparkPi --master local ${SPARK_HOME}/examples/jars/spark-examples_*.jar 10 > /dev/null 2>&1 
 isSuccess "execute cmd: spark-submit --class org.apache.spark.examples.SparkPi "

}

function checkMysql(){

     if [ -z "${MYSQL_HOST}" ] || [ -z "${MYSQL_PORT}" ] || [ -z "${MYSQL_DB}" ] || [ -z "${MYSQL_USER}" ] || [ -z "${MYSQL_PASSWORD}" ];then
        echo "MYSQL_HOST/MYSQL_PORT/MYSQL_USER/MYSQL_PASSWORD] are  Invalid,Pls check parameter define"
        exit 2
     fi

    mysql -h${MYSQL_HOST} -P${MYSQL_PORT} -u${MYSQL_USER} -p${MYSQL_PASSWORD} -e "select version();">/dev/null 2>&1
    isSuccess "execute cmd: mysql -h${MYSQL_HOST} -P${MYSQL_PORT}"
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


echo -e "1. <-----start to check used cmd---->\n"

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
echo "check shell"
need_cmd $SHELL


echo -e "\n<-----end to check used cmd---->"

# --- Begin to check Spark/HDFS/Hive Service Status

echo -e "\n2. <-----start to check service status---->\n"
checkPythonAndJava
checkMysql

if [ "$ENABLE_SPARK" == "true" ]; then
  echo "check spark-submit"
  need_cmd spark-submit
  echo "check spark-shell"
  need_cmd spark-shell
  echo "check spark-sql"
  need_cmd spark-sql
  checkSpark
fi

if [ "$ENABLE_HDFS" == "true" ]; then
  echo "check hadoop cmd"
  need_cmd hadoop
  echo "check hdfs cmd"
  need_cmd hdfs
  checkHdfs
fi

if [ "$ENABLE_HIVE" == "true" ]; then
  checkHive
fi

echo -e "\n<-----End to check service status---->"

# --- check Service Port
echo -e "\n3. <-----Start to check service Port---->"

if [ "$DISCOVERY" == "EUREKA" ]; then
  SERVER_PORT=$EUREKA_PORT
  check_service_port
fi

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

echo -e "\n<-----End to check service Port---->"
