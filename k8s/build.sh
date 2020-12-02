#!/bin/bash

# docker build -t $HARBOR_LINKIS/linkis:emr-base-spark2.4.4 -f emr-base/Dockerfile .

docker build -t $HARBOR_LINKIS/linkis:linkis-bml-0.10.0 -f bml/bmlserver/Dockerfile bml/bmlserver

docker build -t $HARBOR_LINKIS/linkis:linkis-dsm-server-0.10.0 -f datasource/datasourcemanager/server/Dockerfile datasource/datasourcemanager/server

docker build -t $HARBOR_LINKIS/linkis:linkis-mdm-server-0.10.0 -f datasource/metadatamanager/server/Dockerfile datasource/metadatamanager/server
docker build -t $HARBOR_LINKIS/linkis:linkis-mdm-service-mysql-0.10.0 -f datasource/metadatamanager/service/mysql/Dockerfile datasource/metadatamanager/service/mysql
docker build -t $HARBOR_LINKIS/linkis:linkis-mdm-service-es-0.10.0 -f datasource/metadatamanager/service/elasticsearch/Dockerfile datasource/metadatamanager/service/elasticsearch
docker build -t $HARBOR_LINKIS/linkis:linkis-mdm-service-hive-0.10.0 -f datasource/metadatamanager/service/hive/Dockerfile datasource/metadatamanager/service/hive

docker build -t $HARBOR_LINKIS/linkis:linkis-gateway-0.10.0 -f gateway/gateway-ujes-support/Dockerfile gateway/gateway-ujes-support

docker build -t $HARBOR_LINKIS/linkis:linkis-resourcemanager-0.10.0 -f resourceManager/resourcemanagerserver/Dockerfile resourceManager/resourcemanagerserver

docker build -t $HARBOR_LINKIS/linkis:linkis-cs-server-0.10.0 -f contextservice/cs-server/Dockerfile contextservice/cs-server

docker build -t $HARBOR_LINKIS/linkis:linkis-metadata-0.10.0 -f metadata/Dockerfile metadata

docker build -t $HARBOR_LINKIS/linkis:linkis-publicservice-0.10.0 -f publicService/Dockerfile publicService

docker build -t $HARBOR_LINKIS/linkis:linkis-ujes-spark-enginemanager-0.10.0 -f ujes/definedEngines/spark/enginemanager/Dockerfile ujes/definedEngines/spark/enginemanager
docker build -t $HARBOR_LINKIS/linkis:linkis-ujes-spark-entrance-0.10.0 -f ujes/definedEngines/spark/entrance/Dockerfile ujes/definedEngines/spark/entrance

docker build -t $HARBOR_LINKIS/linkis:linkis-ujes-hive-enginemanager-0.10.0 -f ujes/definedEngines/hive/enginemanager/Dockerfile ujes/definedEngines/hive/enginemanager
docker build -t $HARBOR_LINKIS/linkis:linkis-ujes-hive-entrance-0.10.0 -f ujes/definedEngines/hive/entrance/Dockerfile ujes/definedEngines/hive/entrance

docker build -t $HARBOR_LINKIS/linkis:linkis-ujes-python-enginemanager-0.10.0 -f ujes/definedEngines/python/enginemanager/Dockerfile ujes/definedEngines/python/enginemanager
docker build -t $HARBOR_LINKIS/linkis:linkis-ujes-python-entrance-0.10.0 -f ujes/definedEngines/python/entrance/Dockerfile ujes/definedEngines/python/entrance

docker build -t $HARBOR_LINKIS/linkis:linkis-ujes-pipeline-enginemanager-0.10.0 -f ujes/definedEngines/pipeline/enginemanager/Dockerfile ujes/definedEngines/pipeline/enginemanager
docker build -t $HARBOR_LINKIS/linkis:linkis-ujes-pipeline-entrance-0.10.0 -f ujes/definedEngines/pipeline/entrance/Dockerfile ujes/definedEngines/pipeline/entrance

docker build -t $HARBOR_LINKIS/linkis:linkis-ujes-jdbc-enginemanager-0.10.0 -f ujes/definedEngines/jdbc/entrance/Dockerfile ujes/definedEngines/jdbc/entrance

docker build -t $HARBOR_LINKIS/linkis:linkis-ujes-mlsql-entrance-0.10.0 -f ujes/definedEngines/mlsql/entrance/Dockerfile ujes/definedEngines/mlsql/entrance

docker build -t $HARBOR_LINKIS/linkis:linkis-ujes-shell-enginemanager-0.10.0 -f ujes/definedEngines/shell/entrance/Dockerfile ujes/definedEngines/shell/entrance
docker build -t $HARBOR_LINKIS/linkis:linkis-ujes-shell-entrance-0.10.0 -f ujes/definedEngines/shell/enginemanager/Dockerfile ujes/definedEngines/shell/enginemanager


