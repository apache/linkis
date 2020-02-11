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
export LINKIS_CONFIG_PATH=${LINKIS_CONFIG_PATH:-"${workDir}/conf/config.sh"}
export LINKIS_DB_CONFIG_PATH=${LINKIS_DB_CONFIG_PATH:-"${workDir}/conf/db.sh"}
export DISTRIBUTION=${DISTRIBUTION:-"${workDir}/conf/config.sh"}
source ${LINKIS_CONFIG_PATH}
source ${LINKIS_DB_CONFIG_PATH}
source ${DISTRIBUTION}
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

##install mode choice

if [ "$INSTALL_MODE" == "" ];then
  echo "Please enter the mode selection such as: 1"
  echo " 1: Lite"
  echo " 2: Simple"
  echo " 3: Standard"
  echo ""
  read -p "Please input the choice:"  idx
  INSTALL_MODE=$idx
fi

if [[ '1' = "$INSTALL_MODE" ]];then
  echo "You chose Lite installation mode" 
  checkPythonAndJava
elif [[ '2' = "$INSTALL_MODE" ]];then
  echo "You chose Simple installation mode"
  checkPythonAndJava
  checkHadoopAndHive
elif [[ '3' = "$INSTALL_MODE" ]];then
  echo "You chose Standard installation mode"
  checkPythonAndJava
  checkHadoopAndHive
  checkSpark
else
  echo "no choice,exit!"
  exit 1
fi

##env check
echo "Do you want to clear Linkis table information in the database?"
echo " 1: Do not execute table-building statements"
echo " 2: Dangerous! Clear all data and rebuild the tables"
echo ""

MYSQL_INSTALL_MODE=1

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

##init db
if [[ '2' = "$MYSQL_INSTALL_MODE" ]];then
    mysql -h$MYSQL_HOST -P$MYSQL_PORT -u$MYSQL_USER -p$MYSQL_PASSWORD --default-character-set=utf8 -e "CREATE DATABASE IF NOT EXISTS $MYSQL_DB DEFAULT CHARSET utf8 COLLATE utf8_general_ci;"
	mysql -h$MYSQL_HOST -P$MYSQL_PORT -u$MYSQL_USER -p$MYSQL_PASSWORD -D$MYSQL_DB  --default-character-set=utf8 -e "source ${workDir}/db/linkis_ddl.sql"
	isSuccess "source linkis_ddl.sql"
	mysql -h$MYSQL_HOST -P$MYSQL_PORT -u$MYSQL_USER -p$MYSQL_PASSWORD -D$MYSQL_DB  --default-character-set=utf8 -e "source ${workDir}/db/linkis_dml.sql"
	isSuccess "source linkis_dml.sql"
	echo "Rebuild the table"
fi

##Deal special symbol '#'
HIVE_META_PASSWORD=$(echo ${HIVE_META_PASSWORD//'#'/'\#'})
MYSQL_PASSWORD=$(echo ${MYSQL_PASSWORD//'#'/'\#'})

##Eurkea install
SERVER_NAME=eureka
SERVER_IP=$EUREKA_INSTALL_IP
SERVER_PORT=$EUREKA_PORT
SERVER_HOME=$LINKIS_INSTALL_HOME
echo "$SERVER_NAME-step1: create dir"
if test -z "$SERVER_IP"
then
  SERVER_IP=$local_host
fi
EUREKA_URL=http://$SERVER_IP:$EUREKA_PORT/eureka/

if ! executeCMD $SERVER_IP "test -e $SERVER_HOME"; then
  executeCMD $SERVER_IP  "sudo mkdir -p $SERVER_HOME;sudo chown -R $deployUser:$deployUser $SERVER_HOME"
  isSuccess "create the dir of $SERVER_HOME"
fi

echo "$SERVER_NAME-step2:copy install package"
copyFile $SERVER_IP ${workDir}/share/springcloud/$SERVER_NAME/$SERVER_NAME.zip $SERVER_HOME
isSuccess "copy $SERVER_NAME"
executeCMD $SERVER_IP   "cd $SERVER_HOME/;rm -rf eureka;unzip  $SERVER_NAME.zip > /dev/null;cd -"
echo "$SERVER_NAME-step3:subsitution conf"
eureka_conf_path=$SERVER_HOME/$SERVER_NAME/conf/application-$SERVER_NAME.yml
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#port:.*#port: $SERVER_PORT#g\" $eureka_conf_path"
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#defaultZone:.*#defaultZone: $EUREKA_URL#g\" $eureka_conf_path"
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#hostname:.*#hostname: $SERVER_IP#g\" $eureka_conf_path"
isSuccess "subsitution conf of $SERVER_NAME"
echo "<----------------$SERVER_NAME:end------------------->"
##Eurkea install  end



##function
function installPackage(){
echo "$SERVER_NAME-step1: create dir"
if test -z "$SERVER_IP"
then
  SERVER_IP=$local_host
fi

if ! executeCMD $SERVER_IP "test -e $SERVER_HOME"; then
  executeCMD $SERVER_IP "sudo mkdir -p $SERVER_HOME;sudo chown -R $deployUser:$deployUser $SERVER_HOME"
  isSuccess "create the dir of  $SERVER_NAME"
fi

echo "$SERVER_NAME-step2:copy install package"
copyFile $SERVER_IP ${workDir}/share/$PACKAGE_DIR/$SERVER_NAME.zip $SERVER_HOME
isSuccess "copy  ${SERVER_NAME}.zip"
executeCMD $SERVER_IP "cd $SERVER_HOME/;rm -rf $SERVER_NAME-bak; mv -f $SERVER_NAME $SERVER_NAME-bak;cd -"
executeCMD $SERVER_IP "cd $SERVER_HOME/;unzip -o $SERVER_NAME.zip > /dev/null; cd -"
isSuccess "unzip  ${SERVER_NAME}.zip"
if [ "$SERVER_NAME" != "linkis-gateway" ]
then
    copyFile $SERVER_IP ${workDir}/share/linkis/module/module.zip $SERVER_HOME
    isSuccess "cp module.zip"
    executeCMD $SERVER_IP  "cd $SERVER_HOME/;rm -rf modulebak;mv -f module modulebak;cd -"
    executeCMD $SERVER_IP  "cd $SERVER_HOME/;unzip -o  module.zip > /dev/null;cp -f module/lib/* $SERVER_HOME/$SERVER_NAME/lib/;cd -"
    isSuccess "unzip module.zip"
fi
echo "$SERVER_NAME-step3:subsitution conf"
SERVER_CONF_PATH=$SERVER_HOME/$SERVER_NAME/conf/application.yml
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#port:.*#port: $SERVER_PORT#g\" $SERVER_CONF_PATH"
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#defaultZone:.*#defaultZone: $EUREKA_URL#g\" $SERVER_CONF_PATH"
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#hostname:.*#hostname: $SERVER_IP#g\" $SERVER_CONF_PATH"
isSuccess "subsitution conf of $SERVER_NAME"
}
##function end

##GateWay Install
PACKAGE_DIR=springcloud/gateway
SERVER_NAME=linkis-gateway
SERVER_IP=$GATEWAY_INSTALL_IP
SERVER_PORT=$GATEWAY_PORT
SERVER_HOME=$LINKIS_INSTALL_HOME
###install dir
installPackage
###update linkis.properties
echo "$SERVER_NAME-step4:update linkis conf"
SERVER_CONF_PATH=$SERVER_HOME/$SERVER_NAME/conf/linkis.properties
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#wds.linkis.ldap.proxy.url.*#wds.linkis.ldap.proxy.url=$LDAP_URL#g\" $SERVER_CONF_PATH"
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#wds.linkis.ldap.proxy.baseDN.*#wds.linkis.ldap.proxy.baseDN=$LDAP_BASEDN#g\" $SERVER_CONF_PATH"
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#wds.linkis.gateway.admin.user.*#wds.linkis.gateway.admin.user=$deployUser#g\" $SERVER_CONF_PATH"
isSuccess "subsitution linkis.properties of $SERVER_NAME"
echo "<----------------$SERVER_NAME:end------------------->"
##GateWay Install end

##publicservice install
PACKAGE_DIR=linkis/linkis-publicservice
SERVER_NAME=linkis-publicservice
SERVER_IP=$PUBLICSERVICE_INSTALL_IP
SERVER_PORT=$PUBLICSERVICE_PORT
SERVER_HOME=$LINKIS_INSTALL_HOME
###install dir
installPackage
###update linkis.properties
echo "$SERVER_NAME-step4:update linkis conf"
SERVER_CONF_PATH=$SERVER_HOME/$SERVER_NAME/conf/linkis.properties
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#wds.linkis.server.mybatis.datasource.url.*#wds.linkis.server.mybatis.datasource.url=jdbc:mysql://${MYSQL_HOST}:${MYSQL_PORT}/${MYSQL_DB}?characterEncoding=UTF-8#g\" $SERVER_CONF_PATH"
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#wds.linkis.server.mybatis.datasource.username.*#wds.linkis.server.mybatis.datasource.username=$MYSQL_USER#g\" $SERVER_CONF_PATH"
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#wds.linkis.server.mybatis.datasource.password.*#wds.linkis.server.mybatis.datasource.password=$MYSQL_PASSWORD#g\" $SERVER_CONF_PATH"
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#wds.linkis.workspace.filesystem.localuserrootpath.*#wds.linkis.workspace.filesystem.localuserrootpath=$WORKSPACE_USER_ROOT_PATH#g\" $SERVER_CONF_PATH"
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#wds.linkis.workspace.filesystem.hdfsuserrootpath.prefix.*#wds.linkis.workspace.filesystem.hdfsuserrootpath.prefix=$HDFS_USER_ROOT_PATH#g\" $SERVER_CONF_PATH"
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#\#hadoop.config.dir.*#hadoop.config.dir=$HADOOP_CONF_DIR#g\" $SERVER_CONF_PATH"
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#wds.linkis.gateway.ip.*#wds.linkis.gateway.ip=$GATEWAY_INSTALL_IP#g\" $SERVER_CONF_PATH"
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#wds.linkis.gateway.port.*#wds.linkis.gateway.port=$GATEWAY_PORT#g\" $SERVER_CONF_PATH"
isSuccess "subsitution linkis.properties of $SERVER_NAME"
echo "<----------------$SERVER_NAME:end------------------->"
##publicservice end

##ResourceManager install
PACKAGE_DIR=linkis/rm
SERVER_NAME=linkis-resourcemanager
SERVER_IP=$RESOURCEMANAGER_INSTALL_IP
SERVER_PORT=$RESOURCEMANAGER_PORT
SERVER_HOME=$LINKIS_INSTALL_HOME
###install dir
installPackage
###update linkis.properties
echo "$SERVER_NAME-step4:update linkis conf"
SERVER_CONF_PATH=$SERVER_HOME/$SERVER_NAME/conf/linkis.properties
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#wds.linkis.server.mybatis.datasource.url.*#wds.linkis.server.mybatis.datasource.url=jdbc:mysql://${MYSQL_HOST}:${MYSQL_PORT}/${MYSQL_DB}?characterEncoding=UTF-8#g\" $SERVER_CONF_PATH"
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#wds.linkis.server.mybatis.datasource.username.*#wds.linkis.server.mybatis.datasource.username=$MYSQL_USER#g\" $SERVER_CONF_PATH"
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#wds.linkis.server.mybatis.datasource.password.*#wds.linkis.server.mybatis.datasource.password=$MYSQL_PASSWORD#g\" $SERVER_CONF_PATH"
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#\#hadoop.config.dir.*#hadoop.config.dir=$HADOOP_CONF_DIR#g\" $SERVER_CONF_PATH"
executeCMD $SERVER_IP   "rm $SERVER_HOME/$SERVER_NAME/lib/json4s-*3.5.3.jar"
echo "subsitution linkis.properties of $SERVER_NAME"
echo "<----------------$SERVER_NAME:end------------------->"
##ResourceManager install end


##PythonEM install
PACKAGE_DIR=linkis/ujes/python
SERVER_NAME=linkis-ujes-python-enginemanager
SERVER_IP=$PYTHON_INSTALL_IP
SERVER_PORT=$PYTHON_EM_PORT
SERVER_HOME=$LINKIS_INSTALL_HOME
###install dir
installPackage
###update linkis.properties
echo "$SERVER_NAME-step4:update linkis conf"
SERVER_CONF_PATH=$SERVER_HOME/$SERVER_NAME/conf/linkis.properties
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#wds.linkis.enginemanager.sudo.script.*#wds.linkis.enginemanager.sudo.script=$SERVER_HOME/$SERVER_NAME/bin/rootScript.sh#g\" $SERVER_CONF_PATH"
SERVER_ENGINE_CONF_PATH=$SERVER_HOME/$SERVER_NAME/conf/linkis-engine.properties
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#\#hadoop.config.dir.*#hadoop.config.dir=$HADOOP_CONF_DIR#g\" $SERVER_ENGINE_CONF_PATH"
isSuccess "subsitution linkis.properties of $SERVER_NAME"
echo "<----------------$SERVER_NAME:end------------------->"


##PythonEntrance install
PACKAGE_DIR=linkis/ujes/python
SERVER_NAME=linkis-ujes-python-entrance
SERVER_PORT=$PYTHON_ENTRANCE_PORT
###install dir
installPackage
###update linkis.properties
echo "$SERVER_NAME-step4:update linkis conf"
SERVER_CONF_PATH=$SERVER_HOME/$SERVER_NAME/conf/linkis.properties
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#wds.linkis.entrance.config.logPath.*#wds.linkis.entrance.config.logPath=$WORKSPACE_USER_ROOT_PATH#g\" $SERVER_CONF_PATH"
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#wds.linkis.resultSet.store.path.*#wds.linkis.resultSet.store.path=$RESULT_SET_ROOT_PATH#g\" $SERVER_CONF_PATH"
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#\#hadoop.config.dir.*#hadoop.config.dir=$HADOOP_CONF_DIR#g\" $SERVER_CONF_PATH"
isSuccess "subsitution linkis.properties of $SERVER_NAME"
echo "<----------------$SERVER_NAME:end------------------->"
##PythonEntrance install end

if [[ '1' = "$INSTALL_MODE" ]];then
	echo "Lite install end"
	exit 0
fi

##BML install
PACKAGE_DIR=linkis/linkis-bml
SERVER_NAME=linkis-bml
SERVER_IP=$BML_INSTALL_IP
SERVER_PORT=$BML_PORT
SERVER_HOME=$LINKIS_INSTALL_HOME
###install dir
installPackage
###update linkis.properties
echo "$SERVER_NAME-step4:update linkis conf"
SERVER_CONF_PATH=$SERVER_HOME/$SERVER_NAME/conf/linkis.properties
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#wds.linkis.server.mybatis.datasource.url.*#wds.linkis.server.mybatis.datasource.url=jdbc:mysql://${MYSQL_HOST}:${MYSQL_PORT}/${MYSQL_DB}?characterEncoding=UTF-8#g\" $SERVER_CONF_PATH"
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#wds.linkis.server.mybatis.datasource.username.*#wds.linkis.server.mybatis.datasource.username=$MYSQL_USER#g\" $SERVER_CONF_PATH"
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#wds.linkis.server.mybatis.datasource.password.*#wds.linkis.server.mybatis.datasource.password=$MYSQL_PASSWORD#g\" $SERVER_CONF_PATH"
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#\#hadoop.config.dir.*#hadoop.config.dir=$HADOOP_CONF_DIR#g\" $SERVER_CONF_PATH"
isSuccess "subsitution linkis.properties of $SERVERNAME"
echo "<----------------$SERVER_NAME:end------------------->"
##BML end

##linkis-metadata install
PACKAGE_DIR=linkis/linkis-metadata
SERVER_NAME=linkis-metadata
SERVER_IP=$METADATA_INSTALL_IP
SERVER_PORT=$METADATA_PORT
SERVER_HOME=$LINKIS_INSTALL_HOME
###install dir
installPackage
###update linkis.properties
echo "$SERVER_NAME-step4:update linkis conf"
SERVER_CONF_PATH=$SERVER_HOME/$SERVER_NAME/conf/linkis.properties
executeCMD $SERVER_IP  "sed -i ${txt}  \"s#wds.linkis.server.mybatis.datasource.url.*#wds.linkis.server.mybatis.datasource.url=jdbc:mysql://${MYSQL_HOST}:${MYSQL_PORT}/${MYSQL_DB}?characterEncoding=UTF-8#g\" $SERVER_CONF_PATH"
executeCMD $SERVER_IP  "sed -i ${txt}  \"s#wds.linkis.server.mybatis.datasource.username.*#wds.linkis.server.mybatis.datasource.username=$MYSQL_USER#g\" $SERVER_CONF_PATH"
executeCMD $SERVER_IP  "sed -i ${txt}  \"s#wds.linkis.server.mybatis.datasource.password.*#wds.linkis.server.mybatis.datasource.password=$MYSQL_PASSWORD#g\" $SERVER_CONF_PATH"
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#\#hive.config.dir.*#hive.config.dir=$HIVE_CONF_DIR#g\" $SERVER_CONF_PATH"
if [ "$HIVE_META_URL" != "" ]
then
  executeCMD $SERVER_IP   "sed -i ${txt}  \"s#hive.meta.url.*#hive.meta.url=$HIVE_META_URL#g\" $SERVER_CONF_PATH"
fi
if [ "$HIVE_META_USER" != "" ]
then
  executeCMD $SERVER_IP   "sed -i ${txt}  \"s#hive.meta.user.*#hive.meta.user=$HIVE_META_USER#g\" $SERVER_CONF_PATH"
fi
if [ "$HIVE_META_PASSWORD" != "" ]
then
  HIVE_META_PASSWORD=$(echo ${HIVE_META_PASSWORD//'#'/'\#'})
  executeCMD $SERVER_IP   "sed -i ${txt}  \"s#hive.meta.password.*#hive.meta.password=$HIVE_META_PASSWORD#g\" $SERVER_CONF_PATH"
fi
isSuccess "subsitution linkis.properties of $SERVER_NAME"
echo "<----------------$SERVER_NAME:end------------------->"
##metadata end

##HiveEM install
PACKAGE_DIR=linkis/ujes/hive
SERVER_NAME=linkis-ujes-hive-enginemanager
SERVER_IP=$HIVE_INSTALL_IP
SERVER_PORT=$HIVE_EM_PORT
SERVER_HOME=$LINKIS_INSTALL_HOME
###install dir
installPackage
###update linkis.properties
echo "$SERVER_NAME-step4:update linkis conf"
SERVER_CONF_PATH=$SERVER_HOME/$SERVER_NAME/conf/linkis.properties
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#wds.linkis.enginemanager.sudo.script.*#wds.linkis.enginemanager.sudo.script=$SERVER_HOME/$SERVER_NAME/bin/rootScript.sh#g\" $SERVER_CONF_PATH"
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#\#hadoop.config.dir.*#hadoop.config.dir=$HADOOP_CONF_DIR#g\" $SERVER_CONF_PATH"
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#\#hive.config.dir.*#hive.config.dir=$HIVE_CONF_DIR#g\" $SERVER_CONF_PATH"
SERVER_ENGINE_CONF_PATH=$SERVER_HOME/$SERVER_NAME/conf/linkis-engine.properties
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#\#hadoop.config.dir.*#hadoop.config.dir=$HADOOP_CONF_DIR#g\" $SERVER_ENGINE_CONF_PATH"
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#\#hive.config.dir.*#hive.config.dir=$HIVE_CONF_DIR#g\" $SERVER_ENGINE_CONF_PATH"
isSuccess "subsitution linkis.properties of $SERVER_NAME"
executeCMD $SERVER_IP   "rm $SERVER_HOME/$SERVER_NAME/lib/servlet-api-2.5.jar"
echo "<----------------$SERVER_NAME:end------------------->"
##HiveEM install end

##HiveEntrance install
PACKAGE_DIR=linkis/ujes/hive
SERVER_NAME=linkis-ujes-hive-entrance
SERVER_PORT=$HIVE_ENTRANCE_PORT
###install dir
installPackage
###update linkis.properties
echo "$SERVER_NAME-step4:update linkis conf"
SERVER_CONF_PATH=$SERVER_HOME/$SERVER_NAME/conf/linkis.properties
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#wds.linkis.entrance.config.logPath.*#wds.linkis.entrance.config.logPath=$WORKSPACE_USER_ROOT_PATH#g\" $SERVER_CONF_PATH"
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#wds.linkis.resultSet.store.path.*#wds.linkis.resultSet.store.path=$RESULT_SET_ROOT_PATH#g\" $SERVER_CONF_PATH"
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#\#hadoop.config.dir.*#hadoop.config.dir=$HADOOP_CONF_DIR#g\" $SERVER_CONF_PATH"
isSuccess "subsitution linkis.properties of $SERVER_NAME"
echo "<----------------$SERVER_NAME:end------------------->"
##HiveEntrance install end


if [[ '2' = "$INSTALL_MODE" ]];then
	echo "Simple install end"
	exit 0
fi

if [[ '3' != "$INSTALL_MODE" ]];then
	exit 0
fi

##SparkEM install
PACKAGE_DIR=linkis/ujes/spark
SERVER_NAME=linkis-ujes-spark-enginemanager
SERVER_IP=$SPARK_INSTALL_IP
SERVER_PORT=$SPARK_EM_PORT
SERVER_HOME=$LINKIS_INSTALL_HOME
###install dir
installPackage
###update linkis.properties
echo "$SERVER_NAME-step4:update linkis conf"
SERVER_CONF_PATH=$SERVER_HOME/$SERVER_NAME/conf/linkis.properties
ENGINE_CONF_PATH=$SERVER_HOME/$SERVER_NAME/conf/linkis-engine.properties
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#wds.linkis.enginemanager.sudo.script.*#wds.linkis.enginemanager.sudo.script=$SERVER_HOME/$SERVER_NAME/bin/rootScript.sh#g\" $SERVER_CONF_PATH"
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#wds.linkis.enginemanager.core.jar.*#wds.linkis.enginemanager.core.jar=$SERVER_HOME/$SERVER_NAME/lib/linkis-ujes-spark-engine-$LINKIS_VERSION.jar#g\" $SERVER_CONF_PATH"
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#wds.linkis.spark.driver.conf.mainjar.*#wds.linkis.spark.driver.conf.mainjar=$SERVER_HOME/$SERVER_NAME/conf:$SERVER_HOME/$SERVER_NAME/lib/*#g\" $SERVER_CONF_PATH"
SERVER_ENGINE_CONF_PATH=$SERVER_HOME/$SERVER_NAME/conf/linkis-engine.properties
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#\#hadoop.config.dir.*#hadoop.config.dir=$HADOOP_CONF_DIR#g\" $SERVER_ENGINE_CONF_PATH"
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#\#spark.config.dir.*#spark.config.dir=$SPARK_CONF_DIR#g\" $SERVER_ENGINE_CONF_PATH"
isSuccess "subsitution linkis.properties of $SERVER_NAME"
echo "<----------------$SERVER_NAME:end------------------->"
##SparkEM install end

##SparkEntrance install
PACKAGE_DIR=linkis/ujes/spark
SERVER_NAME=linkis-ujes-spark-entrance
SERVER_PORT=$SPARK_ENTRANCE_PORT
###install dir
installPackage
###update linkis.properties
echo "$SERVER_NAME-step4:update linkis conf"
SERVER_CONF_PATH=$SERVER_HOME/$SERVER_NAME/conf/linkis.properties
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#wds.linkis.entrance.config.logPath.*#wds.linkis.entrance.config.logPath=$WORKSPACE_USER_ROOT_PATH#g\" $SERVER_CONF_PATH"
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#wds.linkis.resultSet.store.path.*#wds.linkis.resultSet.store.path=$HDFS_USER_ROOT_PATH#g\" $SERVER_CONF_PATH"
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#\#hadoop.config.dir.*#hadoop.config.dir=$HADOOP_CONF_DIR#g\" $SERVER_CONF_PATH"
isSuccess "subsitution linkis.properties of $SERVER_NAME"
echo "<----------------$SERVER_NAME:end------------------->"
##SparkEntrance install end


##JDBCEntrance install
PACKAGE_DIR=linkis/ujes/jdbc
SERVER_NAME=linkis-ujes-jdbc-entrance
SERVER_PORT=$JDBC_ENTRANCE_PORT
###install dir
installPackage
###update linkis.properties
echo "$SERVER_NAME-step4:update linkis conf"
SERVER_CONF_PATH=$SERVER_HOME/$SERVER_NAME/conf/linkis.properties
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#wds.linkis.entrance.config.logPath.*#wds.linkis.entrance.config.logPath=$WORKSPACE_USER_ROOT_PATH#g\" $SERVER_CONF_PATH"
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#wds.linkis.resultSet.store.path.*#wds.linkis.resultSet.store.path=$HDFS_USER_ROOT_PATH#g\" $SERVER_CONF_PATH"
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#\#hadoop.config.dir.*#hadoop.config.dir=$HADOOP_CONF_DIR#g\" $SERVER_CONF_PATH"
isSuccess "subsitution linkis.properties of $SERVER_NAME"
echo "<----------------$SERVER_NAME:end------------------->"
##JDBCEntrance install end


##ShellEM install
PACKAGE_DIR=linkis/ujes/shell
SERVER_NAME=linkis-ujes-shell-enginemanager
SERVER_IP=$SHELL_INSTALL_IP
SERVER_PORT=$SHELL_EM_PORT
SERVER_HOME=$LINKIS_INSTALL_HOME
###install dir
installPackage
###update linkis.properties
echo "$SERVER_NAME-step4:update linkis conf"
SERVER_CONF_PATH=$SERVER_HOME/$SERVER_NAME/conf/linkis.properties
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#wds.linkis.enginemanager.sudo.script.*#wds.linkis.enginemanager.sudo.script=$SERVER_HOME/$SERVER_NAME/bin/rootScript.sh#g\" $SERVER_CONF_PATH"
SERVER_ENGINE_CONF_PATH=$SERVER_HOME/$SERVER_NAME/conf/linkis-engine.properties
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#\#hadoop.config.dir.*#hadoop.config.dir=$HADOOP_CONF_DIR#g\" $SERVER_ENGINE_CONF_PATH"
isSuccess "subsitution linkis.properties of $SERVER_NAME"
echo "<----------------$SERVER_NAME:end------------------->"

##SHELLEntrance install
PACKAGE_DIR=linkis/ujes/shell
SERVER_NAME=linkis-ujes-shell-entrance
SERVER_PORT=$SHELL_ENTRANCE_PORT
###install dir
installPackage
###update linkis.properties
echo "$SERVER_NAME-step4:update linkis conf"
SERVER_CONF_PATH=$SERVER_HOME/$SERVER_NAME/conf/linkis.properties
executeCMD $SERVER_IP   "sed -i ${txt}  \"s#\#hadoop.config.dir.*#hadoop.config.dir=$HADOOP_CONF_DIR#g\" $SERVER_CONF_PATH"
isSuccess "subsitution linkis.properties of $SERVER_NAME"
echo "<----------------$SERVER_NAME:end------------------->"
##SHELLEntrance install end


