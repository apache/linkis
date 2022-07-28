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
#

WORK_DIR=`cd $(dirname $0); pwd -P`
PROJECT_ROOT=${WORK_DIR}/../..
RESOURCE_DIR=${WORK_DIR}/resources
TMP_DIR=`mktemp -d -t kind-XXXXX`

set -e

KIND_CLUSTER_NAME=${KIND_CLUSTER_NAME:-test-helm}
MYSQL_VERSION=${MYSQL_VERSION:-5.7}

# evaluate project version
PROJECT_VERSION=`cd ${PROJECT_ROOT} \
   && MAVEN_OPTS="-Dorg.slf4j.simpleLogger.defaultLogLevel=WARN -Dorg.slf4j.simpleLogger.log.org.apache.maven.plugins.help=INFO" \
   mvn help:evaluate -o -Dexpression=project.version | tail -1`
echo "# Project version: ${PROJECT_VERSION}"

# create kind cluster
echo "# Creating KinD cluster ..."
# create data dir for KinD cluster
KIND_CLUSTER_HOST_PATH=${TMP_DIR}/data
mkdir -p ${KIND_CLUSTER_HOST_PATH}
# create kind cluster conf
KIND_CLUSTER_CONF_TPL=${RESOURCE_DIR}/kind-cluster.yaml
KIND_CLUSTER_CONF_FILE=${TMP_DIR}/kind-cluster.yaml
KIND_CLUSTER_HOST_PATH=${KIND_CLUSTER_HOST_PATH} envsubst < ${KIND_CLUSTER_CONF_TPL} > ${KIND_CLUSTER_CONF_FILE}

echo "- kind cluster config: ${KIND_CLUSTER_CONF_FILE}"
cat ${KIND_CLUSTER_CONF_FILE}
kind create cluster --name ${KIND_CLUSTER_NAME} --config ${KIND_CLUSTER_CONF_FILE}

# load images
echo "# Loading images into KinD cluster ..."
kind load docker-image linkis:${PROJECT_VERSION} --name ${KIND_CLUSTER_NAME}
kind load docker-image linkis-web:${PROJECT_VERSION} --name ${KIND_CLUSTER_NAME}
kind load docker-image mysql:${MYSQL_VERSION} --name ${KIND_CLUSTER_NAME}

if [ "X${WITH_LDH}" == "Xtrue" ]; then
  kind load docker-image linkis-ldh:${PROJECT_VERSION} --name ${KIND_CLUSTER_NAME}
fi
