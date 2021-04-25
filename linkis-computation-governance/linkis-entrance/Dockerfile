FROM nm.hub.com/luban/linkis-base:1.0.4

WORKDIR /opt/linkis

COPY target/out/ /opt/linkis/linkis-entrance/
COPY target/*.jar /opt/linkis/linkis-entrance/lib/

ENTRYPOINT ["linkis-entrance/bin/startup.sh"]