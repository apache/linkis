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

set -e

KIND_CLUSTER_NAME=test-helm
MYSQL_VERSION=5.7

# evaluate project version
PROJECT_VERSION=`cd ${PROJECT_ROOT} \
   && MAVEN_OPTS="-Dorg.slf4j.simpleLogger.defaultLogLevel=WARN -Dorg.slf4j.simpleLogger.log.org.apache.maven.plugins.help=INFO" \
   mvn help:evaluate -o -Dexpression=project.version | tail -1`
echo "# Project version: ${PROJECT_VERSION}"

# create kind cluster
echo "# Creating KinD cluster ..."
kind create cluster --name ${KIND_CLUSTER_NAME}

# load images
echo "# Loading images into KinD cluster ..."
kind load docker-image linkis:${PROJECT_VERSION} --name ${KIND_CLUSTER_NAME}
kind load docker-image mysql:${MYSQL_VERSION} --name ${KIND_CLUSTER_NAME}

# deploy mysql
echo "# Deploying MySQL ..."
kubectl create ns mysql
kubectl apply -n mysql -f ${RESOURCE_DIR}/mysql.yaml



