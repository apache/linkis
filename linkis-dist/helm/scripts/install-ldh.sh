#!/usr/bin/env bash
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

. ${WORK_DIR}/common.sh

set -e

USING_KIND=${1:-false}
LDH_VERSION=${LDH_VERSION-${LINKIS_IMAGE_TAG}}
echo "# LDH version: ${LINKIS_IMAGE_TAG}"

# load image
if [[ "X${USING_KIND}" == "Xtrue" ]]; then
  echo "# Loading LDH image ..."
  kind load docker-image linkis-ldh:${LINKIS_IMAGE_TAG} --name ${KIND_CLUSTER_NAME}
fi

# deploy LDH
echo "# Deploying LDH ..."
set +e
x=`kubectl get ns ldh 2> /dev/null`
set -e
if [[ "X${x}" == "X" ]]; then
  kubectl create ns ldh
fi
kubectl apply -n ldh -f ${RESOURCE_DIR}/ldh/configmaps

LDH_VERSION=${LDH_VERSION} envsubst < ${RESOURCE_DIR}/ldh/ldh.yaml | kubectl apply -n ldh -f -
