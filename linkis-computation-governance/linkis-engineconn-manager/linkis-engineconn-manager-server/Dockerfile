FROM nm.hub.com/luban/linkis-base:1.0.4

WORKDIR /opt/linkis

COPY target/out/ /opt/linkis/linkis-engineconn-manager-server/
COPY target/*.jar /opt/linkis/linkis-engineconn-manager-server/lib/

ENTRYPOINT ["linkis-engineconn-manager-server/bin/startup.sh"]