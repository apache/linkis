#!/bin/bash
#
# description:  Starts and stops Server
#
# @name:        linkis-demo

# @created:     01.16.2021
#
# Modified for Linkis 1.0.0

# SSH_PORT=22

### deploy user
deployUser=hadoop

##Linkis_SERVER_VERSION
LINKIS_SERVER_VERSION=v1

### Specifies the user workspace, which is used to store the user's script files and log files.
### Generally local directory
WORKSPACE_USER_ROOT_PATH=file:///tmp/linkis/ ##file:// required
### User's root hdfs path
HDFS_USER_ROOT_PATH=hdfs:///tmp/linkis ##hdfs:// required

### Path to store job ResultSet:file or hdfs path
RESULT_SET_ROOT_PATH=hdfs:///tmp/linkis ##hdfs:// required

### Path to store started engines and engine logs, must be local
ENGINECONN_ROOT_PATH=/appcom/tmp   ## file://  required

ENTRANCE_CONFIG_LOG_PATH=hdfs:///tmp/linkis/ ##file:// required

### Provide the DB information of Hive metadata database.
HIVE_META_URL=
HIVE_META_USER=
HIVE_META_PASSWORD=

##YARN REST URL  spark engine required
YARN_RESTFUL_URL=http://127.0.0.1:8088

###HADOOP CONF DIR
HADOOP_CONF_DIR=/appcom/config/hadoop-config

###HIVE CONF DIR
HIVE_CONF_DIR=/appcom/config/hive-config

###SPARK CONF DIR
SPARK_CONF_DIR=/appcom/config/spark-config

## Engine version conf
#SPARK_VERSION
#SPARK_VERSION=2.4.3
##HIVE_VERSION
#HIVE_VERSION=1.2.1
#PYTHON_VERSION=python2

################### The install Configuration of all Micro-Services #####################
#
#    NOTICE:
#       1. If you just wanna try, the following micro-service configuration can be set without any settings.
#            These services will be installed by default on this machine.
#       2. In order to get the most complete enterprise-level features, we strongly recommend that you install
#            Linkis in a distributed manner and set the following microservice parameters
#

###  EUREKA install information
###  You can access it in your browser at the address below:http://${EUREKA_INSTALL_IP}:${EUREKA_PORT}
#EUREKA_INSTALL_IP=127.0.0.1         # Microservices Service Registration Discovery Center
EUREKA_PORT=20303
EUREKA_PREFER_IP=false

###  Gateway install information
#GATEWAY_INSTALL_IP=127.0.0.1
GATEWAY_PORT=9001

### ApplicationManager
#MANAGER_INSTALL_IP=127.0.0.1
MANAGER_PORT=9101

### EngineManager
#ENGINECONNMANAGER_INSTALL_IP=127.0.0.1
ENGINECONNMANAGER_PORT=9102



### EnginePluginServer
#ENGINECONN_PLUGIN_SERVER_INSTALL_IP=127.0.0.1
ENGINECONN_PLUGIN_SERVER_PORT=9103

### LinkisEntrance
#ENTRANCE_INSTALL_IP=127.0.0.1
ENTRANCE_PORT=9104

###  publicservice
#PUBLICSERVICE_INSTALL_IP=127.0.0.1
PUBLICSERVICE_PORT=9105


### Hive Metadata Query service, provide the metadata information of Hive databases.
#DATASOURCE_INSTALL_IP=127.0.0.1
DATASOURCE_PORT=9106

### BML
### This service is used to provide BML capability.
#BML_INSTALL_IP=127.0.0.1
BML_PORT=9107

### cs
#CS_INSTALL_IP=127.0.0.1
CS_PORT=9108

########################################################################################

## LDAP is for enterprise authorization, if you just want to have a try, ignore it.
#LDAP_URL=ldap://localhost:1389/
#LDAP_BASEDN=dc=webank,dc=com
#LDAP_USER_NAME_FORMAT=cn=%s@xxx.com,OU=xxx,DC=xxx,DC=com

## java application default jvm memory
export SERVER_HEAP_SIZE="512M"

if test -z "$EUREKA_INSTALL_IP"
then
  export EUREKA_INSTALL_IP="`hostname --fqdn`"
fi
if [ "true" != "$EUREKA_PREFER_IP" ]
then
  export EUREKA_HOSTNAME=$EUREKA_INSTALL_IP
fi
export EUREKA_URL=http://$EUREKA_INSTALL_IP:$EUREKA_PORT/eureka/

LINKIS_VERSION=1.0.0

# for install
LINKIS_PUBLIC_MODULE=lib/linkis-commons/public-module