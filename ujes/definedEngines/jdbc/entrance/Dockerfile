FROM 10.206.0.5/library/linkis:emr-base-spark2.4.4

RUN yum install -y unzip
WORKDIR /opt/ihome

COPY target/linkis-ujes-jdbcentrance.zip /opt/ihome
RUN unzip linkis-ujes-jdbcentrance.zip

WORKDIR /opt/ihome/linkis-ujes-jdbcentrance/bin
ENTRYPOINT ["/opt/ihome/linkis-ujes-jdbcentrance/bin/startup.sh"]