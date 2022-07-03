# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
# 
#   http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

FROM centos:7

WORKDIR /usr/local

ENV TZ=Asia/Shanghai LANG=zh_CN.utf8 LC_ALL=zh_CN.UTF-8
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo '$TZ' > /etc/timezone
RUN localedef -c -f UTF-8 -i zh_CN zh_CN.utf8

RUN yum install -y vim java-1.8.0-openjdk.x86_64 java-1.8.0-openjdk-devel.x86_64 unzip curl sudo krb5-workstation sssd crontabs python-pip && yum clean all


COPY hadoop /usr/local/hadoop
#COPY python /tmp/python

COPY spark /usr/local/spark
#COPY flink /usr/local/flink
# hive客户端可选择不安装，只需要hive conf
COPY hive /usr/local/hive

RUN echo $' \n\
export LC_ALL="zh_CN.UTF-8"  \n\
export LANG="zh_CN.UTF-8"  \n\
export PYSPARK_ALLOW_INSECURE_GATEWAY=1  \n\
export SPARK_HOME=/usr/local/spark  \n\
export HIVE_HOME=/usr/local/hive  \n\
export HADOOP_HOME=/usr/local/hadoop \n\
export JAVA_HOME=/usr/lib/jvm/java-1.8.0 \n\
export HADOOP_CONF_DIR=$HADOOP_HOME/etc/hadoop \n\
export HIVE_CONF_DIR=$HIVE_HOME/conf \n\
export SPARK_CONF_DIR=$SPARK_HOME/conf \n\
export CLASSPATH=$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar \n\
export PATH=${JAVA_HOME}/bin/:${SPARK_HOME}/bin:${HIVE_HOME}/bin:${HADOOP_HOME}/bin:$PATH  \n\
' >> /etc/profile && source /etc/profile

ENV PYSPARK_ALLOW_INSECURE_GATEWAY=1
ENV SPARK_HOME=/usr/local/spark
ENV HIVE_HOME=/usr/local/hive
ENV HADOOP_HOME=/usr/local/hadoop
ENV HADOOP_CONF_DIR=$HADOOP_HOME/etc/hadoop
ENV HIVE_CONF_DIR=$HIVE_HOME/conf
ENV SPARK_CONF_DIR=$SPARK_HOME/conf
ENV JAVA_HOME /usr/lib/jvm/java-1.8.0
ENV CLASSPATH $JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar
ENV PATH ${JAVA_HOME}/bin/:${SPARK_HOME}/bin:${HIVE_HOME}/bin:${HADOOP_HOME}/sbin:${HADOOP_HOME}/bin:$PATH
