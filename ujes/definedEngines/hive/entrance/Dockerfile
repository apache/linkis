FROM 10.206.0.5/library/linkis:emr-base-spark2.4.4

RUN yum install -y unzip
WORKDIR /opt/ihome

COPY target/linkis-ujes-hive-entrance.zip /opt/ihome
RUN unzip linkis-ujes-hive-entrance.zip

WORKDIR /opt/ihome/linkis-ujes-hive-entrance/bin
ENTRYPOINT ["/opt/ihome/linkis-ujes-hive-entrance/bin/startup.sh"]