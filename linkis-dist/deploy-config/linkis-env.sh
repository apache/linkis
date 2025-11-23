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

# SSH_PORT=22

### deploy user
deployUser=hadoop
##If you don't set it, a random password string will be generated during installation
deployPwd=

### database type
### choose mysql or postgresql, default mysql
dbType=mysql
export dbType

##Linkis_SERVER_VERSION
LINKIS_SERVER_VERSION=v1

### Specifies the user workspace, which is used to store the user's script files and log files.
### Generally local directory, path mode can be [file://] or [hdfs://]
WORKSPACE_USER_ROOT_PATH=file:///tmp/linkis/
### User's root hdfs path, path mode can be [file://] or [hdfs://]
HDFS_USER_ROOT_PATH=hdfs:///tmp/linkis



### Path to store started engines and engine logs, must be local
ENGINECONN_ROOT_PATH=/appcom/tmp
###path mode can be [file://] or [hdfs://]
#ENTRANCE_CONFIG_LOG_PATH=hdfs:///tmp/linkis/

### Path to store job ResultSet
### path mode can be [file://] or [hdfs://]
RESULT_SET_ROOT_PATH=hdfs:///tmp/linkis

##YARN REST URL  spark engine required
# Active resourcemanager address needed. Recommended to add all ha addresses. eg YARN_RESTFUL_URL="http://127.0.0.1:8088;http://127.0.0.2:8088"
YARN_RESTFUL_URL="http://127.0.0.1:8088"

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
HADOOP_HOME=${HADOOP_HOME:-"/appcom/Install/hadoop"}
HADOOP_CONF_DIR=${HADOOP_CONF_DIR:-"/appcom/config/hadoop-config"}
HADOOP_KERBEROS_ENABLE=${HADOOP_KERBEROS_ENABLE:-"false"}
HADOOP_KEYTAB_PATH=${HADOOP_KEYTAB_PATH:-"/appcom/keytab/"}
## Hadoop env version
HADOOP_VERSION=${HADOOP_VERSION:-"3.3.4"}

#Hive
HIVE_HOME=/appcom/Install/hive
HIVE_CONF_DIR=/appcom/config/hive-config

#Spark
SPARK_HOME=/appcom/Install/spark
SPARK_CONF_DIR=/appcom/config/spark-config


## Engine version conf
#SPARK_VERSION
#SPARK_VERSION=3.2.1

##HIVE_VERSION
#HIVE_VERSION=3.1.3

#PYTHON_VERSION=python2

################### The install Configuration of all Micro-Services #####################
#
#    NOTICE:
#       1. If you just wanna try, the following micro-service configuration can be set without any settings.
#            These services will be installed by default on this machine.
#       2. In order to get the most complete enterprise-level features, we strongly recommend that you install
#            Linkis in a distributed manner and set the following microservice parameters
#

###  DISCOVERY
DISCOVERY=EUREKA

###  EUREKA install information
###  You can access it in your browser at the address below:http://${EUREKA_INSTALL_IP}:${EUREKA_PORT}
#EUREKA: Microservices Service Registration Discovery Center
#EUREKA_INSTALL_IP=127.0.0.1
EUREKA_PORT=20303
export EUREKA_PREFER_IP=false
#EUREKA_HEAP_SIZE="512M"

###  NACOS install information
###  NACOS
NACOS_SERVER_ADDR=127.0.0.1:8848

##linkis-mg-gateway
#GATEWAY_INSTALL_IP=127.0.0.1
GATEWAY_PORT=9001
#GATEWAY_HEAP_SIZE="512M"

##linkis-cg-linkismanager
#MANAGER_INSTALL_IP=127.0.0.1
MANAGER_PORT=9101
#MANAGER_HEAP_SIZE="512M"

##linkis-cg-engineconnmanager
#ENGINECONNMANAGER_INSTALL_IP=127.0.0.1
ENGINECONNMANAGER_PORT=9102
#ENGINECONNMANAGER_HEAP_SIZE="512M"

##linkis-cg-entrance
#ENTRANCE_INSTALL_IP=127.0.0.1
ENTRANCE_PORT=9104
#ENTRANCE_HEAP_SIZE="512M"

##linkis-ps-publicservice
#PUBLICSERVICE_INSTALL_IP=127.0.0.1
PUBLICSERVICE_PORT=9105
#PUBLICSERVICE_HEAP_SIZE="512M"

########################################################################################

## LDAP is for enterprise authorization, if you just want to have a try, ignore it.
#LDAP_URL=ldap://localhost:1389/
#LDAP_BASEDN=dc=apache,dc=com
#LDAP_USER_NAME_FORMAT=cn=%s@xxx.com,OU=xxx,DC=xxx,DC=com

## java application default jvm memory
export SERVER_HEAP_SIZE="512M"

##The decompression directory and the installation directory need to be inconsistent
#LINKIS_HOME=/appcom/Install/LinkisInstall

##The extended lib such mysql-connector-java-*.jar
#LINKIS_EXTENDED_LIB=/appcom/common/linkisExtendedLib

LINKIS_VERSION=1.8.0

# for install
LINKIS_PUBLIC_MODULE=lib/linkis-commons/public-module

## If SKYWALKING_AGENT_PATH is set, the Linkis components will be started with Skywalking agent
#SKYWALKING_AGENT_PATH=/appcom/config/skywalking-agent/skywalking-agent.jar

##If you want to enable prometheus for monitoring linkis, you can set this export PROMETHEUS_ENABLE=true
export PROMETHEUS_ENABLE=false

#If you only want to experience linkis streamlined services, not rely on hdfs
#you can set the following configuration to false  and for the configuration related to the file directory,
#use path mode of [file://] to replace [hdfs://]
export ENABLE_HDFS=true
export ENABLE_HIVE=true
export ENABLE_SPARK=true

## define  MYSQL_CONNECT_JAVA_PATH&OLK_JDBC_PATH, the linkis can check JDBC driver
MYSQL_CONNECT_JAVA_PATH=
OLK_JDBC_PATH=