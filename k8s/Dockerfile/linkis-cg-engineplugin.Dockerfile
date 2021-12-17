FROM nm.hub.com/luban/linkis-base:1.0.5

WORKDIR /opt/linkis

COPY lib/linkis-commons/public-module/ /opt/linkis/public-module/
COPY lib/linkis-engineconn-plugins/ /opt/linkis/linkis-cg-engineplugin/plugins/
COPY lib/linkis-computation-governance/linkis-cg-engineplugin/ /opt/linkis/linkis-cg-engineplugin/lib/
COPY sbin/k8s/linkis-cg-engineplugin.sh /opt/linkis/linkis-cg-engineplugin/bin/startup.sh

ENTRYPOINT ["linkis-cg-engineplugin/bin/startup.sh"]