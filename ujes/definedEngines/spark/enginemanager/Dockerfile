FROM 10.206.0.5/library/linkis:emr-base-spark2.4.4
MAINTAINER brian.rongzhang@gmail.com
RUN yum install -y unzip
WORKDIR /opt/ihome

COPY target/linkis-ujes-spark-enginemanager.zip /opt/ihome
RUN unzip linkis-ujes-spark-enginemanager.zip

WORKDIR /opt/ihome/linkis-ujes-spark-enginemanager/bin
ENTRYPOINT ["/opt/ihome/linkis-ujes-spark-enginemanager/bin/startup.sh"]