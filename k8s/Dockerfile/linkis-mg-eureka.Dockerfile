FROM linkis-base:1.2.0

WORKDIR /opt/linkis

COPY lib/linkis-commons/public-module/ /opt/linkis/public-module/
COPY lib/linkis-spring-cloud-services/linkis-mg-eureka/ /opt/linkis/linkis-mg-eureka/lib/
COPY sbin/k8s/linkis-mg-eureka.sh /opt/linkis/linkis-mg-eureka/bin/startup.sh

ENTRYPOINT ["linkis-mg-eureka/bin/startup.sh"]
