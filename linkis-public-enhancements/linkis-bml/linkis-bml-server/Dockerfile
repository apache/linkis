FROM nm.hub.com/luban/linkis-base:1.0.4

WORKDIR /opt/linkis

COPY target/out/ /opt/linkis/linkis-bml-server/
COPY target/*.jar /opt/linkis/linkis-bml-server/lib/

ENTRYPOINT ["linkis-bml-server/bin/startup.sh"]







