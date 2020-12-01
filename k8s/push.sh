#!/bin/bash

docker push $HARBOR_LINKIS/linkis:linkis-bml-0.10.0

docker push $HARBOR_LINKIS/linkis:linkis-dsm-server-0.10.0

docker push $HARBOR_LINKIS/linkis:linkis-mdm-server-0.10.0
docker push $HARBOR_LINKIS/linkis:linkis-mdm-service-mysql-0.10.0
docker push $HARBOR_LINKIS/linkis:linkis-mdm-service-es-0.10.0
docker push $HARBOR_LINKIS/linkis:linkis-mdm-service-hive-0.10.0

docker push $HARBOR_LINKIS/linkis:linkis-gateway-0.10.0

docker push $HARBOR_LINKIS/linkis:linkis-resourcemanager-0.10.0

docker push $HARBOR_LINKIS/linkis:linkis-cs-server-0.10.0

docker push $HARBOR_LINKIS/linkis:linkis-metadata0.10.0

docker push $HARBOR_LINKIS/linkis:linkis-publicservice0.10.0

docker push $HARBOR_LINKIS/linkis:linkis-ujes-spark-enginemanager-0.10.0
docker push $HARBOR_LINKIS/linkis:linkis-ujes-spark-entrance-0.10.0

docker push $HARBOR_LINKIS/linkis:linkis-ujes-hive-enginemanager-0.10.0
docker push $HARBOR_LINKIS/linkis:linkis-ujes-hive-entrance-0.10.0

docker push $HARBOR_LINKIS/linkis:linkis-ujes-python-enginemanager-0.10.0
docker push $HARBOR_LINKIS/linkis:linkis-ujes-python-entrance-0.10.0

docker push $HARBOR_LINKIS/linkis:linkis-ujes-pipeline-enginemanager-0.10.0
docker push $HARBOR_LINKIS/linkis:linkis-ujes-pipeline-entrance-0.10.0

docker push $HARBOR_LINKIS/linkis:linkis-ujes-jdbc-enginemanager-0.10.0

docker push $HARBOR_LINKIS/linkis:linkis-ujes-mlsql-entrance-0.10.0

docker push $HARBOR_LINKIS/linkis:linkis-ujes-shell-enginemanager-0.10.0
docker push $HARBOR_LINKIS/linkis:linkis-ujes-shell-entrance-0.10.0


