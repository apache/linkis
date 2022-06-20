FROM linkis-base:1.2.0

WORKDIR /opt/linkis

COPY lib/linkis-commons/public-module/ /opt/linkis/public-module/
COPY lib/linkis-spring-cloud-services/linkis-mg-gateway/ /opt/linkis/linkis-mg-gateway/lib/
COPY sbin/k8s/linkis-mg-gateway.sh /opt/linkis/linkis-mg-gateway/bin/startup.sh

ENTRYPOINT ["linkis-mg-gateway/bin/startup.sh"]
