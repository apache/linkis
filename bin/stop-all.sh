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

sudo yum install dos2unix > /dev/null 2>&1


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




#eureka
echo "<-------------------------------->"
echo "Begin to stop Eureka Server"
EUREKA_NAME="eureka"
EUREKA_BIN=${LINKIS_INSTALL_HOME}/${EUREKA_NAME}/bin
EUREKA_STOP_CMD="cd ${EUREKA_BIN}; dos2unix ./* > /dev/null 2>&1; dos2unix ../conf/* > /dev/null 2>&1; sh stop-${EUREKA_NAME}.sh > /dev/null"
if [ -n "${EUREKA_INSTALL_IP}" ];then
    ssh ${EUREKA_INSTALL_IP} "${EUREKA_STOP_CMD}"

else
    ssh ${local_host} "${EUREKA_STOP_CMD}"
fi
isSuccess "End to stop Eureka Server"
echo "<-------------------------------->"
sleep 3

#gateway
echo "<-------------------------------->"
echo "Begin to stop Gateway"
GATEWAY_NAME="gateway"
GATEWAY_BIN=${LINKIS_INSTALL_HOME}/${APP_PREFIX}${GATEWAY_NAME}/bin
GATEWAY_STOP_CMD="cd ${GATEWAY_BIN}; dos2unix ./* > /dev/null 2>&1; dos2unix ../conf/* > /dev/null 2>&1; sh stop-${GATEWAY_NAME}.sh > /dev/null"
if [ -n "${GATEWAY_INSTALL_IP}"  ];then
    ssh ${GATEWAY_INSTALL_IP} "${GATEWAY_STOP_CMD}"
else
    ssh ${local_host} "${GATEWAY_STOP_CMD}"
fi
isSuccess "End to stop Gateway"
echo "<-------------------------------->"
sleep 3

#pub_service
echo "<-------------------------------->"
echo "Begin to stop Public Service"
PUB_SERVICE_NAME="publicservice"
PUB_SERVICE_BIN=${LINKIS_INSTALL_HOME}/${APP_PREFIX}${PUB_SERVICE_NAME}/bin
PUB_SERVICE_STOP_CMD="cd ${PUB_SERVICE_BIN}; dos2unix ./* > /dev/null 2>&1; dos2unix ../conf/* > /dev/null 2>&1; sh stop-${PUB_SERVICE_NAME}.sh > /dev/null"
if [ -n "${PUBLICSERVICE_INSTALL_IP}" ];then
    ssh ${PUBLICSERVICE_INSTALL_IP} "${PUB_SERVICE_STOP_CMD}"
else
        ssh ${local_host} "${PUB_SERVICE_STOP_CMD}"
fi
isSuccess "End to stop Public Service"
echo "<-------------------------------->"
sleep 3

#metadata
echo "<-------------------------------->"
echo "Begin to stop Metadata."
METADATA_NAME="metadata"
METADATA_BIN=${LINKIS_INSTALL_HOME}/${APP_PREFIX}${METADATA_NAME}/bin
METADATA_STOP_CMD="if [ -d ${METADATA_BIN} ];then cd ${METADATA_BIN}; dos2unix ./* > /dev/null 2>&1; dos2unix ../conf/* > /dev/null 2>&1; sh stop-metadata.sh > /dev/null;else echo 'WARNING:Metadata did not start';fi"
if [ -n "${METADATA_INSTALL_IP}" ];then
    ssh ${METADATA_INSTALL_IP} "${METADATA_STOP_CMD}"
else
    ssh ${local_host} "${METADATA_STOP_CMD}"
fi
isSuccess  "End to stop Metadata."
echo "<-------------------------------->"
sleep 3

#Resource Manager
echo "<-------------------------------->"
echo "Begin to stop Resource Manager"
RM_NAME="resourcemanager"
RM_BIN=${LINKIS_INSTALL_HOME}/${APP_PREFIX}${RM_NAME}/bin
RM_STOP_CMD="cd ${RM_BIN}; dos2unix ./* > /dev/null 2>&1; dos2unix ../conf/* > /dev/null 2>&1; sh stop-${RM_NAME}.sh > /dev/null"
if [ -n "${RESOURCEMANAGER_INSTALL_IP}" ];then
    ssh ${RESOURCEMANAGER_INSTALL_IP} "${RM_STOP_CMD}"
else
    ssh ${local_host} "${RM_STOP_CMD}"
fi
isSuccess "End to stop Resource Manager"
echo "<-------------------------------->"

sleep 3

#SparkEntrance
echo "<-------------------------------->"
echo "Begin to stop Spark Entrance"
SPARK_ENTRANCE_NAME="ujes-spark-entrance"
SPARK_ENTRANCE_BIN=${LINKIS_INSTALL_HOME}/${APP_PREFIX}${SPARK_ENTRANCE_NAME}/bin
SPARK_ENTRANCE_STOP_CMD="if [ -d ${SPARK_ENTRANCE_BIN} ];then cd ${SPARK_ENTRANCE_BIN}; dos2unix ./* > /dev/null 2>&1; dos2unix ../conf/* > /dev/null 2>&1; sh stop-sparkentrance.sh > /dev/null;else echo 'WARNING:Spark Entrance did not start';fi"
if [ -n "${SPARK_INSTALL_IP}" ];then
    ssh ${SPARK_INSTALL_IP} "${SPARK_ENTRANCE_STOP_CMD}"
else
    ssh ${local_host} "${SPARK_ENTRANCE_STOP_CMD}"
fi
echo "End to stop Spark Entrance"
echo "<-------------------------------->"

#Spark Engine Manager
echo "<-------------------------------->"
echo "Begin to Spark Engine Manager"
SPARK_EM_NAME="ujes-spark-enginemanager"
SPARK_EM_BIN=${LINKIS_INSTALL_HOME}/${APP_PREFIX}${SPARK_EM_NAME}/bin
SPARK_EM_STOP_CMD="if [ -d ${SPARK_EM_BIN} ];then cd ${SPARK_EM_BIN}; dos2unix ./* > /dev/null 2>&1; dos2unix ../conf/* > /dev/null 2>&1; sh stop-sparkenginemanager.sh > /dev/null;else echo 'WARNING:Spark EM did not start';fi"
if [ -n "${SPARK_INSTALL_IP}" ];then
    ssh ${SPARK_INSTALL_IP} "${SPARK_EM_STOP_CMD}"
else
    ssh ${local_host} "${SPARK_EM_STOP_CMD}"
fi
echo "End to stop Spark Engine Manager "
echo "<-------------------------------->"

#HiveEntrance
echo "<-------------------------------->"
echo "Begin to stop Hive Entrance"
HIVE_ENTRANCE_NAME="ujes-hive-entrance"
HIVE_ENTRANCE_BIN=${LINKIS_INSTALL_HOME}/${APP_PREFIX}${HIVE_ENTRANCE_NAME}/bin
HIVE_ENTRANCE_STOP_CMD="if [ -d ${HIVE_ENTRANCE_BIN} ];then cd ${HIVE_ENTRANCE_BIN}; dos2unix ./* > /dev/null 2>&1; dos2unix ../conf/* > /dev/null 2>&1; sh stop-hiveentrance.sh > /dev/null;else echo 'WARNING:Hive Entrance did not start';fi"
if [ -n "${HIVE_INSTALL_IP}" ];then
    ssh ${HIVE_INSTALL_IP} "${HIVE_ENTRANCE_STOP_CMD}"
else
    ssh ${local_host} "${HIVE_ENTRANCE_STOP_CMD}"
fi
echo "End to stop Hive Entrance"
echo "<-------------------------------->"
#Hive Engine Manager
echo "<-------------------------------->"
echo "Begin to stop Hive Engine Manager"
HIVE_EM_NAME="ujes-hive-enginemanager"
HIVE_EM_BIN=${LINKIS_INSTALL_HOME}/${APP_PREFIX}${HIVE_EM_NAME}/bin
HIVE_EM_STOP_CMD="if [ -d ${HIVE_EM_BIN} ];then cd ${HIVE_EM_BIN}; dos2unix ./* > /dev/null 2>&1; dos2unix ../conf/* > /dev/null 2>&1; sh stop-hiveenginemanager.sh > /dev/null;else echo 'WARNING:Hive EM did not start';fi"
if [ -n "${HIVE_INSTALL_IP}" ];then
    ssh ${HIVE_INSTALL_IP} "${HIVE_EM_STOP_CMD}"
else
    ssh ${local_host} "${HIVE_EM_STOP_CMD}"
fi
echo "End to stop Hive Engine Manager"
echo "<-------------------------------->"

#PythonEntrance
echo "<-------------------------------->"
echo "Begin to stop Python Entrance"
PYTHON_ENTRANCE_NAME="ujes-python-entrance"
PYTHON_ENTRANCE_BIN=${LINKIS_INSTALL_HOME}/${APP_PREFIX}${PYTHON_ENTRANCE_NAME}/bin
PYTHON_ENTRANCE_STOP_CMD="if [ -d ${PYTHON_ENTRANCE_BIN} ];then cd ${PYTHON_ENTRANCE_BIN}; dos2unix ./* > /dev/null 2>&1; dos2unix ../conf/* > /dev/null 2>&1; sh stop-pythonentrance.sh > /dev/null;else echo 'WARNING:Python Entrance did not start';fi"
if [ -n "${PYTHON_INSTALL_IP}" ];then
    ssh ${PYTHON_INSTALL_IP} "${PYTHON_ENTRANCE_STOP_CMD}"
else
    ssh ${local_host} "${PYTHON_ENTRANCE_STOP_CMD}"
fi
echo "End to stop Python Entrance"
echo "<-------------------------------->"
#Python Engine Manager
echo "<-------------------------------->"
echo "Begin to stop Python Engine Manager"
PYTHON_EM_NAME="ujes-python-enginemanager"
PYTHON_EM_BIN=${LINKIS_INSTALL_HOME}/${APP_PREFIX}${PYTHON_EM_NAME}/bin
PYTHON_EM_STOP_CMD="if [ -d ${PYTHON_EM_BIN} ];then cd ${PYTHON_EM_BIN}; dos2unix ./* > /dev/null 2>&1; dos2unix ../conf/* > /dev/null 2>&1; sh stop-pythonenginemanager.sh > /dev/null;else echo 'WARNING:Python EM did not start';fi"
if [ -n "${PYTHON_INSTALL_IP}" ];then
    ssh ${PYTHON_INSTALL_IP} "${PYTHON_EM_STOP_CMD}"
else
    ssh ${local_host} "${PYTHON_EM_STOP_CMD}"
fi
echo "End to stop Python Engine Manager"
echo "<-------------------------------->"



#JDBCEntrance
echo "<-------------------------------->"
echo "Begin to stop JDBC Entrance"
JDBC_ENTRANCE_NAME="ujes-jdbc-entrance"
JDBC_ENTRANCE_BIN=${LINKIS_INSTALL_HOME}/${APP_PREFIX}${JDBC_ENTRANCE_NAME}/bin
JDBC_ENTRANCE_STOP_CMD="if [ -d ${JDBC_ENTRANCE_BIN} ];then cd ${JDBC_ENTRANCE_BIN}; dos2unix ./* > /dev/null 2>&1; dos2unix ../conf/* > /dev/null 2>&1; sh stop-jdbcentrance.sh > /dev/null;else echo 'WARNING:JDBC Entrance will not start';fi"
if [ -n "${JDBC_INSTALL_IP}" ];then
    ssh ${JDBC_INSTALL_IP} "${JDBC_ENTRANCE_STOP_CMD}"
else
    ssh ${local_host} "${JDBC_ENTRANCE_STOP_CMD}"
fi
echo "End to stop JDBC Entrance"
echo "<-------------------------------->"

sleep 3

#MLSQLEntrance
echo "<-------------------------------->"
echo "Begin to stop MLSQL Entrance"
MLSQL_ENTRANCE_NAME="ujes-mlsql-entrance"
MLSQL_ENTRANCE_BIN=${LINKIS_INSTALL_HOME}/${APP_PREFIX}${MLSQL_ENTRANCE_NAME}/bin
MLSQL_ENTRANCE_START_CMD="if [ -d ${MLSQL_ENTRANCE_BIN} ];then cd ${MLSQL_ENTRANCE_BIN}; dos2unix ./* > /dev/null 2>&1; dos2unix ../conf/* > /dev/null 2>&1; sh stop-mlsqlentrance.sh > /dev/null;else echo 'WARNING:MLSQL Entrance will not start';fi"
if [ -n "${MLSQL_INSTALL_IP}" ];then
    ssh ${MLSQL_INSTALL_IP} "${MLSQL_ENTRANCE_START_CMD}"
else
    ssh ${local_host} "${MLSQL_ENTRANCE_START_CMD}"
fi
echo "End to stop MLSQL Entrance"
echo "<-------------------------------->"

sleep 3

##PipelineEntrance
#echo "Pipeline Entrance is Stoping"
#PIPELINE_ENTRANCE_NAME="ujes-pipeline-entrance"
#PIPELINE_ENTRANCE_BIN=${LINKIS_INSTALL_HOME}/${APP_PREFIX}${PIPELINE_ENTRANCE_NAME}/bin
#PIPELINE_ENTRANCE_STOP_CMD="cd ${PIPELINE_ENTRANCE_BIN}; dos2unix ./* > /dev/null 2>&1; dos2unix ../conf/* > /dev/null 2>&1; sh stop-pipelineentrance.sh > /dev/null"
#if [ -n "${PIPELINE_INSTALL_IP}" ];then
#    ssh ${PIPELINE_INSTALL_IP} "${PIPELINE_ENTRANCE_STOP_CMD}"
#else
#    ssh ${local_host} "${PIPELINE_ENTRANCE_STOP_CMD}"
#fi
#echo "Pipeline Entrance stoped "
#
##Pipeline Engine Manager
#echo "Pipeline Engine Manager is Stoping"
#PIPELINE_EM_NAME="ujes-pipeline-enginemanager"
#PIPELINE_EM_BIN=${LINKIS_INSTALL_HOME}/${APP_PREFIX}${PIPELINE_EM_NAME}/bin
#PIPELINE_EM_STOP_CMD="cd ${PIPELINE_EM_BIN}; dos2unix ./* > /dev/null 2>&1; dos2unix ../conf/* > /dev/null 2>&1; sh stop-pipelineenginemanager.sh > /dev/null"
#if [ -n "${PIPELINE_INSTALL_IP}" ];then
#    ssh ${PIPELINE_INSTALL_IP} "${PIPELINE_EM_STOP_CMD}"
#else
#    ssh ${local_host} "${PIPELINE_EM_STOP_CMD}"
#fi
#echo "Pipeline Engine Manager stoped "