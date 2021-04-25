FROM nm.hub.com/luban/linkis-base:1.0.4

WORKDIR /opt/linkis

COPY target/out/ /opt/linkis/linkis-eureka/
COPY target/*.jar /opt/linkis/linkis-eureka/lib/

ENTRYPOINT ["linkis-eureka/bin/startup.sh"]