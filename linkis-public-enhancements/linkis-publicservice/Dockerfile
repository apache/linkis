FROM nm.hub.com/luban/linkis-base:1.0.4

WORKDIR /opt/linkis

COPY target/out/ /opt/linkis/linkis-publicservice/

ENTRYPOINT ["linkis-publicservice/bin/startup.sh"]