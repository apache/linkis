#!/bin/bash

docker push $HARBOR_LINKIS/linkis:linkis-bml-1.0.0-RC1

docker push $HARBOR_LINKIS/linkis:linkis-dsm-server-1.0.0-RC1

docker push $HARBOR_LINKIS/linkis:linkis-mdm-server-1.0.0-RC1
docker push $HARBOR_LINKIS/linkis:linkis-mdm-service-mysql-1.0.0-RC1
docker push $HARBOR_LINKIS/linkis:linkis-mdm-service-es-1.0.0-RC1
docker push $HARBOR_LINKIS/linkis:linkis-mdm-service-hive-1.0.0-RC1

docker push $HARBOR_LINKIS/linkis:linkis-gateway-1.0.0-RC1

docker push $HARBOR_LINKIS/linkis:linkis-resourcemanager-1.0.0-RC1

docker push $HARBOR_LINKIS/linkis:linkis-cs-server-1.0.0-RC1

docker push $HARBOR_LINKIS/linkis:linkis-metadata-1.0.0-RC1

docker push $HARBOR_LINKIS/linkis:linkis-publicservice-1.0.0-RC1

docker push $HARBOR_LINKIS/linkis:linkis-ujes-spark-enginemanager-1.0.0-RC1
docker push $HARBOR_LINKIS/linkis:linkis-ujes-spark-entrance-1.0.0-RC1

docker push $HARBOR_LINKIS/linkis:linkis-ujes-hive-enginemanager-1.0.0-RC1
docker push $HARBOR_LINKIS/linkis:linkis-ujes-hive-entrance-1.0.0-RC1

docker push $HARBOR_LINKIS/linkis:linkis-ujes-python-enginemanager-1.0.0-RC1
docker push $HARBOR_LINKIS/linkis:linkis-ujes-python-entrance-1.0.0-RC1

docker push $HARBOR_LINKIS/linkis:linkis-ujes-pipeline-enginemanager-1.0.0-RC1
docker push $HARBOR_LINKIS/linkis:linkis-ujes-pipeline-entrance-1.0.0-RC1

docker push $HARBOR_LINKIS/linkis:linkis-ujes-jdbc-enginemanager-1.0.0-RC1

docker push $HARBOR_LINKIS/linkis:linkis-ujes-mlsql-entrance-1.0.0-RC1

docker push $HARBOR_LINKIS/linkis:linkis-ujes-shell-enginemanager-1.0.0-RC1
docker push $HARBOR_LINKIS/linkis:linkis-ujes-shell-entrance-1.0.0-RC1


