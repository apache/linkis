FROM 10.206.0.5/library/linkis:emr-base-spark2.4.4

MAINTAINER brian.rongzhang@gmail.com

RUN yum install -y unzip
WORKDIR /opt/ihome

COPY target/linkis-bml.zip /opt/ihome
RUN unzip linkis-bml.zip

WORKDIR /opt/ihome/linkis-bml/bin
ENTRYPOINT ["/opt/ihome/linkis-bml/bin/startup.sh"]