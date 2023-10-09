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

shellDir=`dirname $0`
workDir=`cd ${shellDir}/..;pwd`
DBParDir=${workDir}/deploy-config/db.sh
LinkisParDir=${workDir}/deploy-config/linkis-env.sh

source ${workDir}/bin/common.sh
source ${workDir}/deploy-config/linkis-env.sh
source ${workDir}/deploy-config/db.sh

function print_usage(){
  echo "Usage: checkAdd [EngineName]"
  echo " EngineName : The Engine name that you want to check"
  echo " Engine list as bellow: JDBC Flink openLooKeng Presto Sqoop Elasticsearch Impala Trino Seatunnel"
}

if [ $# -gt 1 ]; then
  print_usage
  exit 2
fi

# Define verification functions for addtional engines: 1,check command;2,check parameters;3,check server status.

function checkJDBC(){

# 1. check command
    java -version > /dev/null 2>&1
    isSuccess "execute cmd: java -version"

# 2. check parameters
     if [ -z "${MYSQL_HOST}" ] || [ -z "${MYSQL_PORT}" ] || [ -z "${MYSQL_DB}" ] || [ -z "${MYSQL_USER}" ] || [ -z "${MYSQL_PASSWORD}" ];then
       echo "[MYSQL_HOST/MYSQL_PORT/MYSQL_USER/MYSQL_PASSWORD] are Invalid,Pls check parameters  in ${DBParDir} "
       exit 2
     fi

     if [ -z "${MYSQL_CONNECT_JAVA_PATH}" ] || [ ! -f ${MYSQL_CONNECT_JAVA_PATH} ];then
       echo "MySQL connector ${MYSQL_CONNECT_JAVA_PATH} is not exist,Pls check parameters in ${LinkisParDir} "
       exit 2
     fi

# 3. check server status
# set java path and other independency
CLASSPATH=$CLASSPATH:${MYSQL_CONNECT_JAVA_PATH}

# prepare java code to connect with JDBC dirver
echo "import java.sql.*;

public class JdbcTest {
    public static void main(String[] args) {
        // define connection variables
        String url = \"jdbc:mysql://${MYSQL_HOST}:${MYSQL_PORT}/${MYSQL_DB}\";
        String username = \"${MYSQL_USER}\";
        String password = \"${MYSQL_PASSWORD}\";

        // Try to connect use JDBC Driver
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            System.out.println(\"jdbc:mysql://${MYSQL_HOST}:${MYSQL_PORT} connection successful！\");
        } catch (SQLException e) {
            System.out.println(\"connection failed：\");
            e.printStackTrace();
        }
    }
}" > JdbcTest.java

# compile java source
    javac -cp "$CLASSPATH" JdbcTest.java

# execute java program
    java -cp "$CLASSPATH":. JdbcTest
    isSuccess "execute cmd: java -cp CLASSPATH:. JdbcTest"

#clear files
    rm JdbcTest.*

}

function checkFlink(){

# 1. check command
    flink --version > /dev/null 2>&1
    isSuccess "execute cmd: flink--version"

# 2. check parameters
      if [ -z ${FLINK_HOME} ];then
         echo "Parameter [FLINK_HOME] is Invalid,Pls check parameters  in ${LinkisParDir} "
         exit 2
      fi

# 3. check server status
    cd ${FLINK_HOME}
    ./bin/flink run -m yarn-cluster ./examples/batch/WordCount.jar > /dev/null 2>&1
    isSuccess "execute cmd: Flink run -m yarn-cluster "

}

function checkopenLooKeng(){

# 2. check parameters
     if [ -z "${OLK_HOST}" ] || [ -z "${OLK_PORT}" ] || [ -z "${OLK_CATALOG}" ]|| [ -z "${OLK_SCHEMA}" ] || [ -z "${OLK_USER}" ] || [ -z "${OLK_PASSWORD}" ];then
       echo "[OLK_HOST/OLK_PORT/OLK_USER/OLK_PASSWORD] are  Invalid,Pls check parameters in ${DBParDir} "
       exit 2
     fi

     if [ -z "${OLK_JDBC_PATH}" ] || [ ! -f ${OLK_JDBC_PATH} ];then
       echo "openLooKeng connector ${OLK_JDBC_PATH} is not exist,Pls check parameters in ${LinkisParDir} "
       exit 2
     fi

# 3. check server status
# set java path and other independency
CLASSPATH=$CLASSPATH:${OLK_JDBC_PATH}

# prepare java code to connect with JDBC dirver
echo "import java.sql.*;

public class openLooKengTest{
    public static void main(String[] args) {
        // define connection variables
        String url = \"jdbc:lk://${OLK_HOST}:${OLK_PORT}/${OLK_CATALOG}/${OLK_SCHEMA}\";
        String username = \"${OLK_USER}\";
        String password = \"${OLK_PASSWORD}\";

        // try to connect
        //try (Connection connection = DriverManager.getConnection(url, username, password)) {
        try (Connection connection = DriverManager.getConnection(url, username,null)) {
            System.out.println(\"connection successfully！\");
        } catch (SQLException e) {
            System.out.println(\"connection failed：\");
            e.printStackTrace();
        }
    }
}" > openLooKengTest.java

# compile java source code
javac -cp "$CLASSPATH" openLooKengTest.java

# execute java program
java -cp "$CLASSPATH":. openLooKengTest
isSuccess "execute cmd: java -cp CLASSPATH:. openLooKengTest"

# clear temple files
rm openLooKengTest.*

}

function checkPresto(){

# 1. check command
    presto --version > /dev/null 2>&1
    isSuccess "execute cmd: presto --version"

# 2. check parameters
      if [ -z "${PRESTO_HOST}" ] || [ -z "${PRESTO_PORT}" ] || [ -z "${PRESTO_CATALOG}" ]|| [ -z "${PRESTO_SCHEMA}"  ];then
       echo "[PRESTO_HOST/PRESTO_PORT/PRESTO_CATALOG/PRESTO_SCHEMA] are  Invalid,Pls check parameters in ${DBParDir}"
         exit 2
      fi

# 3. check server status
    presto --server ${PRESTO_HOST}:${PRESTO_PORT} --catalog ${PRESTO_CATALOG} --schema ${PRESTO_SCHEMA} --execute "show catalogs" >/dev/null 2>&1
    isSuccess "execute cmd: presto --server ${PRESTO_HOST}:${PRESTO_PORT}"
}

function checkSqoop(){

# 1. check command
    sqoop version > /dev/null 2>&1
    isSuccess "execute cmd: sqoop version"

# 2. check parameters
      if [ -z "${HIVE_META_URL}" ] || [ -z "${HIVE_META_USER}" ] || [ -z "${HIVE_META_PASSWORD}" ];then
       echo "[HIVE_META_URL/HIVE_META_USER/HIVE_META_PASSWORD] are  Invalid,Pls check parameters in ${DBParDir}"
         exit 2
      fi

# 3. check server status
    sqoop list-databases --connect ${HIVE_META_URL} --username ${HIVE_META_USER} --password ${HIVE_META_PASSWORD} >/dev/null 2>&1
    isSuccess "execute cmd: sqoop list-databases --connect ${HIVE_META_URL}"
    }

function checkElasticsearch(){

# 2. check parameters
      if [ -z "${ES_RESTFUL_URL}" ]; then
         echo "Parameter [ES_RESTFUL_URL] is Invalid,Pls check parameters in ${LinkisParDir}"
      fi

# 3. check server status
    curl ${ES_RESTFUL_URL} > /dev/null 2>&1
    isSuccess "execute cmd: curl ElasticSearch address ${ES_RESTFUL_URL}"
}

function checkImpala(){

# 1. check command
    impala-shell --version > /dev/null 2>&1
    isSuccess "execute cmd: impala-shell --version"

# 2. check parameters
      if [ -z "${IMPALA_HOST}" ] || [ -z "${IMPALA_PORT}" ];then
         echo "Parameter [IMPALA_HOST/IMPALA_PORT] are Invalid ,Pls check parameters in ${DBParDir}"
         exit 2
      fi

# 3. check server status
    impala-shell -i ${IMPALA_HOST}:${IMPALA_PORT} > /dev/null 2>&1
    isSuccess "execute cmd: impala-shell -i ${IMPALA_HOST}:${IMPALA_PORT}"

}

function checkTrino(){
# 1. check command
    trino-cli --version > /dev/null 2>&1
    isSuccess "execute cmd: trino-shell --version"

# 2. check parameters
      if [ -z "${TRINO_COORDINATOR_HOST}" ] || [ -z "${TRINO_COORDINATOR_PORT}" ];then
         echo "Parameter [TRINO_COORDINATOR_HOST/TRINO_COORDINATOR_PORT] are Invalid ,Pls check parameters in ${DBParDir}"
         exit 2
      fi

# 3. check server status
    trino-cli --server ${TRINO_COORDINATOR_HOST}:${TRINO_COORDINATOR_PORT} --catalog ${TRINO_COORDINATOR_CATALOG} --schema ${TRINO_COORDINATOR_SCHEMA} --execute "show catalogs" > /dev/null 2>&1
    isSuccess "trino-cli --server ${TRINO_COORDINATOR_HOST}:${TRINO_COORDINATOR_PORT}"
}

function checkSeatunnel(){

# 2. check parameters
      if [ -z "${SEATUNNEL_HOST}" ] || [ -z "${SEATUNNEL_PORT}" ];then
         echo "Parameter [SEATUNNEL_HOST/SEATUNNEL_PORT] are Invalid ,Pls check parameters in ${DBParDir}"
         exit 2
      fi

# 3. check server status
    curl http://${SEATUNNEL_HOST}:${SEATUNNEL_PORT} > /dev/null 2>&1
    isSuccess "execute cmd: curl http://${SEATUNNEL_HOST}:${SEATUNNEL_PORT}"
}


# Begin check addtional engine parameters
echo "======== Begin to check Engine: ${1} ======== "

EngineName=$1
case $EngineName in
    "JDBC")
        checkJDBC
        ;;
    "Flink")
        checkFlink
        ;;
    "openLooKeng")
        checkopenLooKeng
        ;;
    "Presto")
        checkPresto
        ;;
    "Sqoop")
        checkSqoop
        ;;
    "Elasticsearch")
        checkElasticsearch
        ;;
    "Impala")
        checkImpala
        ;;
    "Trino")
        checkTrino
        ;;
    "Seatunnel")
        checkSeatunnel
        ;;
    *)
        print_usage
        exit 2
        ;;
esac

echo "======== End checking Engine: ${1} ========== "