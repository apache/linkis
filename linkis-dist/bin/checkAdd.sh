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
source ${workDir}/bin/common.sh
source ${workDir}/deploy-config/linkis-env.sh
source ${workDir}/deploy-config/db.sh

function print_usage(){
  echo "Usage: checkAdd [EngineName]"
  echo " EngineName : The Engine name that you want to check"
  echo " Engine list as bellow: JDBC Flink openLooKeng Pipeline Presto Sqoop Elasticsearch "
}

if [ $# -gt 1 ]; then
  print_usage
  exit 2
fi

# --- Begin Check service function 

function checkJDBC(){

# --- 1. check command
    java -version > /dev/null 2>&1
    isSuccess "execute cmd: java -version"

# --- 2. check parameters
     if [ -z "${MYSQL_HOST}" ] || [ -z "${MYSQL_PORT}" ] || [ -z "${MYSQL_DB}" ] || [ -z "${MYSQL_USER}" ] || [ -z "${MYSQL_PASSWORD}" ];then
       echo "MYSQL_HOST/MYSQL_PORT/MYSQL_USER/MYSQL_PASSWORD] are  Invalid,Pls check parameter define"
       exit 2
     fi
 
# --- 3. check server status
# 设置Java类路径，指向你所下载的MySQL JDBC驱动程序的JAR文件和其他依赖项
CLASSPATH=$CLASSPATH:/data/test/bin/mysql-connector-java-8.0.28.jar

# 编写Java代码，尝试建立JDBC连接并验证
echo "import java.sql.*;

public class JdbcTest {
    public static void main(String[] args) {
        // 定义连接信息
        String url = \"jdbc:mysql://${MYSQL_HOST}:${MYSQL_PORT}/${MYSQL_DB}\";
        String username = \"${MYSQL_USER}\";
        String password = \"${MYSQL_PASSWORD}\";

        // 尝试建立连接
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            System.out.println(\"jdbc:mysql://${MYSQL_HOST}:${MYSQL_PORT} 连接成功！\");
        } catch (SQLException e) {
            System.out.println(\"连接失败：\");
            e.printStackTrace();
        }
    }
}" > JdbcTest.java

# 编译Java源文件
    javac -cp "$CLASSPATH" JdbcTest.java

# 运行Java程序
    java -cp "$CLASSPATH":. JdbcTest
    isSuccess "execute cmd: java -cp CLASSPATH:. JdbcTest"

#clear files
    rm JdbcTest.*

}

function checkFlink(){

# --- 1. check command
    flink --version > /dev/null 2>&1
    isSuccess "execute cmd: flink--version"

# --- 2. check parameters
      if [ -z ${YARN_RESTFUL_URL} ];then
         echo "Parameter YARN_RESTFUL_URL is Invalid:" ${YARN_RESTFUL_URL}
         exit 2
      fi
 
# --- 3. check server status
    YanRMAddress=`echo ${YARN_RESTFUL_URL}|awk -F';' '{print $1}'`
    curl $YanRMAddress > /dev/null 2>&1
    isSuccess "execute cmd: Flink yarn service check "

}

function checkopenLooKeng(){

# --- 2. check parameters
     if [ -z "${OLK_HOST}" ] || [ -z "${OLK_PORT}" ] || [ -z "${OLK_CATALOG}" ]|| [ -z "${OLK_SCHEMA}" ] || [ -z "${OLK_USER}" ] || [ -z "${OLK_PASSWORD}" ];then
       echo "OLK_HOST/OLK_PORT/OLK_USER/OLK_PASSWORD] are  Invalid,Pls check parameter define"
       exit 2
     fi
 
# --- 3. check server status
# 设置Java类路径，指向你所下载的MySQL JDBC驱动程序的JAR文件和其他依赖项
CLASSPATH=$CLASSPATH:/data/test/bin/hetu-jdbc-1.10.0.jar

# 编写Java代码，尝试建立JDBC连接并验证
echo "import java.sql.*;

public class openLooKengTest{
    public static void main(String[] args) {
        // 定义连接信息
        String url = \"jdbc:lk://${OLK_HOST}:${OLK_PORT}/${OLK_CATALOG}/${OLK_SCHEMA}\";
        String username = \"${OLK_USER}\";
        String password = \"${OLK_PASSWORD}\";

        // 尝试建立连接
        //try (Connection connection = DriverManager.getConnection(url, username, password)) {
	try (Connection connection = DriverManager.getConnection(url, username,null)) {
            System.out.println(\"连接成功！\");
        } catch (SQLException e) {
            System.out.println(\"连接失败：\");
            e.printStackTrace();
        }
    }
}" > openLooKengTest.java

# 编译Java源文件
javac -cp "$CLASSPATH" openLooKengTest.java

# 运行Java程序
java -cp "$CLASSPATH":. openLooKengTest
isSuccess "execute cmd: java -cp CLASSPATH:. openLooKengTest"

# 清理临时文件
rm openLooKengTest.*

}

function checkPresto(){

# --- 1. check command
    presto --version > /dev/null 2>&1
    isSuccess "execute cmd: presto --version"

# --- 2. check parameters
      if [ -z "${PRESTO_HOST}" ] || [ -z "${PRESTO_PORT}" ] || [ -z "${PRESTO_CATALOG}" ]|| [ -z "${PRESTO_SCHEMA}"  ];then
       echo "PRESTO_HOST/PRESTO_PORT/PRESTO_CATALOG/PRESTO_SCHEMA] are  Invalid,Pls check parameters definition"
         exit 2
      fi

# --- 3. check server status
    presto --server ${PRESTO_HOST}:${PRESTO_PORT} --catalog ${PRESTO_CATALOG} --schema ${PRESTO_SCHEMA} --execute "show catalogs" >/dev/null 2>&1
    isSuccess "execute cmd: presto --server ${PRESTO_HOST}:${PRESTO_PORT}"
}

function checkSqoop(){

# --- 1. check command
    sqoop version > /dev/null 2>&1
    isSuccess "execute cmd: sqoop version"

# --- 2. check parameters
      if [ -z "${HIVE_META_URL}" ] || [ -z "${HIVE_META_USER}" ] || [ -z "${HIVE_META_PASSWORD}" ];then
       echo "[HIVE_META_URL/HIVE_META_USER/HIVE_META_PASSWORD] are  Invalid,Pls check parameters definition"
         exit 2
      fi

# --- 3. check server status
    sqoop list-databases --connect ${HIVE_META_URL} --username ${HIVE_META_USER} --password ${HIVE_META_PASSWORD} >/dev/null 2>&1
    isSuccess "execute cmd: sqoop list-databases --connect ${HIVE_META_URL}"
    }

function checkElasticsearch(){

# --- 2. check parameters
      if [ -z "${ES_RESTFUL_URL}" ]; then
         echo "Parameter ES_RESTFUL_URL is Invalid:" ${ES_RESTFUL_URL}
         exit 2
      fi

# --- 3. check server status
    curl ${ES_RESTFUL_URL} > /dev/null 2>&1
    isSuccess "execute cmd: curl ElasticSearch address ${ES_RESTFUL_URL}"
}

# --- Begin check addtional engine parameters
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
    *)
        print_usage
        exit 2
        ;;
esac

echo "======== End checking Engine: ${1} ========== "
