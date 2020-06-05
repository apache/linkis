FROM centos:latest
MAINTAINER zhangrong

RUN yum -y install wget tar && \
    mkdir /opt/{flink,hadoop,spark,hive}

WORKDIR /tmp

COPY hive/conf /opt/hive/apache-hive-2.3.6-bin/conf
COPY hadoop/conf /opt/hadoop/hadoop-2.7.7/etc/hadoop
COPY jdk-8u221-linux-x64.tar.gz  /tmp
COPY scala-2.11.8.tgz /tmp
COPY spark-2.4.4-bin-hadoop2.7.tgz /tmp

RUN tar xf jdk-8u221-linux-x64.tar.gz -C /usr/local/ && \
    tar xf scala-2.11.8.tgz -C /usr/local/ && \
    tar xf spark-2.4.4-bin-hadoop2.7.tgz -C /opt/spark/ && \
    rm -f *.tgz *.tar.gz

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