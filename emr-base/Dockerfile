#
#  Copyright 2019 WeBank
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

FROM centos:latest
MAINTAINER brian.rongzhang@gmail.com

RUN yum -y install wget tar && \
    mkdir /opt/{flink,hadoop,spark,hive}

WORKDIR /tmp

RUN wget -O jdk-8u221-linux-x64.tar.gz \
  -c --content-disposition \
  "https://javadl.oracle.com/webapps/download/AutoDL?BundleId=239835_230deb18db3e4014bb8e3e8324f81b43" && \
   wget "https://archive.apache.org/dist/flink/flink-1.7.2/flink-1.7.2-bin-hadoop27-scala_2.11.tgz" && \
   wget "https://downloads.lightbend.com/scala/2.11.8/scala-2.11.8.tgz" && \
   wget "http://archive.apache.org/dist/spark/spark-2.4.4/spark-2.4.4-bin-hadoop2.7.tgz" && \
   wget "http://archive.apache.org/dist/hive/hive-2.3.6/apache-hive-2.3.6-bin.tar.gz" && \
   wget "http://archive.apache.org/dist/hadoop/common/hadoop-2.7.7/hadoop-2.7.7.tar.gz"


#COPY flink-1.7.2-bin-hadoop27-scala_2.11.tgz .
#COPY scala-2.11.8.tgz .
#COPY jdk-8u221-linux-x64.tar.gz .
#COPY hadoop-2.7.7.tar.gz .
#COPY spark-2.4.4-bin-hadoop2.7.tgz .
#COPY apache-hive-2.3.6-bin.tar.gz .

RUN tar xf flink-1.7.2-bin-hadoop27-scala_2.11.tgz -C /opt/flink/ && \
    tar xf scala-2.11.8.tgz -C /usr/local/ && \
    tar xf jdk-8u221-linux-x64.tar.gz -C /usr/local/ && \
    tar xf hadoop-2.7.7.tar.gz -C /opt/hadoop/ && \
    tar xf spark-2.4.4-bin-hadoop2.7.tgz -C /opt/spark/ && \
    tar xf apache-hive-2.3.6-bin.tar.gz -C /opt/hive/ && \
    rm -f *.tgz *.tar.gz

#COPY hive/conf /opt/hive/apache-hive-2.3.6-bin/conf
#COPY hadoop/conf /opt/hadoop/hadoop-2.7.7/etc/hadoop
#COPY jdk-8u221-linux-x64.tar.gz  /tmp
#COPY scala-2.11.8.tgz /tmp
#COPY spark-2.4.4-bin-hadoop2.7.tgz /tmp
#
#RUN tar xf jdk-8u221-linux-x64.tar.gz -C /usr/local/ && \
#    tar xf scala-2.11.8.tgz -C /usr/local/ && \
#    tar xf spark-2.4.4-bin-hadoop2.7.tgz -C /opt/spark/ && \
#    rm -f *.tgz *.tar.gz

ENV TIME_ZONE Asia/Shanghai
ENV SPARK_HOME=/opt/spark/spark-2.4.4-bin-hadoop2.7
ENV SPARK_CONF_DIR=$SPARK_HOME/conf
ENV PYSPARK_ALLOW_INSECURE_GATEWAY=1
ENV HIVE_HOME=/opt/hive/apache-hive-2.3.6-bin
ENV FLINK_HOME=/opt/flink/flink-1.7.2
ENV HIVE_CONF_DIR=$HIVE_HOME/conf
ENV SCALA_HOME=/usr/local/scala-2.11.8
ENV HADOOP_HOME=/opt/hadoop/hadoop-2.7.7
ENV HADOOP_CONF_PATH=$HADOOP_HOME/etc/hadoop
ENV HADOOP_CONF_DIR=$HADOOP_HOME/etc/hadoop
ENV JAVA_HOME /usr/local/jdk1.8.0_221
ENV CLASSPATH $JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar
ENV PATH ${JAVA_HOME}/bin/:${SPARK_HOME}/bin:${HIVE_HOME}/bin:${SCALA_HOME}/bin:${FLINK_HOME}/bin:${HADOOP_HOME}/sbin:${HADOOP_HOME}/bin:$PATH