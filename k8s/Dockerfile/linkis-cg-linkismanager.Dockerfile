FROM nm.hub.com/luban/linkis-base:1.0.5

WORKDIR /opt/linkis

COPY lib/linkis-commons/public-module /opt/linkis/public-module
COPY lib/linkis-computation-governance/linkis-cg-linkismanager/ /opt/linkis/linkis-cg-linkismanager/lib/
COPY sbin/k8s/linkis-cg-linkismanager.sh /opt/linkis/linkis-cg-linkismanager/bin/startup.sh

ENTRYPOINT ["linkis-cg-linkismanager/bin/startup.sh"]