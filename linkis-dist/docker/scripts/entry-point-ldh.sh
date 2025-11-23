#!/usr/bin/env bash
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

WORK_DIR=`cd $(dirname $0); pwd -P`

# start all components
# - hdfs & yarn
hdfs namenode -format
/opt/ldh/current/hadoop/sbin/hadoop-daemon.sh start namenode
/opt/ldh/current/hadoop/sbin/hadoop-daemon.sh start datanode
# hadoop2.7 is yarn-daemon.sh
/opt/ldh/current/hadoop/bin/yarn --daemon  start resourcemanager
/opt/ldh/current/hadoop/bin/yarn --daemon start nodemanager

# - init dirs on hdfs
hdfs dfs -mkdir -p /tmp
hdfs dfs -chmod -R 777 /tmp
hdfs dfs -mkdir -p /user
hdfs dfs -chmod -R 777 /user
hdfs dfs -mkdir -p /spark2-history
hdfs dfs -chmod -R 777 /spark2-history
hdfs dfs -mkdir -p /completed-jobs
hdfs dfs -chmod -R 777 /completed-jobs
hdfs dfs -chmod 777 /

#copy mysql-connector-java-*.jar from shared directory
mysql_connector_jar_path=/opt/ldh/current/hive/lib/mysql-connector-java-*.jar
jar_files=$(ls $mysql_connector_jar_path  2> /dev/null | wc -l);

if [ "$jar_files" == "0" ] ;then  #if not exist
  echo "try to copy mysql-connector-java-*.jar to /opt/ldh/current/hive/lib/ and /opt/ldh/current/spark/jars/"
  cp /opt/common/extendlib/mysql-connector-java-*.jar /opt/ldh/current/hive/lib/
  cp /opt/common/extendlib/mysql-connector-java-*.jar /opt/ldh/current/spark/jars/
fi


# - hive
/opt/ldh/current/hive/bin/schematool -initSchema -dbType mysql
/opt/ldh/current/hive/bin/hive --service metastore > /var/log/hive/metastore.out 2>&1 &
/opt/ldh/current/hive/bin/hive --service hiveserver2 > /var/log/hive/hiveserver2.out 2>&1 &

# spark
/opt/ldh/current/spark/sbin/start-history-server.sh

# flink
HADOOP_CLASSPATH=`hadoop classpath` /opt/ldh/current/flink/bin/yarn-session.sh --detached

# zookeeper
/opt/ldh/current/zookeeper/bin/zkServer.sh start

# hold on
while true; do sleep 3600; done
