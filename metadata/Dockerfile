FROM zhangrong1027/linkis:emr-base-spark2.4.4

RUN yum install -y unzip
WORKDIR /opt/ihome

COPY target/linkis-metadata.zip /opt/ihome
RUN unzip linkis-metadata.zip

WORKDIR /opt/ihome/linkis-metadata/bin
ENTRYPOINT ["/opt/ihome/linkis-metadata/bin/startup.sh"]