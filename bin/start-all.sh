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
# 启动linkis所有的后台微服务应用
info="We will start all linkis applications, it will take some time, please wait"
echo ${info}


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
#安装dos2unix
sudo yum install dos2unix

#获取本机IP
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



#顺序启动linkis的各个微服务,如果用户没有指定微服务的部署ip，则在本地启动
#eureka
echo "Eureka Server is Starting"
EUREKA_NAME="eureka"
EUREKA_BIN=${LINKIS_INSTALL_HOME}/${EUREKA_NAME}/bin
EUREKA_START_CMD="cd ${EUREKA_BIN}; dos2unix ./*; dos2unix ../conf/*; sh start-${EUREKA_NAME}.sh > /dev/null"
if [ -n "${EUREKA_INSTALL_IP}" ];then
    ssh ${EUREKA_INSTALL_IP} "${EUREKA_START_CMD}"

else
    ssh ${local_host} "${EUREKA_START_CMD}"
fi
isSuccess "eureka started"

sleep 3

#gateway
echo "Gateway is Starting"
GATEWAY_NAME="gateway"
GATEWAY_BIN=${LINKIS_INSTALL_HOME}/${APP_PREFIX}${GATEWAY_NAME}/bin
GATEWAY_START_CMD="cd ${GATEWAY_BIN}; dos2unix ./*; dos2unix ../conf/*; sh start-${GATEWAY_NAME}.sh > /dev/null"
if [ -n "${GATEWAY_INSTALL_IP}"  ];then
    ssh ${GATEWAY_INSTALL_IP} "${GATEWAY_START_CMD}"
else
    ssh ${local_host} "${GATEWAY_START_CMD}"
fi
isSuccess "Gateway started "

sleep 3

#pub_service
echo "Public Service is Starting"
PUB_SERVICE_NAME="publicservice"
PUB_SERVICE_BIN=${LINKIS_INSTALL_HOME}/${APP_PREFIX}${PUB_SERVICE_NAME}/bin
PUB_SERVICE_START_CMD="cd ${PUB_SERVICE_BIN}; dos2unix ./*; dos2unix ../conf/*; sh start-${PUB_SERVICE_NAME}.sh > /dev/null"
if [ -n "${PUBLICSERVICE_INSTALL_IP}" ];then
    ssh ${PUBLICSERVICE_INSTALL_IP} "${PUB_SERVICE_START_CMD}"
else
    ssh ${local_host} "${PUB_SERVICE_START_CMD}"
fi
isSuccess "Public Service started "
sleep 3

#database
echo "Database is Starting"
DATABASE_NAME="database"
DATABASE_BIN=${LINKIS_INSTALL_HOME}/${APP_PREFIX}${DATABASE_NAME}/bin
DATABASE_START_CMD="cd ${DATABASE_BIN}; dos2unix ./*; dos2unix ../conf/*; sh start-${DATABASE_NAME}.sh > /dev/null"
if [ -n "${DATABASE_INSTALL_IP}" ];then
    ssh ${DATABASE_INSTALL_IP} "${DATABASE_START_CMD}"
else
    ssh ${local_host} "${DATABASE_START_CMD}"
fi
isSuccess  "Database started "
sleep 3

#Resource Manager
echo "Resource Manager is Starting"
RM_NAME="resourcemanager"
RM_BIN=${LINKIS_INSTALL_HOME}/${APP_PREFIX}${RM_NAME}/bin
RM_START_CMD="cd ${RM_BIN}; dos2unix ./*; dos2unix ../conf/*; sh start-${RM_NAME}.sh > /dev/null"
if [ -n "${RESOURCEMANAGER_INSTALL_IP}" ];then
    ssh ${RESOURCEMANAGER_INSTALL_IP} "${RM_START_CMD}"
else
    ssh ${local_host} "${RM_START_CMD}"
fi
isSuccess "Resource Manager started "


echo "sleep 10 seconds to wait RM to start"
sleep 10

#SparkEntrance
echo "Spark Entrance is Starting"
SPARK_ENTRANCE_NAME="ujes-spark-entrance"
SPARK_ENTRANCE_BIN=${LINKIS_INSTALL_HOME}/${APP_PREFIX}${SPARK_ENTRANCE_NAME}/bin
SPARK_ENTRANCE_START_CMD="cd ${SPARK_ENTRANCE_BIN}; dos2unix ./*; dos2unix ../conf/*; sh start-sparkentrance.sh > /dev/null"
if [ -n "${SPARK_INSTALL_IP}" ];then
    ssh ${SPARK_INSTALL_IP} "${SPARK_ENTRANCE_START_CMD}"
else
    ssh ${local_host} "${SPARK_ENTRANCE_START_CMD}"
fi
echo "Spark Entrance started "

#Spark Engine Manager
echo "Spark Engine Manager is Starting"
SPARK_EM_NAME="ujes-spark-enginemanager"
SPARK_EM_BIN=${LINKIS_INSTALL_HOME}/${APP_PREFIX}${SPARK_EM_NAME}/bin
SPARK_EM_START_CMD="cd ${SPARK_EM_BIN}; dos2unix ./*; dos2unix ../conf/*; sh start-sparkenginemanager.sh > /dev/null"
if [ -n "${SPARK_INSTALL_IP}" ];then
    ssh ${SPARK_INSTALL_IP} "${SPARK_EM_START_CMD}"
else
    ssh ${local_host} "${SPARK_EM_START_CMD}"
fi
echo "Spark Engine Manager started "


#HiveEntrance
echo "Hive Entrance is Starting"
HIVE_ENTRANCE_NAME="ujes-hive-entrance"
HIVE_ENTRANCE_BIN=${LINKIS_INSTALL_HOME}/${APP_PREFIX}${HIVE_ENTRANCE_NAME}/bin
HIVE_ENTRANCE_START_CMD="cd ${HIVE_ENTRANCE_BIN}; dos2unix ./*; dos2unix ../conf/*; sh start-hiveentrance.sh > /dev/null"
if [ -n "${HIVE_INSTALL_IP}" ];then
    ssh ${HIVE_INSTALL_IP} "${HIVE_ENTRANCE_START_CMD}"
else
    ssh ${local_host} "${HIVE_ENTRANCE_START_CMD}"
fi
echo "Hive Entrance started "

#Hive Engine Manager
echo "Hive Engine Manager is Starting"
HIVE_EM_NAME="ujes-hive-enginemanager"
HIVE_EM_BIN=${LINKIS_INSTALL_HOME}/${APP_PREFIX}${HIVE_EM_NAME}/bin
HIVE_EM_START_CMD="cd ${HIVE_EM_BIN}; dos2unix ./*; dos2unix ../conf/*; sh start-hiveenginemanager.sh > /dev/null"
if [ -n "${HIVE_INSTALL_IP}" ];then
    ssh ${HIVE_INSTALL_IP} "${HIVE_EM_START_CMD}"
else
    ssh ${local_host} "${HIVE_EM_START_CMD}"
fi
echo "Hive Engine Manager started "


#PythonEntrance
echo "Python Entrance is Starting"
PYTHON_ENTRANCE_NAME="ujes-python-entrance"
PYTHON_ENTRANCE_BIN=${LINKIS_INSTALL_HOME}/${APP_PREFIX}${PYTHON_ENTRANCE_NAME}/bin
PYTHON_ENTRANCE_START_CMD="cd ${PYTHON_ENTRANCE_BIN}; dos2unix ./*; dos2unix ../conf/*; sh start-pythonentrance.sh > /dev/null"
if [ -n "${PYTHON_INSTALL_IP}" ];then
    ssh ${PYTHON_INSTALL_IP} "${PYTHON_ENTRANCE_START_CMD}"
else
    ssh ${local_host} "${PYTHON_ENTRANCE_START_CMD}"
fi
echo "Python Entrance started "

#Python Engine Manager
echo "Python Engine Manager is Starting"
PYTHON_EM_NAME="ujes-python-enginemanager"
PYTHON_EM_BIN=${LINKIS_INSTALL_HOME}/${APP_PREFIX}${PYTHON_EM_NAME}/bin
PYTHON_EM_START_CMD="cd ${PYTHON_EM_BIN}; dos2unix ./*; dos2unix ../conf/*; sh start-pythonenginemanager.sh > /dev/null"
if [ -n "${PYTHON_INSTALL_IP}" ];then
    ssh ${PYTHON_INSTALL_IP} "${PYTHON_EM_START_CMD}"
else
    ssh ${local_host} "${PYTHON_EM_START_CMD}"
fi
echo "Python Engine Manager started "




#PipelineEntrance
echo "Pipeline Entrance is Starting"
PIPELINE_ENTRANCE_NAME="ujes-pipeline-entrance"
PIPELINE_ENTRANCE_BIN=${LINKIS_INSTALL_HOME}/${APP_PREFIX}${PIPELINE_ENTRANCE_NAME}/bin
PIPELINE_ENTRANCE_START_CMD="cd ${PIPELINE_ENTRANCE_BIN}; dos2unix ./*; dos2unix ../conf/*; sh start-pipelineentrance.sh > /dev/null"
if [ -n "${PIPELINE_INSTALL_IP}" ];then
    ssh ${PIPELINE_INSTALL_IP} "${PIPELINE_ENTRANCE_START_CMD}"
else
    ssh ${local_host} "${PIPELINE_ENTRANCE_START_CMD}"
fi
echo "Pipeline Entrance started "

#Pipeline Engine Manager
echo "Pipeline Engine Manager is Starting"
PIPELINE_EM_NAME="ujes-pipeline-enginemanager"
PIPELINE_EM_BIN=${LINKIS_INSTALL_HOME}/${APP_PREFIX}${PIPELINE_EM_NAME}/bin
PIPELINE_EM_START_CMD="cd ${PIPELINE_EM_BIN}; dos2unix ./*; dos2unix ../conf/*; sh start-pipelineenginemanager.sh > /dev/null"
if [ -n "${PIPELINE_INSTALL_IP}" ];then
    ssh ${PIPELINE_INSTALL_IP} "${PIPELINE_EM_START_CMD}"
else
    ssh ${local_host} "${PIPELINE_EM_START_CMD}"
fi
echo "Pipeline Engine Manager started "