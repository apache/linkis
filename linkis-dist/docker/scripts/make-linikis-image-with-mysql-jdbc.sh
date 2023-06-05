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

. ${WORK_DIR}/utils.sh

IMAGE_NAME=${IMAGE_NAME:-linkis:with-jdbc}
LINKIS_IMAGE=${LINKIS_IMAGE:-ghcr.io/apache/linkis/linkis:latest}
LINKIS_HOME=${LINKIS_HOME:-/opt/linkis}
MYSQL_JDBC_VERSION=${MYSQL_JDBC_VERSION:-8.0.28}
MYSQL_JDBC_FILENAME=mysql-connector-java-${MYSQL_JDBC_VERSION}.jar

BUILD_DIR=${WORK_DIR}/../../tmp

echo "#          build dir: ${BUILD_DIR}"
echo "#         base image: ${LINKIS_IMAGE}"
echo "# mysql jdbc version: ${MYSQL_JDBC_VERSION}"


# /home/runner/work/linkis/linkis/linkis-dist/docker/scripts/

echo "try to exec: docker build -f ${WORK_DIR}/../linkis-with-mysql-jdbc.Dockerfile \
  -t ${IMAGE_NAME} \
  --build-arg LINKIS_IMAGE=${LINKIS_IMAGE} \
  --build-arg LINKIS_HOME=${LINKIS_HOME} \
  --build-arg MYSQL_JDBC_VERSION=${MYSQL_JDBC_VERSION} \
  ${BUILD_DIR}"

docker build -f ${WORK_DIR}/../linkis-with-mysql-jdbc.Dockerfile \
  -t ${IMAGE_NAME} \
  --build-arg LINKIS_IMAGE=${LINKIS_IMAGE} \
  --build-arg LINKIS_HOME=${LINKIS_HOME} \
  --build-arg MYSQL_JDBC_VERSION=${MYSQL_JDBC_VERSION} \
  ${BUILD_DIR}

echo "# done, image: ${IMAGE_NAME}"
