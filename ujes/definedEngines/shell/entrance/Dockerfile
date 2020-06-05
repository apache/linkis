FROM hub.ihomefnt.com/library/linkis:emr-base-spark2.4.4

RUN yum install -y unzip
WORKDIR /opt/ihome

COPY target/linkis-ujes-shell-entrance.zip /opt/ihome
RUN unzip linkis-ujes-shell-entrance.zip

WORKDIR /opt/ihome/linkis-ujes-shell-entrance/bin
ENTRYPOINT ["/opt/ihome/linkis-ujes-shell-entrance/bin/startup.sh"]