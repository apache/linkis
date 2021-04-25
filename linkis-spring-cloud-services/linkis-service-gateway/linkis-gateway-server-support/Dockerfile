FROM nm.hub.com/luban/linkis-base:1.0.4

WORKDIR /opt/linkis

COPY target/out/ /opt/linkis/linkis-gateway-server-support/
COPY target/*.jar /opt/linkis/linkis-gateway-server-support/lib/

ENTRYPOINT ["linkis-gateway-server-support/bin/startup.sh"]