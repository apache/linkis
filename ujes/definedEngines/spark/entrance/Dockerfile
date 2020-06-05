FROM zhangrong1027/linkis:emr-base-spark2.4.4

RUN yum install -y unzip
WORKDIR /opt/ihome

COPY target/linkis-ujes-spark-entrance.zip /opt/ihome
RUN unzip linkis-ujes-spark-entrance.zip

WORKDIR /opt/ihome/linkis-ujes-spark-entrance/bin
ENTRYPOINT ["/opt/ihome/linkis-ujes-spark-entrance/bin/startup.sh"]