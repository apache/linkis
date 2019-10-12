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

sudo yum -y install dos2unix


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


#eureka
echo "<-------------------------------->"
echo "Begin to start Eureka Server"
EUREKA_NAME="eureka"
EUREKA_BIN=${LINKIS_INSTALL_HOME}/${EUREKA_NAME}/bin
EUREKA_START_CMD="cd ${EUREKA_BIN}; dos2unix ./* > /dev/null 2>&1; dos2unix ../conf/* > /dev/null 2>&1; sh start-${EUREKA_NAME}.sh"
if [ -n "${EUREKA_INSTALL_IP}" ];then
    ssh ${EUREKA_INSTALL_IP} "${EUREKA_START_CMD}"

else
    ssh ${local_host} "${EUREKA_START_CMD}"
fi
isSuccess "End to start Eureka Server"
echo "<-------------------------------->"
sleep 3

#gateway
echo "<-------------------------------->"
echo "Begin to start Gateway"
GATEWAY_NAME="gateway"
GATEWAY_BIN=${LINKIS_INSTALL_HOME}/${APP_PREFIX}${GATEWAY_NAME}/bin
GATEWAY_START_CMD="cd ${GATEWAY_BIN}; dos2unix ./* > /dev/null 2>&1; dos2unix ../conf/* > /dev/null 2>&1; sh start-${GATEWAY_NAME}.sh"
if [ -n "${GATEWAY_INSTALL_IP}"  ];then
    ssh ${GATEWAY_INSTALL_IP} "${GATEWAY_START_CMD}"
else
    ssh ${local_host} "${GATEWAY_START_CMD}"
fi
isSuccess "End to start Gateway"
echo "<-------------------------------->"
sleep 3

#pub_service
echo "<-------------------------------->"
echo "Begin to start Public Service"
PUB_SERVICE_NAME="publicservice"
PUB_SERVICE_BIN=${LINKIS_INSTALL_HOME}/${APP_PREFIX}${PUB_SERVICE_NAME}/bin
PUB_SERVICE_START_CMD="cd ${PUB_SERVICE_BIN}; dos2unix ./* > /dev/null 2>&1; dos2unix ../conf/* > /dev/null 2>&1; sh start-${PUB_SERVICE_NAME}.sh"
if [ -n "${PUBLICSERVICE_INSTALL_IP}" ];then
    ssh ${PUBLICSERVICE_INSTALL_IP} "${PUB_SERVICE_START_CMD}"
else
        ssh ${local_host} "${PUB_SERVICE_START_CMD}"
fi
isSuccess "End to start Public Service"
echo "<-------------------------------->"
sleep 3

#metadata
echo "<-------------------------------->"
echo "Begin to start metadata"
METADATA_NAME="metadata"
METADATA_BIN=${LINKIS_INSTALL_HOME}/${APP_PREFIX}${METADATA_NAME}/bin
METADATA_START_CMD="if [ -d ${METADATA_BIN} ];then cd ${METADATA_BIN}; dos2unix ./* > /dev/null 2>&1; dos2unix ../conf/* > /dev/null 2>&1; sh start-metadata.sh;else echo 'WARNING:Metadata will not start';fi"
if [ -n "${METADATA_INSTALL_IP}" ];then
    ssh ${METADATA_INSTALL_IP} "${METADATA_START_CMD}"
else
    ssh ${local_host} "${METADATA_START_CMD}"
fi
isSuccess  "End to start Metadata"
echo "<-------------------------------->"
sleep 3

#Resource Manager
echo "<-------------------------------->"
echo "Begin to start Resource Manager"
RM_NAME="resourcemanager"
RM_BIN=${LINKIS_INSTALL_HOME}/${APP_PREFIX}${RM_NAME}/bin
RM_START_CMD="cd ${RM_BIN}; dos2unix ./* > /dev/null 2>&1; dos2unix ../conf/* > /dev/null 2>&1; sh start-${RM_NAME}.sh"
if [ -n "${RESOURCEMANAGER_INSTALL_IP}" ];then
    ssh ${RESOURCEMANAGER_INSTALL_IP} "${RM_START_CMD}"
else
    ssh ${local_host} "${RM_START_CMD}"
fi
isSuccess "End to start Resource Manager"
echo "<-------------------------------->"

echo "sleep 15 seconds to wait RM to be ready"
sleep 15

#SparkEntrance
echo "<-------------------------------->"
echo "Begin to start Spark Entrance"
SPARK_ENTRANCE_NAME="ujes-spark-entrance"
SPARK_ENTRANCE_BIN=${LINKIS_INSTALL_HOME}/${APP_PREFIX}${SPARK_ENTRANCE_NAME}/bin
SPARK_ENTRANCE_START_CMD="if [ -d ${SPARK_ENTRANCE_BIN} ];then cd ${SPARK_ENTRANCE_BIN}; dos2unix ./* > /dev/null 2>&1; dos2unix ../conf/* > /dev/null 2>&1; sh start-sparkentrance.sh;else echo 'WARNING:Spark Entrance will not start';fi"
if [ -n "${SPARK_INSTALL_IP}" ];then
    ssh ${SPARK_INSTALL_IP} "${SPARK_ENTRANCE_START_CMD}"
else
    ssh ${local_host} "${SPARK_ENTRANCE_START_CMD}"
fi
echo "End to end Spark Entrance started"
echo "<-------------------------------->"
sleep 3
#Spark Engine Manager
echo "<-------------------------------->"
echo "Begin to Spark Engine Manager"
SPARK_EM_NAME="ujes-spark-enginemanager"
SPARK_EM_BIN=${LINKIS_INSTALL_HOME}/${APP_PREFIX}${SPARK_EM_NAME}/bin
SPARK_EM_START_CMD="if [ -d ${SPARK_EM_BIN} ];then cd ${SPARK_EM_BIN}; dos2unix ./* > /dev/null 2>&1; dos2unix ../conf/* > /dev/null 2>&1; sh start-sparkenginemanager.sh;else echo 'WARNING:Spark EM will not start';fi"
if [ -n "${SPARK_INSTALL_IP}" ];then
    ssh ${SPARK_INSTALL_IP} "${SPARK_EM_START_CMD}"
else
    ssh ${local_host} "${SPARK_EM_START_CMD}"
fi
echo "End to start Spark Engine Manager "
echo "<-------------------------------->"
sleep 3
#HiveEntrance
echo "<-------------------------------->"
echo "Begin to start Hive Entrance"
HIVE_ENTRANCE_NAME="ujes-hive-entrance"
HIVE_ENTRANCE_BIN=${LINKIS_INSTALL_HOME}/${APP_PREFIX}${HIVE_ENTRANCE_NAME}/bin
HIVE_ENTRANCE_START_CMD="if [ -d ${HIVE_ENTRANCE_BIN} ];then cd ${HIVE_ENTRANCE_BIN}; dos2unix ./* > /dev/null 2>&1; dos2unix ../conf/* > /dev/null 2>&1; sh start-hiveentrance.sh;else echo 'WARNING:Hive Entrance will not start';fi"
if [ -n "${HIVE_INSTALL_IP}" ];then
    ssh ${HIVE_INSTALL_IP} "${HIVE_ENTRANCE_START_CMD}"
else
    ssh ${local_host} "${HIVE_ENTRANCE_START_CMD}"
fi
echo "End to start Hive Entrance"
echo "<-------------------------------->"

sleep 3


#Hive Engine Manager
echo "<-------------------------------->"
echo "Begin to start Hive Engine Manager"
HIVE_EM_NAME="ujes-hive-enginemanager"
HIVE_EM_BIN=${LINKIS_INSTALL_HOME}/${APP_PREFIX}${HIVE_EM_NAME}/bin
HIVE_EM_START_CMD="if [ -d ${HIVE_EM_BIN} ];then cd ${HIVE_EM_BIN}; dos2unix ./* > /dev/null 2>&1; dos2unix ../conf/* > /dev/null 2>&1; sh start-hiveenginemanager.sh > /dev/null;else echo 'WARNING:Hive EM will not start';fi"
if [ -n "${HIVE_INSTALL_IP}" ];then
    ssh ${HIVE_INSTALL_IP} "${HIVE_EM_START_CMD}"
else
    ssh ${local_host} "${HIVE_EM_START_CMD}"
fi
echo "End to start Hive Engine Manager"
echo "<-------------------------------->"

sleep 3

#PythonEntrance
echo "<-------------------------------->"
echo "Begin to start Python Entrance"
PYTHON_ENTRANCE_NAME="ujes-python-entrance"
PYTHON_ENTRANCE_BIN=${LINKIS_INSTALL_HOME}/${APP_PREFIX}${PYTHON_ENTRANCE_NAME}/bin
PYTHON_ENTRANCE_START_CMD="if [ -d ${PYTHON_ENTRANCE_BIN} ];then cd ${PYTHON_ENTRANCE_BIN}; dos2unix ./* > /dev/null 2>&1; dos2unix ../conf/* > /dev/null 2>&1; sh start-pythonentrance.sh;else echo 'WARNING:Python Entrance will not start';fi"
if [ -n "${PYTHON_INSTALL_IP}" ];then
    ssh ${PYTHON_INSTALL_IP} "${PYTHON_ENTRANCE_START_CMD}"
else
    ssh ${local_host} "${PYTHON_ENTRANCE_START_CMD}"
fi
echo "End to start Python Entrance"
echo "<-------------------------------->"

sleep 3

#Python Engine Manager
echo "<-------------------------------->"
echo "Begin to start Python Engine Manager"
PYTHON_EM_NAME="ujes-python-enginemanager"
PYTHON_EM_BIN=${LINKIS_INSTALL_HOME}/${APP_PREFIX}${PYTHON_EM_NAME}/bin
PYTHON_EM_START_CMD="if [ -d ${PYTHON_EM_BIN} ];then cd ${PYTHON_EM_BIN}; dos2unix ./* > /dev/null 2>&1; dos2unix ../conf/* > /dev/null 2>&1; sh start-pythonenginemanager.sh;else echo 'WARNING:Python EM will not start';fi"
if [ -n "${PYTHON_INSTALL_IP}" ];then
    ssh ${PYTHON_INSTALL_IP} "${PYTHON_EM_START_CMD}"
else
    ssh ${local_host} "${PYTHON_EM_START_CMD}"
fi
echo "End to start Python Engine Manager"
echo "<-------------------------------->"

sleep 3



#JDBCEntrance
echo "<-------------------------------->"
echo "Begin to start JDBC Entrance"
JDBC_ENTRANCE_NAME="ujes-jdbc-entrance"
JDBC_ENTRANCE_BIN=${LINKIS_INSTALL_HOME}/${APP_PREFIX}${JDBC_ENTRANCE_NAME}/bin
JDBC_ENTRANCE_START_CMD="if [ -d ${JDBC_ENTRANCE_BIN} ];then cd ${JDBC_ENTRANCE_BIN}; dos2unix ./* > /dev/null 2>&1; dos2unix ../conf/* > /dev/null 2>&1; sh start-jdbcentrance.sh;else echo 'WARNING:JDBC Entrance will not start';fi"
if [ -n "${JDBC_INSTALL_IP}" ];then
    ssh ${JDBC_INSTALL_IP} "${JDBC_ENTRANCE_START_CMD}"
else
    ssh ${local_host} "${JDBC_ENTRANCE_START_CMD}"
fi
echo "End to start JDBC Entrance"
echo "<-------------------------------->"

sleep 3



##PipelineEntrance
#echo "Pipeline Entrance is Starting"
#PIPELINE_ENTRANCE_NAME="ujes-pipeline-entrance"
#PIPELINE_ENTRANCE_BIN=${LINKIS_INSTALL_HOME}/${APP_PREFIX}${PIPELINE_ENTRANCE_NAME}/bin
#PIPELINE_ENTRANCE_START_CMD="cd ${PIPELINE_ENTRANCE_BIN}; dos2unix ./* > /dev/null 2>&1; dos2unix ../conf/* > /dev/null 2>&1; sh start-pipelineentrance.sh"
#if [ -n "${PIPELINE_INSTALL_IP}" ];then
#    ssh ${PIPELINE_INSTALL_IP} "${PIPELINE_ENTRANCE_START_CMD}"
#else
#    ssh ${local_host} "${PIPELINE_ENTRANCE_START_CMD}"
#fi
#echo "Pipeline Entrance started "
#
##Pipeline Engine Manager
#echo "Pipeline Engine Manager is Starting"
#PIPELINE_EM_NAME="ujes-pipeline-enginemanager"
#PIPELINE_EM_BIN=${LINKIS_INSTALL_HOME}/${APP_PREFIX}${PIPELINE_EM_NAME}/bin
#PIPELINE_EM_START_CMD="cd ${PIPELINE_EM_BIN}; dos2unix ./* > /dev/null 2>&1; dos2unix ../conf/* > /dev/null 2>&1; sh start-pipelineenginemanager.sh"
#if [ -n "${PIPELINE_INSTALL_IP}" ];then
#    ssh ${PIPELINE_INSTALL_IP} "${PIPELINE_EM_START_CMD}"
#else
#    ssh ${local_host} "${PIPELINE_EM_START_CMD}"
#fi
#echo "Pipeline Engine Manager started "


echo "start-all shell script executed completely"