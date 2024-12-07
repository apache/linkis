#!/usr/bin/env bash
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

info="We will prepare the distributed environment and distribute files, it will take some time, please wait"
echo ${info}

source ${LINKIS_HOME}/sbin/common.sh
# set LINKIS_CONF_DIR
if [ "$LINKIS_CONF_DIR" = "" ]; then
  export LINKIS_CONF_DIR=$LINKIS_HOME/conf
fi
source $LINKIS_CONF_DIR/linkis-env.sh

source $LINKIS_CONF_DIR/linkis-distribution-env.sh

unique_ips=()
function genUniqueIPs(){
  ips=${EUREKA_SERVER_IPS},${GATEWAY_SERVER_IPS},${PUBLICSERVICE_SERVER_IPS},${MANAGER_SERVER_IPS},${ENTRANCE_SERVER_IPS},${ENGINECONNMANAGER_SERVER_IPS}
  ip_arr=(`echo $ips | tr ',' ' '`)
  unique_ips=($(echo "${ip_arr[@]}" | tr ' ' '\n' | sort -u | tr '\n' ' '))
}
genUniqueIPs

function loopIPs(){
  ip_arr=(`echo $2 | tr ',' ' '`)
  for current_ip in ${ip_arr[*]}
    do
    eval "$1 $current_ip"
  done
}

function loopServices(){
  eval "$1 $2 $EUREKA_SERVER_IPS"
  eval "$1 $2 $GATEWAY_SERVER_IPS"
  eval "$1 $2 $PUBLICSERVICE_SERVER_IPS"
  eval "$1 $2 $MANAGER_SERVER_IPS"
  eval "$1 $2 $ENTRANCE_SERVER_IPS"
  eval "$1 $2 $ENGINECONNMANAGER_SERVER_IPS"
}

function checkSingleNetwork(){
   isLocal $1
   flag=$?
   if [ $flag == "0" ];then
      eval $2
   fi

  ping -c 1 $1 > /dev/null 2>&1
  if [ $? -ne 0 ]; then
    echo "ping $1 network failure. Please check the network"
    exit 1
  fi
}

function checkSingleNonEncryption(){
  isLocal $1
  flag=$?
  if [ $flag != "0" ];then
    res=`ssh hadoop@$1 -o PreferredAuthentications=publickey -o StrictHostKeyChecking=no "date" |wc -l`
    if [ $res -ne 1 ] ; then
      echo "ssh non-encryption is not configured for $1"
      exit 1
    fi
  fi
}

function checkServicePort() {
    pid=`ssh hadoop@$2 "lsof -i TCP:$1 | fgrep LISTEN"`
    if [ "$pid" != "" ];then
      echo "ip:$2 port:$1 already used"
      exit 1
    fi
}

function checkEnvVariable(){
  echo $1 $2
  isLocal $2
  flag=$?
  res=""
  if [ $flag != "0" ];then
    res=`ssh hadoop@$2 "echo $1"`
  else
    res=`echo $1`
  fi
  if [ $res == "" ] ; then
    echo "$2 is not configured env $1"
    exit 1
  fi
}

#1.check cluster configuration
echo "--------------------------------------------------------------------------"
echo "1.start checking and setting the cluster configuration."
# set
if [ "$EUREKA_SERVER_IPS" = "" ]; then
  export EUREKA_SERVER_IPS=127.0.0.1
fi

# set GATEWAY_SERVER_IPS
if [ "$GATEWAY_SERVER_IPS" = "" ]; then
  export GATEWAY_SERVER_IPS=127.0.0.1
fi

# set PUBLICSERVICE_SERVER_IPS
if [ "$PUBLICSERVICE_SERVER_IPS" = "" ]; then
  export PUBLICSERVICE_SERVER_IPS=127.0.0.1
fi

# set MANAGER_SERVER_IPS
if [ "$MANAGER_SERVER_IPS" = "" ]; then
  export MANAGER_SERVER_IPS=127.0.0.1
fi

# set
if [ "$ENTRANCE_SERVER_IPS" = "" ]; then
  export ENTRANCE_SERVER_IPS=127.0.0.1
fi

# set ENGINECONNMANAGER_SERVER_IPS
if [ "$ENGINECONNMANAGER_SERVER_IPS" = "" ]; then
  export ENGINECONNMANAGER_SERVER_IPS=127.0.0.1
fi
echo "check end, and everything is [OK]"
echo "--------------------------------------------------------------------------"
echo

#2.check network
echo "--------------------------------------------------------------------------"
echo "start checking the cluster configuration network."
loopServices "loopIPs" "checkSingleNetwork"
echo "check end, and everything is [OK]"
echo "--------------------------------------------------------------------------"
echo

#3.check non-encryption configuration
echo "--------------------------------------------------------------------------"
echo "start checking the non-encryption configuration."
loopServices "loopIPs" "checkSingleNonEncryption"
echo "check end, and everything is [OK]"
echo "--------------------------------------------------------------------------"
echo

#4.check port
echo "--------------------------------------------------------------------------"
echo "start checking service port."
loopIPs "checkServicePort $EUREKA_PORT" $EUREKA_SERVER_IPS
loopIPs "checkServicePort $GATEWAY_PORT" $GATEWAY_SERVER_IPS
loopIPs "checkServicePort $MANAGER_PORT" $PUBLICSERVICE_SERVER_IPS
loopIPs "checkServicePort $ENGINECONNMANAGER_PORT" $MANAGER_SERVER_IPS
loopIPs "checkServicePort $ENTRANCE_PORT" $ENTRANCE_SERVER_IPS
loopIPs "checkServicePort $PUBLICSERVICE_PORT" $ENGINECONNMANAGER_SERVER_IPS
echo "check end, and everything is [OK]"
echo "--------------------------------------------------------------------------"
echo

#5.check necessary environment
echo "--------------------------------------------------------------------------"
echo "start checking service env."
echo

#java
loopIPs "checkEnvVariable '\$JAVA_HOME'" $EUREKA_SERVER_IPS
loopIPs "checkEnvVariable '\$JAVA_HOME'" $GATEWAY_SERVER_IPS
loopIPs "checkEnvVariable '\$JAVA_HOME'" $PUBLICSERVICE_SERVER_IPS
loopIPs "checkEnvVariable '\$JAVA_HOME'" $MANAGER_SERVER_IPS
loopIPs "checkEnvVariable '\$JAVA_HOME'" $ENTRANCE_SERVER_IPS
loopIPs "checkEnvVariable '\$JAVA_HOME'" $ENGINECONNMANAGER_SERVER_IPS

#hadoop
loopIPs "checkEnvVariable '\$HADOOP_HOME'" $PUBLICSERVICE_SERVER_IPS

#hive
loopIPs "checkEnvVariable '\$HIVE_HOME'" $ENGINECONNMANAGER_SERVER_IPS

#spark
loopIPs "checkEnvVariable '\$SPARK_HOME'" $ENGINECONNMANAGER_SERVER_IPS

#python

#shell


echo "--------------------------------------------------------------------------"
echo "check end, and everything is [OK]"
echo

#6.check authority
echo "--------------------------------------------------------------------------"
echo "start checking authority."
echo "--------------------------------------------------------------------------"
echo "check end, and everything is [OK]"
echo

#7.update eureka configuration
echo "--------------------------------------------------------------------------"
echo "start updating eureka configuration."
eureka_str=""
eureka_ip_arr=(`echo $EUREKA_SERVER_IPS | tr ',' ' '`)
for current_ip in ${eureka_ip_arr[*]}
  do eureka_str="${eureka_str}http://$current_ip:$EUREKA_PORT/eureka/,"
done
eureka_str=${eureka_str%?}
sed -i ${txt}  "s#defaultZone:.*#defaultZone: $eureka_str#g" $LINKIS_HOME/sbin/application-eureka.yml
sed -i ${txt}  "s#defaultZone:.*#defaultZone: $eureka_str#g" $LINKIS_HOME/sbin/application-linkis.yml

echo "--------------------------------------------------------------------------"
echo "update end, and everything [OK]"
echo
echo "everything is prepared [OK]"
echo "--------------------------------------------------------------------------"



#8.check or create remote dir $linkis_home
echo "--------------------------------------------------------------------------"
echo "start checking or creating remote dir."
for remote_ip in  ${unique_ips[*]}
do
  if [ "ssh hadoop@$remote_ip -f $LINKIS_HOME" ];then
    continue
  else
    ssh hadoop@$remote_ip "mkdir $LINKIS_HOME"
    ssh hadoop@$remote_ip "sudo mkdir -p $LINKIS_HOME;sudo chown hadoop:hadoop -R $LINKIS_HOME"
  fi
done

echo "--------------------------------------------------------------------------"
echo "check end, and everything is [OK]"
echo

function xrsync(){
#1.go through all the directories, send them one by one
for host in ${unique_ips[*]}
do
   isLocal $host
   flag=$?
   if [ $flag == "0" ];then
      continue
   fi
  echo ==================== $host ===========
  #2.go through all the directories, send them one by one
  for file in $1
  do
    #3.check whether the file exists
    if [ -e $file ]
       then
         #4.obtain parent dir
         pdir=$(cd -P $(dirname $file); pwd)
         #5.obtain current file name
         fname=$(basename $file)
         ssh $host mkdir -p "$pdir"
         rsync -av $pdir/$fname $host:$pdir
    else
         echo $file does not exists!
    fi
  done
done
}

#9.distribute package
echo "--------------------------------------------------------------------------"
echo "start distributing package."
xrsync $LINKIS_HOME
echo "--------------------------------------------------------------------------"
echo "distribute end, and everything is [OK]"
echo
echo "everything prepared, and everything is [OK]"
echo "--------------------------------------------------------------------------"
echo