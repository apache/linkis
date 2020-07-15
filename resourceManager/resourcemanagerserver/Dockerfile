FROM 10.206.0.5/library/linkis:emr-base-spark2.4.4
MAINTAINER brian.rongzhang@gmail.com

RUN yum install -y unzip
WORKDIR /opt/ihome

COPY target/linkis-resourcemanager.zip /opt/ihome
RUN unzip linkis-resourcemanager.zip

WORKDIR /opt/ihome/linkis-resourcemanager/bin
ENTRYPOINT ["/opt/ihome/linkis-resourcemanager/bin/startup.sh"]