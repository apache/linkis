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
# 启动linkis所有的后台微服务应用
info="We will stop all linkis applications, it will take some time, please wait"
echo ${info}


workDir=`dirname "${BASH_SOURCE-$0}"`
workDir=`cd "$workDir"; pwd`


CONF_DIR="${workDir}"/../conf
CONF_FILE=${CONF_DIR}/config.sh

#安装dos2unix
sudo yum install dos2unix

#获取本机IP
local_host="`hostname --fqdn`"

#if there is no LINKIS_INSTALL_HOME，we need to source config again
if [ -z ${LINKIS_INSTALL_HOME} ];then
    echo "Warning: LINKIS_INSTALL_HOME does not exist, we will source config"
    if [ ! -f "${CONF_FILE}" ];then
        echo "Error: can not find config file, stop applications failed"
        exit 1
    else
        source ${CONF_FILE}
    fi
fi

APP_PREFIX="linkis-"



#顺序启动linkis的各个微服务,如果用户没有指定微服务的部署ip，则在本地启动
#eureka
echo "Eureka Server is Stoping"
EUREKA_NAME="eureka"
EUREKA_BIN=${LINKIS_INSTALL_HOME}/${EUREKA_NAME}/bin
EUREKA_STOP_CMD="cd ${EUREKA_BIN}; sh stop-${EUREKA_NAME}.sh > /dev/null"
if [ -n "${EUREKA_INSTALL_IP}" ];then
    ssh ${EUREKA_INSTALL_IP} "${EUREKA_STOP_CMD}"
else
    ssh ${local_host} "${EUREKA_STOP_CMD}"
fi
echo "Eureka Server stoped "


#gateway
echo "Gateway is Stoping"
GATEWAY_NAME="gateway"
GATEWAY_BIN=${LINKIS_INSTALL_HOME}/${APP_PREFIX}${GATEWAY_NAME}/bin
GATEWAY_STOP_CMD="cd ${GATEWAY_BIN}; sh stop-${GATEWAY_NAME}.sh > /dev/null"
if [ -n "${GATEWAY_INSTALL_IP}"  ];then
    ssh ${GATEWAY_INSTALL_IP} "${GATEWAY_STOP_CMD}"
else
    ssh ${local_host} "${GATEWAY_STOP_CMD}"
fi
echo "Gateway stoped "

#pub_service
echo "Public Service is Stoping"
PUB_SERVICE_NAME="publicservice"
PUB_SERVICE_BIN=${LINKIS_INSTALL_HOME}/${APP_PREFIX}${PUB_SERVICE_NAME}/bin
PUB_SERVICE_STOP_CMD="cd ${PUB_SERVICE_BIN}; sh stop-${PUB_SERVICE_NAME}.sh > /dev/null"
if [ -n "${PUBLICSERVICE_INSTALL_IP}" ];then
    ssh ${PUBLICSERVICE_INSTALL_IP} "${PUB_SERVICE_STOP_CMD}"
else
    ssh ${local_host} "${PUB_SERVICE_STOP_CMD}"
fi
echo "Public Service stoped "


#database
echo "Database is Stoping"
DATABASE_NAME="database"
DATABASE_BIN=${LINKIS_INSTALL_HOME}/${APP_PREFIX}${DATABASE_NAME}/bin
DATABASE_STOP_CMD="cd ${DATABASE_BIN}; sh stop-${DATABASE_NAME}.sh > /dev/null"
if [ -n "${DATABASE_INSTALL_IP}" ];then
    ssh ${DATABASE_INSTALL_IP} "${DATABASE_STOP_CMD}"
else
    ssh ${local_host} "${DATABASE_STOP_CMD}"
fi
echo "database stoped "


#Resource Manager
echo "Resource Manager is Stoping"
RM_NAME="resourcemanager"
RM_BIN=${LINKIS_INSTALL_HOME}/${APP_PREFIX}${RM_NAME}/bin
RM_STOP_CMD="cd ${RM_BIN}; sh stop-${RM_NAME}.sh > /dev/null"
if [ -n "${RESOURCEMANAGER_INSTALL_IP}" ];then
    ssh ${RESOURCEMANAGER_INSTALL_IP} "${RM_STOP_CMD}"
else
    ssh ${local_host} "${RM_STOP_CMD}"
fi
echo "Resource Manager stoped "

#SparkEntrance
echo "Spark Entrance is Stoping"
SPARK_ENTRANCE_NAME="ujes-spark-entrance"
SPARK_ENTRANCE_BIN=${LINKIS_INSTALL_HOME}/${APP_PREFIX}${SPARK_ENTRANCE_NAME}/bin
SPARK_ENTRANCE_STOP_CMD="cd ${SPARK_ENTRANCE_BIN}; sh stop-sparkentrance.sh > /dev/null"
if [ -n "${SPARK_INSTALL_IP}" ];then
    ssh ${SPARK_INSTALL_IP} "${SPARK_ENTRANCE_STOP_CMD}"
else
    ssh ${local_host} "${SPARK_ENTRANCE_STOP_CMD}"
fi
echo "Spark Entrance stoped "

#Spark Engine Manager
echo "Spark Engine Manager is Stoping"
SPARK_EM_NAME="ujes-spark-enginemanager"
SPARK_EM_BIN=${LINKIS_INSTALL_HOME}/${APP_PREFIX}${SPARK_EM_NAME}/bin
SPARK_EM_STOP_CMD="cd ${SPARK_EM_BIN}; sh stop-sparkenginemanager.sh > /dev/null"
if [ -n "${SPARK_INSTALL_IP}" ];then
    ssh ${SPARK_INSTALL_IP} "${SPARK_EM_STOP_CMD}"
else
    ssh ${local_host} "${SPARK_EM_STOP_CMD}"
fi
echo "Spark Engine Manager stoped "


#HiveEntrance
echo "Hive Entrance is Stoping"
HIVE_ENTRANCE_NAME="ujes-hive-entrance"
HIVE_ENTRANCE_BIN=${LINKIS_INSTALL_HOME}/${APP_PREFIX}${HIVE_ENTRANCE_NAME}/bin
HIVE_ENTRANCE_STOP_CMD="cd ${HIVE_ENTRANCE_BIN}; sh stop-hiveentrance.sh > /dev/null"
if [ -n "${HIVE_INSTALL_IP}" ];then
    ssh ${HIVE_INSTALL_IP} "${HIVE_ENTRANCE_STOP_CMD}"
else
    ssh ${local_host} "${HIVE_ENTRANCE_STOP_CMD}"
fi
echo "Hive Entrance stoped "

#Hive Engine Manager
echo "Hive Engine Manager is Stoping"
HIVE_EM_NAME="ujes-hive-enginemanager"
HIVE_EM_BIN=${LINKIS_INSTALL_HOME}/${APP_PREFIX}${HIVE_EM_NAME}/bin
HIVE_EM_STOP_CMD="cd ${HIVE_EM_BIN}; sh stop-hiveenginemanager.sh > /dev/null"
if [ -n "${HIVE_INSTALL_IP}" ];then
    ssh ${HIVE_INSTALL_IP} "${HIVE_EM_STOP_CMD}"
else
    ssh ${local_host} "${HIVE_EM_STOP_CMD}"
fi
echo "Hive Engine Manager stoped "


#PythonEntrance
echo "Python Entrance is Stoping"
PYTHON_ENTRANCE_NAME="ujes-python-entrance"
PYTHON_ENTRANCE_BIN=${LINKIS_INSTALL_HOME}/${APP_PREFIX}${PYTHON_ENTRANCE_NAME}/bin
PYTHON_ENTRANCE_STOP_CMD="cd ${PYTHON_ENTRANCE_BIN}; sh stop-pythonentrance.sh > /dev/null"
if [ -n "${PYTHON_INSTALL_IP}" ];then
    ssh ${PYTHON_INSTALL_IP} "${PYTHON_ENTRANCE_STOP_CMD}"
else
    ssh ${local_host} "${PYTHON_ENTRANCE_STOP_CMD}"
fi
echo "Python Entrance stoped "

#Python Engine Manager
echo "Python Engine Manager is Stoping"
PYTHON_EM_NAME="ujes-python-enginemanager"
PYTHON_EM_BIN=${LINKIS_INSTALL_HOME}/${APP_PREFIX}${PYTHON_EM_NAME}/bin
PYTHON_EM_STOP_CMD="cd ${PYTHON_EM_BIN}; sh stop-pythonenginemanager.sh > /dev/null"
if [ -n "${PYTHON_INSTALL_IP}" ];then
    ssh ${PYTHON_INSTALL_IP} "${PYTHON_EM_STOP_CMD}"
else
    ssh ${local_host} "${PYTHON_EM_STOP_CMD}"
fi
echo "Python Engine Manager stoped "




#PipelineEntrance
echo "Pipeline Entrance is Stoping"
PIPELINE_ENTRANCE_NAME="ujes-pipeline-entrance"
PIPELINE_ENTRANCE_BIN=${LINKIS_INSTALL_HOME}/${APP_PREFIX}${PIPELINE_ENTRANCE_NAME}/bin
PIPELINE_ENTRANCE_STOP_CMD="cd ${PIPELINE_ENTRANCE_BIN}; sh stop-pipelineentrance.sh > /dev/null"
if [ -n "${PIPELINE_INSTALL_IP}" ];then
    ssh ${PIPELINE_INSTALL_IP} "${PIPELINE_ENTRANCE_STOP_CMD}"
else
    ssh ${local_host} "${PIPELINE_ENTRANCE_STOP_CMD}"
fi
echo "Pipeline Entrance stoped "

#Pipeline Engine Manager
echo "Pipeline Engine Manager is Stoping"
PIPELINE_EM_NAME="ujes-pipeline-enginemanager"
PIPELINE_EM_BIN=${LINKIS_INSTALL_HOME}/${APP_PREFIX}${PIPELINE_EM_NAME}/bin
PIPELINE_EM_STOP_CMD="cd ${PIPELINE_EM_BIN}; sh stop-pipelineenginemanager.sh > /dev/null"
if [ -n "${PIPELINE_INSTALL_IP}" ];then
    ssh ${PIPELINE_INSTALL_IP} "${PIPELINE_EM_STOP_CMD}"
else
    ssh ${local_host} "${PIPELINE_EM_STOP_CMD}"
fi
echo "Pipeline Engine Manager stoped "