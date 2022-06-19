FROM linkis-base:1.2.0

WORKDIR /opt/linkis

COPY lib/linkis-commons/public-module /opt/linkis/linkis-cg-linkismanager/lib/
COPY lib/linkis-computation-governance/linkis-cg-linkismanager/ /opt/linkis/linkis-cg-linkismanager/lib/
COPY jars/mysql-connector-java-5.1.49.jar /opt/linkis/linkis-cg-linkismanager/lib/
COPY jars/jakarta.ws.rs-api-2.1.6.jar /opt/linkis/linkis-cg-linkismanager/lib/
COPY sbin/k8s/linkis-cg-linkismanager.sh /opt/linkis/linkis-cg-linkismanager/bin/startup.sh

ENTRYPOINT ["linkis-cg-linkismanager/bin/startup.sh"]
