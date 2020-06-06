FROM 10.206.0.5/library/linkis:emr-base-spark2.4.4

RUN yum install -y unzip
WORKDIR /opt/ihome

COPY target/linkis-ujes-mlsql-entrance.zip /opt/ihome
RUN unzip linkis-ujes-mlsql-entrance.zip

WORKDIR /opt/ihome/linkis-ujes-mlsql-entrance/bin
ENTRYPOINT ["/opt/ihome/linkis-ujes-mlsql-entrance/bin/startup.sh"]