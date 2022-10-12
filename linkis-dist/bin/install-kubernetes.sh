#!/bin/bash
docker pull ghcr.io/apache/incubator-linkis/linkis-ldh:latest
docker pull ghcr.io/apache/incubator-linkis/linkis:latest
docker pull ghcr.io/apache/incubator-linkis/linkis-web:latest
docker tag ghcr.io/apache/incubator-linkis/linkis:latest linkis:dev
docker tag ghcr.io/apache/incubator-linkis/linkis-web:latest linkis-web:dev
docker tag ghcr.io/apache/incubator-linkis/linkis-ldh:latest linkis-ldh:dev
../docker/scripts/make-linikis-image-with-mysql-jdbc.sh
docker tag linkis:with-jdbc linkis:dev
../helm/scripts/create-kind-cluster.sh
../helm/scripts/install-mysql.shl
../helm/scripts/install-ldh.sh
../helm/scripts/install-linkis.sh
kubectl get pods -A
