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

#
# description:  Starts and stops Server
#
# @name:        linkis-env
#
# Modified for Linkis 1.0.0
SSH_PORT=60002

### deploy user
deployUser=root
##If you don't set it, a random password string will be generated during installation
deployPwd=


##Linkis_SERVER_VERSION
LINKIS_SERVER_VERSION=v1

### Specifies the user workspace, which is used to store the user's script files and log files.
### Generally local directory, path mode can be [file://] or [hdfs://]
WORKSPACE_USER_ROOT_PATH=file:///opt/linkis/workspace
### User's root hdfs path, path mode can be [file://] or [hdfs://]
HDFS_USER_ROOT_PATH=hdfs:///linkis



### Path to store started engines and engine logs, must be local
ENGINECONN_ROOT_PATH=/opt/engineconn
###path mode can be [file://] or [hdfs://]
ENTRANCE_CONFIG_LOG_PATH=hdfs:///linkis/

### Path to store job ResultSet
### path mode can be [file://] or [hdfs://]
RESULT_SET_ROOT_PATH=hdfs:///linkis/tmp/resultSet

##YARN REST URL  spark engine required
# Active resourcemanager address needed. Recommended to add all ha addresses. eg YARN_RESTFUL_URL="http://127.0.0.1:8088;http://127.0.0.2:8088"
YARN_RESTFUL_URL="http://bigd-cdh-a-dev-03:8088/"

## request Yarn resource restful interface When Yarn need auth by user
## If your environment yarn interface can be accessed directly, ignore it
#YARN_AUTH_ENABLE=false
#YARN_AUTH_USER=hadoop
#YARN_AUTH_PWD=123456

## request spnego enabled Yarn resource restful interface When Yarn enable kerberos
## If your environment yarn interface can be accessed directly, ignore it
#YARN_KERBEROS_ENABLE=true
#YARN_PRINCIPAL_NAME=yarn
#YARN_KEYTAB_PATH=/etc/security/keytabs/yarn.keytab
#YARN_KRB5_PATH=/etc/krb5.conf


##############################################################
#
#    NOTICE:
#         You can also set these variables as system environment in ~/.bashrc file

#HADOOP
HADOOP_HOME=/opt/cloudera/parcels/CDH/lib/hadoop
HADOOP_CONF_DIR=/etc/hadoop/conf
#HADOOP_KERBEROS_ENABLE=true
#HADOOP_KEYTAB_PATH=/appcom/keytab/

#Hive
HIVE_HOME=/opt/cloudera/parcels/CDH/lib/hive
HIVE_CONF_DIR=/etc/hive/conf

#Spark
SPARK_HOME=/opt/cloudera/parcels/CDH/lib/spark
SPARK_CONF_DIR=/etc/spark/conf


## Engine version conf
#SPARK_VERSION
SPARK_VERSION=2.4.0

##HIVE_VERSION
HIVE_VERSION=2.1.1

#PYTHON_VERSION=python2

## Hadoop env version
HADOOP_VERSION=3.0.0

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
#EUREKA: Microservices Service Registration Discovery Center
#EUREKA_INSTALL_IP=127.0.0.1
EUREKA_PORT=8761
export EUREKA_PREFER_IP=false

##linkis-mg-gateway
#GATEWAY_INSTALL_IP=127.0.0.1
GATEWAY_PORT=9111

##linkis-cg-linkismanager
#MANAGER_INSTALL_IP=127.0.0.1
MANAGER_PORT=9101

##linkis-cg-engineconnmanager
#ENGINECONNMANAGER_INSTALL_IP=127.0.0.1
ENGINECONNMANAGER_PORT=9102

##linkis-cg-entrance
#ENTRANCE_INSTALL_IP=127.0.0.1
ENTRANCE_PORT=9104

##linkis-ps-publicservice
#PUBLICSERVICE_INSTALL_IP=127.0.0.1
PUBLICSERVICE_PORT=9105

########################################################################################

## LDAP is for enterprise authorization, if you just want to have a try, ignore it.
#LDAP_URL=ldap://localhost:1389/
#LDAP_BASEDN=dc=apache,dc=com
#LDAP_USER_NAME_FORMAT=cn=%s@xxx.com,OU=xxx,DC=xxx,DC=com

## java application default jvm memory
export SERVER_HEAP_SIZE="512M"

##The decompression directory and the installation directory need to be inconsistent
LINKIS_HOME=/opt/linkis

##The extended lib such mysql-connector-java-*.jar
#LINKIS_EXTENDED_LIB=/appcom/common/linkisExtendedLib

LINKIS_VERSION=1.3.1

# for install
LINKIS_PUBLIC_MODULE=lib/linkis-commons/public-module

## modify by jiangkun0928 for change skywalking -> pinpoint start
### If SKYWALKING_AGENT_PATH is set, the Linkis components will be started with Skywalking agent
#SKYWALKING_AGENT_PATH=/opt/skywalking-agent/skywalking-agent.jar
#export SW_AGENT_COLLECTOR_BACKEND_SERVICES=bigd-sit-monitor:11800

## the Linkis components will be started with pinpoint agent
PINPOINT_AGENT_PATH=/opt/pinpoint-agent/pinpoint-bootstrap.jar
export PP_APPLICATION_NAME=apache-linkis
## modify by jiangkun0928 for change skywalking -> pinpoint end

##If you want to enable prometheus for monitoring linkis, you can set this export PROMETHEUS_ENABLE=true
export PROMETHEUS_ENABLE=true

#If you only want to experience linkis streamlined services, not rely on hdfs
#you can set the following configuration to false  and for the configuration related to the file directory,
#use path mode of [file://] to replace [hdfs://]
export ENABLE_HDFS=true
export ENABLE_HIVE=true
export ENABLE_SPARK=true
