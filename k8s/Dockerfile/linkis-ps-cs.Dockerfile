FROM linkis-base:1.2.0

WORKDIR /opt/linkis

COPY lib/linkis-commons/public-module /opt/linkis/linkis-ps-cs/lib/
COPY lib/linkis-public-enhancements/linkis-ps-cs/ /opt/linkis/linkis-ps-cs/lib/
COPY jars/mysql-connector-java-5.1.49.jar /opt/linkis/linkis-ps-cs/lib/
COPY jars/jakarta.ws.rs-api-2.1.6.jar /opt/linkis/linkis-ps-cs/lib/
COPY sbin/k8s/linkis-ps-cs.sh /opt/linkis/linkis-ps-cs/bin/startup.sh

ENTRYPOINT ["linkis-ps-cs/bin/startup.sh"]
