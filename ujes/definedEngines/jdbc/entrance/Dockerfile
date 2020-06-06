FROM 10.206.0.5/library/linkis:emr-base-spark2.4.4
MAINTAINER brian.rongzhang@gmail.com

RUN yum install -y unzip
WORKDIR /opt/ihome

COPY target/linkis-ujes-jdbc-entrance.zip /opt/ihome
RUN unzip linkis-ujes-jdbc-entrance.zip

WORKDIR /opt/ihome/linkis-ujes-jdbc-entrance/bin
ENTRYPOINT ["/opt/ihome/linkis-ujes-jdbcentrance/bin/startup.sh"]