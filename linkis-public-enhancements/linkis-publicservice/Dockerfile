FROM nm.hub.com/luban/linkis-base:1.0.4

WORKDIR /opt/linkis

COPY target/out/ /opt/linkis/linkis-publicservice/
COPY target/*.jar /opt/linkis/linkis-publicservice/lib/

ENTRYPOINT ["linkis-publicservice/bin/startup.sh"]