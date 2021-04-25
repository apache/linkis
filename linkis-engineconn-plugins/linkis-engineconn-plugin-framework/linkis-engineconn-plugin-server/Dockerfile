FROM nm.hub.com/luban/linkis-base:1.0.4

WORKDIR /opt/linkis

COPY target/out/ /opt/linkis/linkis-engineconn-plugin-server/
COPY target/*.jar /opt/linkis/linkis-engineconn-plugin-server/lib/
COPY plugins/ /opt/linkis/linkis-engineconn-plugin-server/plugins/

ENTRYPOINT ["linkis-engineconn-plugin-server/bin/startup.sh"]