#!/bin/bash
#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
# http://www.apache.org/licenses/LICENSE-2.0
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

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
