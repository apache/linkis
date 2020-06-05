FROM zhangrong1027/linkis:emr-base-spark2.4.4

RUN yum install -y unzip
WORKDIR /opt/ihome

COPY target/linkis-ujes-pipeline-entrance.zip /opt/ihome
RUN unzip linkis-ujes-pipeline-entrance.zip

WORKDIR /opt/ihome/linkis-ujes-pipeline-entrance/bin
ENTRYPOINT ["/opt/ihome/linkis-ujes-pipeline-entrance/bin/startup.sh"]