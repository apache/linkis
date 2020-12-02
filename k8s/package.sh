#!/bin/bash

mvn clean package -f bml/bmlserver/pom_k8s.xml

mvn clean package -f datasource/datasourcemanager/server/pom_k8s.xml

mvn clean package -f datasource/metadatamanager/server/pom_k8s.xml
mvn clean package -f datasource/metadatamanager/service/mysql/pom_k8s.xml
mvn clean package -f datasource/metadatamanager/service/elasticsearch/pom_k8s.xml
mvn clean package -f datasource/metadatamanager/service/hive/pom_k8s.xml

mvn clean package -f gateway/gateway-ujes-support/pom_k8s.xml

mvn clean package -f resourceManager/resourcemanagerserver/pom_k8s.xml

mvn clean package -f contextservice/cs-server/pom_k8s.xml

mvn clean package -f metadata/pom_k8s.xml

mvn clean package -f publicService/pom_k8s.xml

mvn clean package -f ujes/definedEngines/spark/enginemanager/pom_k8s.xml
mvn clean package -f ujes/definedEngines/spark/entrance/pom_k8s.xml

mvn clean package -f ujes/definedEngines/hive/enginemanager/pom_k8s.xml
mvn clean package -f ujes/definedEngines/hive/entrance/pom_k8s.xml

mvn clean package -f ujes/definedEngines/python/enginemanager/pom_k8s.xml
mvn clean package -f ujes/definedEngines/python/entrance/pom_k8s.xml

mvn clean package -f ujes/definedEngines/pipeline/enginemanager/pom_k8s.xml
mvn clean package -f ujes/definedEngines/pipeline/entrance/pom_k8s.xml

mvn clean package -f ujes/definedEngines/jdbc/entrance/pom_k8s.xml

mvn clean package -f ujes/definedEngines/mlsql/entrance/pom_k8s.xml

mvn clean package -f ujes/definedEngines/shell/entrance/pom_k8s.xml
mvn clean package -f ujes/definedEngines/shell/enginemanager/pom_k8s.xml

