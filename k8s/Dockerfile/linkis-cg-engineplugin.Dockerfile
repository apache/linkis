FROM linkis-base:1.2.0

WORKDIR /opt/linkis

COPY lib/linkis-commons/public-module/ /opt/linkis/public-module/
COPY lib/linkis-engineconn-plugins/ /opt/linkis/linkis-cg-engineplugin/plugins/
COPY lib/linkis-computation-governance/linkis-cg-engineplugin/ /opt/linkis/linkis-cg-engineplugin/lib/
COPY jars/mysql-connector-java-5.1.49.jar /opt/linkis/linkis-cg-engineplugin/lib/
COPY sbin/k8s/linkis-cg-engineplugin.sh /opt/linkis/linkis-cg-engineplugin/bin/startup.sh
RUN mkdir -p /appcom/Install/LinkisInstall/lib/linkis-engineconn-plugins


ENTRYPOINT ["linkis-cg-engineplugin/bin/startup.sh"]
