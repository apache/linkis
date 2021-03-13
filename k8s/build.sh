#!/bin/bash

# docker build -t $HARBOR_LINKIS/linkis:emr-base-spark2.4.4 -f emr-base/Dockerfile .

docker build -t $HARBOR_LINKIS/linkis:linkis-bml-1.0.0-RC1 -f bml/bmlserver/Dockerfile bml/bmlserver

docker build -t $HARBOR_LINKIS/linkis:linkis-dsm-server-1.0.0-RC1 -f datasource/datasourcemanager/server/Dockerfile datasource/datasourcemanager/server

docker build -t $HARBOR_LINKIS/linkis:linkis-mdm-server-1.0.0-RC1 -f datasource/metadatamanager/server/Dockerfile datasource/metadatamanager/server
docker build -t $HARBOR_LINKIS/linkis:linkis-mdm-service-mysql-1.0.0-RC1 -f datasource/metadatamanager/service/mysql/Dockerfile datasource/metadatamanager/service/mysql
docker build -t $HARBOR_LINKIS/linkis:linkis-mdm-service-es-1.0.0-RC1 -f datasource/metadatamanager/service/elasticsearch/Dockerfile datasource/metadatamanager/service/elasticsearch
docker build -t $HARBOR_LINKIS/linkis:linkis-mdm-service-hive-1.0.0-RC1 -f datasource/metadatamanager/service/hive/Dockerfile datasource/metadatamanager/service/hive

docker build -t $HARBOR_LINKIS/linkis:linkis-gateway-1.0.0-RC1 -f gateway/gateway-ujes-support/Dockerfile gateway/gateway-ujes-support

docker build -t $HARBOR_LINKIS/linkis:linkis-resourcemanager-1.0.0-RC1 -f resourceManager/resourcemanagerserver/Dockerfile resourceManager/resourcemanagerserver

docker build -t $HARBOR_LINKIS/linkis:linkis-cs-server-1.0.0-RC1 -f contextservice/cs-server/Dockerfile contextservice/cs-server

docker build -t $HARBOR_LINKIS/linkis:linkis-metadata-1.0.0-RC1 -f metadata/Dockerfile metadata

docker build -t $HARBOR_LINKIS/linkis:linkis-publicservice-1.0.0-RC1 -f publicService/Dockerfile publicService

docker build -t $HARBOR_LINKIS/linkis:linkis-ujes-spark-enginemanager-1.0.0-RC1 -f ujes/definedEngines/spark/enginemanager/Dockerfile ujes/definedEngines/spark/enginemanager
docker build -t $HARBOR_LINKIS/linkis:linkis-ujes-spark-entrance-1.0.0-RC1 -f ujes/definedEngines/spark/entrance/Dockerfile ujes/definedEngines/spark/entrance

docker build -t $HARBOR_LINKIS/linkis:linkis-ujes-hive-enginemanager-1.0.0-RC1 -f ujes/definedEngines/hive/enginemanager/Dockerfile ujes/definedEngines/hive/enginemanager
docker build -t $HARBOR_LINKIS/linkis:linkis-ujes-hive-entrance-1.0.0-RC1 -f ujes/definedEngines/hive/entrance/Dockerfile ujes/definedEngines/hive/entrance

docker build -t $HARBOR_LINKIS/linkis:linkis-ujes-python-enginemanager-1.0.0-RC1 -f ujes/definedEngines/python/enginemanager/Dockerfile ujes/definedEngines/python/enginemanager
docker build -t $HARBOR_LINKIS/linkis:linkis-ujes-python-entrance-1.0.0-RC1 -f ujes/definedEngines/python/entrance/Dockerfile ujes/definedEngines/python/entrance

docker build -t $HARBOR_LINKIS/linkis:linkis-ujes-pipeline-enginemanager-1.0.0-RC1 -f ujes/definedEngines/pipeline/enginemanager/Dockerfile ujes/definedEngines/pipeline/enginemanager
docker build -t $HARBOR_LINKIS/linkis:linkis-ujes-pipeline-entrance-1.0.0-RC1 -f ujes/definedEngines/pipeline/entrance/Dockerfile ujes/definedEngines/pipeline/entrance

docker build -t $HARBOR_LINKIS/linkis:linkis-ujes-jdbc-enginemanager-1.0.0-RC1 -f ujes/definedEngines/jdbc/entrance/Dockerfile ujes/definedEngines/jdbc/entrance

docker build -t $HARBOR_LINKIS/linkis:linkis-ujes-mlsql-entrance-1.0.0-RC1 -f ujes/definedEngines/mlsql/entrance/Dockerfile ujes/definedEngines/mlsql/entrance

docker build -t $HARBOR_LINKIS/linkis:linkis-ujes-shell-enginemanager-1.0.0-RC1 -f ujes/definedEngines/shell/entrance/Dockerfile ujes/definedEngines/shell/entrance
docker build -t $HARBOR_LINKIS/linkis:linkis-ujes-shell-entrance-1.0.0-RC1 -f ujes/definedEngines/shell/enginemanager/Dockerfile ujes/definedEngines/shell/enginemanager


