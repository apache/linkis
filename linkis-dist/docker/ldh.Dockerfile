#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

######################################################################
# hadoop all in one image
######################################################################

ARG IMAGE_BASE=centos:7

FROM ${IMAGE_BASE} as linkis-ldh

ARG JDK_VERSION=1.8.0-openjdk
ARG JDK_BUILD_REVISION=1.8.0.332.b09-1.el7_9
ARG MYSQL_JDBC_VERSION=5.1.49

ARG HADOOP_VERSION=2.7.2
ARG HIVE_VERSION=2.3.3
ARG SPARK_VERSION=2.4.3
ARG SPARK_HADOOP_VERSION=2.7
ARG FLINK_VERSION=1.12.2
ARG ZOOKEEPER_VERSION=3.5.9

ARG LINKIS_VERSION=0.0.0

RUN useradd -r -s /bin/bash -u 100001 -g root -G wheel hadoop

RUN yum install -y \
       vim unzip curl sudo krb5-workstation sssd crontabs net-tools python-pip \
       java-${JDK_VERSION}-${JDK_BUILD_REVISION} \
       java-${JDK_VERSION}-devel-${JDK_BUILD_REVISION} \
       mysql \
    && yum clean all

RUN sed -i "s#^%wheel.*#%wheel        ALL=(ALL)       NOPASSWD: ALL#g" /etc/sudoers

RUN mkdir -p /opt/ldh/${LINKIS_VERSION} \
    && mkdir -p /opt/ldh/current \
    && mkdir -p /data \
    && chmod 777 -R /data

ADD ldh-tars/hadoop-${HADOOP_VERSION}.tar.gz /opt/ldh/${LINKIS_VERSION}/
ADD ldh-tars/apache-hive-${HIVE_VERSION}-bin.tar.gz /opt/ldh/${LINKIS_VERSION}/
ADD ldh-tars/spark-${SPARK_VERSION}-bin-hadoop${SPARK_HADOOP_VERSION}.tgz /opt/ldh/${LINKIS_VERSION}/
ADD ldh-tars/flink-${FLINK_VERSION}-bin-scala_2.11.tgz /opt/ldh/${LINKIS_VERSION}/
ADD ldh-tars/apache-zookeeper-${ZOOKEEPER_VERSION}-bin.tar.gz /opt/ldh/${LINKIS_VERSION}/

RUN mkdir -p /etc/ldh \
    && mkdir -p /var/log/hadoop && chmod 777 -R /var/log/hadoop \
    && mkdir -p /var/log/hive && chmod 777 -R /var/log/hive \
    && mkdir -p /var/log/spark && chmod 777 -R /var/log/spark \
    && mkdir -p /var/log/flink && chmod 777 -R /var/log/flink \
    && mkdir -p /var/log/zookeeper && chmod 777 -R /var/log/zookeeper \
    && ln -s /opt/ldh/${LINKIS_VERSION}/hadoop-${HADOOP_VERSION} /opt/ldh/current/hadoop \
    && ln -s /opt/ldh/${LINKIS_VERSION}/apache-hive-${HIVE_VERSION}-bin /opt/ldh/current/hive \
    && ln -s /opt/ldh/${LINKIS_VERSION}/spark-${SPARK_VERSION}-bin-hadoop${SPARK_HADOOP_VERSION} /opt/ldh/current/spark \
    && ln -s /opt/ldh/${LINKIS_VERSION}/flink-${FLINK_VERSION} /opt/ldh/current/flink \
    && ln -s /opt/ldh/${LINKIS_VERSION}/apache-zookeeper-${ZOOKEEPER_VERSION}-bin /opt/ldh/current/zookeeper

RUN curl -L -o /opt/ldh/current/hive/lib/mysql-connector-java-${MYSQL_JDBC_VERSION}.jar \
            https://repo1.maven.org/maven2/mysql/mysql-connector-java/${MYSQL_JDBC_VERSION}/mysql-connector-java-${MYSQL_JDBC_VERSION}.jar \
    && cp /opt/ldh/current/hive/lib/mysql-connector-java-${MYSQL_JDBC_VERSION}.jar /opt/ldh/current/spark/jars/

ENV JAVA_HOME /etc/alternatives/jre
ENV PATH /opt/ldh/current/hadoop/bin:/opt/ldh/current/hive/bin:/opt/ldh/current/spark/bin:/opt/ldh/current/flink/bin:/opt/ldh/current/zookeeper/bin:$PATH
ENV HADOOP_CONF_DIR=/etc/ldh/hadoop
ENV HIVE_CONF_DIR=/etc/ldh/hive
ENV SPARK_CONF_DIR=/etc/ldh/spark
ENV FLINK_CONF_DIR=/etc/ldh/flink
ENV ZOOCFGDIR=/etc/ldh/zookeeper
ENV ZOO_LOG_DIR=/var/log/zookeeper

COPY entry-point-ldh.sh /usr/bin/start-all.sh
RUN chmod +x /usr/bin/start-all.sh

CMD ["sh", "/usr/bin/start-all.sh"]
