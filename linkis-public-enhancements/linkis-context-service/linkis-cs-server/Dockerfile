FROM nm.hub.com/luban/linkis-base:1.0.4

WORKDIR /opt/linkis

COPY target/out/ /opt/linkis/linkis-cs-server/
COPY target/*.jar /opt/linkis/linkis-cs-server/lib/

ENTRYPOINT ["linkis-cs-server/bin/startup.sh"]