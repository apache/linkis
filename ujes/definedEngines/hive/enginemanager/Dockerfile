FROM zhangrong1027/linkis:emr-base-spark2.4.4

RUN yum install -y unzip
WORKDIR /opt/ihome

COPY target/linkis-ujes-hive-enginemanager.zip /opt/ihome
RUN unzip linkis-ujes-hive-enginemanager.zip

WORKDIR /opt/ihome/linkis-ujes-hive-enginemanager/bin
ENTRYPOINT ["/opt/ihome/linkis-ujes-hive-enginemanager/bin/startup.sh"]