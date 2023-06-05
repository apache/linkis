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

. ${WORK_DIR}/common.sh

KUBE_NAMESPACE=${1:-linkis}
HELM_RELEASE_NAME=${2:-linkis-demo}
LOCAL_MODE=${3:-true}
USING_KIND=${4:-false}

if [[ "X${HELM_DEBUG}" == "Xtrue" ]]; then
  # template helm charts
  helm template --namespace ${KUBE_NAMESPACE} -f ${LINKIS_CHART_DIR}/values.yaml ${HELM_RELEASE_NAME} ${LINKIS_CHART_DIR}
else
  # create hadoop configs
  if [[ "X${WITH_LDH}" == "Xtrue" ]]; then
    kubectl apply -n ${KUBE_NAMESPACE} -f ${RESOURCE_DIR}/ldh/configmaps
  fi
  # load image
  if [[ "X${USING_KIND}" == "Xtrue" ]]; then
    echo "# Loading Linkis image ..."
    kind load docker-image linkis:${LINKIS_IMAGE_TAG} --name ${KIND_CLUSTER_NAME}
    kind load docker-image linkis-web:${LINKIS_IMAGE_TAG} --name ${KIND_CLUSTER_NAME}
  fi
  # install helm charts
  echo "# Installing linkis, image tag=${LINKIS_IMAGE_TAG},local mode=${LOCAL_MODE} ..."
  helm install --create-namespace --namespace ${KUBE_NAMESPACE} \
    -f ${LINKIS_CHART_DIR}/values.yaml \
    --set image.tag=${LINKIS_IMAGE_TAG},linkis.featureGates.localMode=${LOCAL_MODE} \
    ${HELM_RELEASE_NAME} ${LINKIS_CHART_DIR}
fi
