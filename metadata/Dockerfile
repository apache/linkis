FROM 10.206.0.5/library/linkis:emr-base-spark2.4.4

RUN yum install -y unzip
WORKDIR /opt/ihome

COPY target/linkis-metadata.zip /opt/ihome
RUN unzip linkis-metadata.zip

WORKDIR /opt/ihome/linkis-metadata/bin
ENTRYPOINT ["/opt/ihome/linkis-metadata/bin/startup.sh"]