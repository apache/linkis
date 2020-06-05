FROM zhangrong1027/linkis:emr-base-spark2.4.4

RUN yum install -y unzip
WORKDIR /opt/ihome

COPY target/linkis-gateway.zip /opt/ihome
RUN unzip linkis-gateway.zip

WORKDIR /opt/ihome/linkis-gateway/bin
ENTRYPOINT ["/opt/ihome/linkis-gateway/bin/startup.sh"]