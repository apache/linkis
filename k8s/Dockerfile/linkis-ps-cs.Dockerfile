FROM nm.hub.com/luban/linkis-base:1.0.5

WORKDIR /opt/linkis

COPY lib/linkis-commons/public-module /opt/linkis/public-module
COPY lib/linkis-public-enhancements/linkis-ps-cs/ /opt/linkis/linkis-ps-cs/lib/
COPY sbin/k8s/linkis-ps-cs.sh /opt/linkis/linkis-ps-cs/bin/startup.sh

ENTRYPOINT ["linkis-ps-cs/bin/startup.sh"]