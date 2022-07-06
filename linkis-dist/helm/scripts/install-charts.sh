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
CHARTS_DIR_ROOT=${WORK_DIR}/../charts
LINKIS_CHART_DIR=${CHARTS_DIR_ROOT}/linkis

KUBE_NAMESPACE=${1:-linkis}
HELM_RELEASE_NAME=${2:-linkis-demo}

if [ "X${HELM_DEBUG}" == "Xtrue" ]; then
  # template helm charts
  helm template --namespace ${KUBE_NAMESPACE} -f ${LINKIS_CHART_DIR}/values.yaml ${HELM_RELEASE_NAME} ${LINKIS_CHART_DIR}
else
  # install helm charts
  helm install --create-namespace --namespace ${KUBE_NAMESPACE} -f ${LINKIS_CHART_DIR}/values.yaml ${HELM_RELEASE_NAME} ${LINKIS_CHART_DIR}
fi



