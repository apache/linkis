FROM hub.ihomefnt.com/library/linkis:emr-base-spark2.4.4

RUN yum install -y unzip
WORKDIR /opt/ihome

COPY target/linkis-publicservice.zip /opt/ihome
RUN unzip linkis-publicservice.zip

WORKDIR /opt/ihome/linkis-publicservice/bin
ENTRYPOINT ["/opt/ihome/linkis-publicservice/bin/startup.sh"]