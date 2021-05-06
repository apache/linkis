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
source ~/.bash_profile
shellDir=`dirname $0`
workDir=`cd ${shellDir}/..;pwd`

#To be compatible with MacOS and Linux
txt=""
if [[ "$OSTYPE" == "darwin"* ]]; then
    txt="''"
elif [[ "$OSTYPE" == "linux-gnu" ]]; then
    # linux
    txt=""
elif [[ "$OSTYPE" == "cygwin" ]]; then
    echo "linkis not support Windows operating system"
    exit 1
elif [[ "$OSTYPE" == "msys" ]]; then
    echo "linkis not support Windows operating system"
    exit 1
elif [[ "$OSTYPE" == "win32" ]]; then
    echo "linkis not support Windows operating system"
    exit 1
elif [[ "$OSTYPE" == "freebsd"* ]]; then

    txt=""
else
    echo "Operating system unknown, please tell us(submit issue) for better service"
    exit 1
fi

function isSuccess(){
if [ $? -ne 0 ]; then
    echo "Failed to " + $1
    exit 1
else
    echo "Succeed to" + $1
fi
}

function checkPythonAndJava(){
    python --version
    isSuccess "execute python --version"
    java -version
    isSuccess "execute java --version"
}

function checkHadoopAndHive(){
    hadoopVersion="`hdfs version`"
    defaultHadoopVersion="2.7"
    checkversion "$hadoopVersion" $defaultHadoopVersion hadoop
    checkversion "$(whereis hive)" "1.2" hive
}

function checkversion(){
versionStr=$1
defaultVersion=$2
module=$3

result=$(echo $versionStr | grep "$defaultVersion")
if [ -n "$result" ]; then
    echo "$module version match"
else
   echo "WARN: Your $module version is not $defaultVersion, there may be compatibility issues:"
   echo " 1: Continue installation, there may be compatibility issues"
   echo " 2: Exit installation"
   echo ""
   read -p "Please input the choice:"  idx
   if [[ '2' = "$idx" ]];then
    echo "You chose  Exit installation"
    exit 1
   fi
fi
}

function checkSpark(){
 spark-submit --version
 isSuccess "execute spark-submit --version"
}

say() {
    printf 'check command fail \n %s\n' "$1"
}

err() {
    say "$1" >&2
    exit 1
}

check_cmd() {
    command -v "$1" > /dev/null 2>&1
}

need_cmd() {
    if ! check_cmd "$1"; then
        err "need '$1' (command not found)"
    fi
}



sh ${workDir}/bin/checkEnv.sh
isSuccess "check env"

##load config
echo "step1:load config "
export LINKIS_CONFIG_PATH=${LINKIS_CONFIG_PATH:-"${workDir}/conf/linkis-env.sh"}
export LINKIS_DB_CONFIG_PATH=${LINKIS_DB_CONFIG_PATH:-"${workDir}/conf/db.sh"}
source ${LINKIS_CONFIG_PATH}
source ${LINKIS_DB_CONFIG_PATH}
isSuccess "load config"
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
   if [ $flag == "0" ];then
      echo "Is local execution:$2"
      eval $2
   else
      echo "Is remote execution:$2"
      ssh -p $SSH_PORT $1 $2
   fi

}
function copyFile(){
   isLocal $1
   flag=$?
   src=$2
   dest=$3
   if [ $flag == "0" ];then
      echo "Is local cp "
      eval "cp -r $src $dest"
   else
      echo "Is remote cp "
      scp -r -P $SSH_PORT  $src $1:$dest
   fi

}


##env check
echo "Do you want to clear Linkis table information in the database?"
echo " 1: Do not execute table-building statements"
echo " 2: Dangerous! Clear all data and rebuild the tables"
echo " other: exit"
echo ""

MYSQL_INSTALL_MODE=1

#使用read参数[-p]后，允许在[-p]后面跟一字符串，在字符串后面跟n个shell变量。n个shell变量用来接收从shell界面输入的字符串
read -p "Please input the choice:"  idx
if [[ '2' = "$idx" ]];then
  MYSQL_INSTALL_MODE=2
  echo "You chose Rebuild the table"
elif [[ '1' = "$idx" ]];then
  MYSQL_INSTALL_MODE=1
  echo "You chose not execute table-building statements"
else
  echo "no choice,exit!"
  exit 1
fi

echo "create hdfs  directory and local directory"
if [ "$WORKSPACE_USER_ROOT_PATH" != "" ]
then
  localRootDir=$WORKSPACE_USER_ROOT_PATH
  if [[ $WORKSPACE_USER_ROOT_PATH == file://* ]];then
    localRootDir=${WORKSPACE_USER_ROOT_PATH#file://}
    mkdir -p $localRootDir/$deployUser
    sudo chmod -R 775 $localRootDir/$deployUser
  elif [[ $WORKSPACE_USER_ROOT_PATH == hdfs://* ]];then
    localRootDir=${WORKSPACE_USER_ROOT_PATH#hdfs://}
    hdfs dfs -mkdir -p $localRootDir/$deployUser
    hdfs dfs -chmod -R 775 $localRootDir/$deployUser
  else
    echo "does not support $WORKSPACE_USER_ROOT_PATH filesystem types"
  fi
fi
isSuccess "create  $WORKSPACE_USER_ROOT_PATH directory"


########################  init hdfs and db  ################################
 if [ "$HDFS_USER_ROOT_PATH" != "" ]
 then
     localRootDir=$HDFS_USER_ROOT_PATH
   if [[ $HDFS_USER_ROOT_PATH == file://* ]];then
     localRootDir=${HDFS_USER_ROOT_PATH#file://}
     mkdir -p $localRootDir/$deployUser
     sudo chmod -R 775 $localRootDir/$deployUser
   elif [[ $HDFS_USER_ROOT_PATH == hdfs://* ]];then
     localRootDir=${HDFS_USER_ROOT_PATH#hdfs://}
     hdfs dfs -mkdir -p $localRootDir/$deployUser
     hdfs dfs -chmod -R 775 $localRootDir/$deployUser
   else
     echo "does not support $HDFS_USER_ROOT_PATH filesystem types"
   fi
 fi
 isSuccess "create  $HDFS_USER_ROOT_PATH directory"


 if [ "$RESULT_SET_ROOT_PATH" != "" ]
 then
   localRootDir=$RESULT_SET_ROOT_PATH
   if [[ $RESULT_SET_ROOT_PATH == file://* ]];then
     localRootDir=${RESULT_SET_ROOT_PATH#file://}
         mkdir -p $localRootDir/$deployUser
         sudo chmod -R 775 $localRootDir/$deployUser
   elif [[ $RESULT_SET_ROOT_PATH == hdfs://* ]];then
     localRootDir=${RESULT_SET_ROOT_PATH#hdfs://}
         hdfs dfs -mkdir -p $localRootDir/$deployUser
         hdfs dfs -chmod -R 775 $localRootDir/$deployUser
   else
     echo "does not support $RESULT_SET_ROOT_PATH filesystem types"
   fi
 fi
 isSuccess "create  $RESULT_SET_ROOT_PATH directory"

## sql init
if [ "$YARN_RESTFUL_URL" != "" ]
then
  sed -i ${txt}  "s#@YARN_RESTFUL_URL#$YARN_RESTFUL_URL#g" $workDir/db/linkis_dml.sql
fi

common_conf=$workDir/conf/linkis.properties
SERVER_IP=$local_host

##Label set start
if [ "$SPARK_VERSION" != "" ]
then
  sed -i ${txt}  "s#spark-2.4.3#spark-$SPARK_VERSION#g" $workDir/db/linkis_dml.sql
  executeCMD $SERVER_IP   "sed -i ${txt}  \"s#\#wds.linkis.spark.engine.version.*#wds.linkis.spark.engine.version=$SPARK_VERSION#g\" $common_conf"
fi

if [ "$HIVE_VERSION" != "" ]
then
  sed -i ${txt}  "s#hive-1.2.1#hive-$HIVE_VERSION#g" $workDir/db/linkis_dml.sql
  executeCMD $SERVER_IP   "sed -i ${txt}  \"s#\#wds.linkis.hive.engine.version.*#wds.linkis.hive.engine.version=$HIVE_VERSION#g\" $common_conf"
fi

if [ "$PYTHON_VERSION" != "" ]
then
  sed -i ${txt}  "s#python-python2#python-$PYTHON_VERSION#g" $workDir/db/linkis_dml.sql
  executeCMD $SERVER_IP   "sed -i ${txt}  \"s#\#wds.linkis.python.engine.version.*#wds.linkis.python.engine.version=$PYTHON_VERSION#g\" $common_conf"
fi

##Label set end


#init db
if [[ '2' = "$MYSQL_INSTALL_MODE" ]];then
     mysql -h$MYSQL_HOST -P$MYSQL_PORT -u$MYSQL_USER -p$MYSQL_PASSWORD --default-character-set=utf8 -e "CREATE DATABASE IF NOT EXISTS $MYSQL_DB DEFAULT CHARSET utf8 COLLATE utf8_general_ci;"
  mysql -h$MYSQL_HOST -P$MYSQL_PORT -u$MYSQL_USER -p$MYSQL_PASSWORD -D$MYSQL_DB  --default-character-set=utf8 -e "source ${workDir}/db/linkis_ddl.sql"
  isSuccess "source linkis_ddl.sql"
  mysql -h$MYSQL_HOST -P$MYSQL_PORT -u$MYSQL_USER -p$MYSQL_PASSWORD -D$MYSQL_DB  --default-character-set=utf8 -e "source ${workDir}/db/linkis_dml.sql"
  isSuccess "source linkis_dml.sql"
  echo "Rebuild the table"
fi
###########################################################################
#Deal special symbol '#'
HIVE_META_PASSWORD=$(echo ${HIVE_META_PASSWORD//'#'/'\#'})
MYSQL_PASSWORD=$(echo ${MYSQL_PASSWORD//'#'/'\#'})

#Deal common config
echo "Update common config..."

if test -z "$GATEWAY_INSTALL_IP"
then
  export GATEWAY_INSTALL_IP="`hostname --fqdn`"
fi

executeCMD $SERVER_IP   "sed -i ${txt}  \"s#wds.linkis.server.version.*#wds.linkis.server.version=$LINKIS_SERVER_VERSION#g\" $common_conf"
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#wds.linkis.gateway.url.*#wds.linkis.gateway.url=http://$GATEWAY_INSTALL_IP:$GATEWAY_PORT#g\" $common_conf"
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#wds.linkis.eureka.defaultZone.*#wds.linkis.eureka.defaultZone=$EUREKA_URL#g\" $common_conf"
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#wds.linkis.server.mybatis.datasource.url.*#wds.linkis.server.mybatis.datasource.url=jdbc:mysql://${MYSQL_HOST}:${MYSQL_PORT}/${MYSQL_DB}?characterEncoding=UTF-8#g\" $common_conf"
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#wds.linkis.server.mybatis.datasource.username.*#wds.linkis.server.mybatis.datasource.username=$MYSQL_USER#g\" $common_conf"
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#wds.linkis.server.mybatis.datasource.password.*#wds.linkis.server.mybatis.datasource.password=$MYSQL_PASSWORD#g\" $common_conf"

executeCMD $SERVER_IP   "sed -i ${txt}  \"s#wds.linkis.ldap.proxy.url.*#wds.linkis.ldap.proxy.url=$LDAP_URL#g\" $common_conf"
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#wds.linkis.ldap.proxy.baseDN.*#wds.linkis.ldap.proxy.baseDN=$LDAP_BASEDN#g\" $common_conf"
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#wds.linkis.ldap.proxy.userNameFormat.*#wds.linkis.ldap.proxy.userNameFormat=$LDAP_USER_NAME_FORMAT#g\" $common_conf"
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#wds.linkis.admin.user.*#wds.linkis.gateway.admin.user=$deployUser#g\" $common_conf"
# hadoop config
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#\#hadoop.config.dir.*#hadoop.config.dir=$HADOOP_CONF_DIR#g\" $common_conf"
#hive config
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#\#hive.config.dir.*#hive.config.dir=$HIVE_CONF_DIR#g\" $common_conf"

#spark config
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#spark.config.dir.*#spark.config.dir=$SPARK_CONF_DIR#g\" $common_conf"

if [ "$HIVE_META_URL" != "" ]
then
  executeCMD $SERVER_IP   "sed -i ${txt}  \"s#hive.meta.url.*#hive.meta.url=$HIVE_META_URL#g\" $common_conf"
fi
if [ "$HIVE_META_USER" != "" ]
then
  executeCMD $SERVER_IP   "sed -i ${txt}  \"s#hive.meta.user.*#hive.meta.user=$HIVE_META_USER#g\" $common_conf"
fi
if [ "$HIVE_META_PASSWORD" != "" ]
then
  HIVE_META_PASSWORD=$(echo ${HIVE_META_PASSWORD//'#'/'\#'})
  executeCMD $SERVER_IP   "sed -i ${txt}  \"s#hive.meta.password.*#hive.meta.password=$HIVE_META_PASSWORD#g\" $common_conf"
fi


executeCMD $SERVER_IP   "sed -i ${txt}  \"s#wds.linkis.filesystem.root.path.*#wds.linkis.filesystem.root.path=$WORKSPACE_USER_ROOT_PATH#g\" $common_conf"
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#wds.linkis.filesystem.hdfs.root.path.*#wds.linkis.filesystem.hdfs.root.path=$HDFS_USER_ROOT_PATH#g\" $common_conf"

# engineconn
if test -z $ENGINECONN_ROOT_PATH
then
  ENGINECONN_ROOT_PATH=$workDir/engineroot
fi
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#wds.linkis.engineconn.root.dir.*#wds.linkis.engineconn.root.dir=$ENGINECONN_ROOT_PATH#g\" $common_conf"
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#wds.linkis.engineconn.home.*#wds.linkis.engineconn.home=${workDir}/lib/linkis-engineconn-plugins#g\" $common_conf"
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#wds.linkis.engineconn.plugin.loader.store.path.*#wds.linkis.engineconn.plugin.loader.store.path=${workDir}/lib/linkis-engineconn-plugins#g\" $common_conf"

# common lib
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#wds.linkis.public_module.path.*#wds.linkis.public_module.path=${workDir}/lib/linkis-commons/public-module#g\" $common_conf"

echo "Congratulations! You have installed Linkis $LINKIS_VERSION successfully, please use sbin/start-all.sh to start it!"





