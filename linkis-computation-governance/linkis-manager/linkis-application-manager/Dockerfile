FROM nm.hub.com/luban/linkis-base:1.0.4

WORKDIR /opt/linkis

COPY target/out/ /opt/linkis/linkis-application-manager/
COPY target/*.jar /opt/linkis/linkis-application-manager/lib/

ENTRYPOINT ["linkis-application-manager/bin/startup.sh"]