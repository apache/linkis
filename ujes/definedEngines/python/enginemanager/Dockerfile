FROM zhangrong1027/linkis:emr-base-spark2.4.4

RUN yum install -y unzip
WORKDIR /opt/ihome

COPY target/linkis-ujes-python-enginemanager.zip /opt/ihome
RUN unzip linkis-ujes-python-enginemanager.zip

WORKDIR /opt/ihome/linkis-ujes-python-enginemanager/bin
ENTRYPOINT ["/opt/ihome/linkis-ujes-python-enginemanager/bin/startup.sh"]