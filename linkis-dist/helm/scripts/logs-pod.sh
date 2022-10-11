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

COMPONENT_NAME=$1

LINKIS_KUBE_NAMESPACE=linkis
LINKIS_INSTANCE_NAME=linkis-demo

logs() {
  component_name=$1
  POD_NAME=`kubectl get pods -n ${LINKIS_KUBE_NAMESPACE} -l app.kubernetes.io/instance=${LINKIS_INSTANCE_NAME}-${component_name} -o jsonpath='{.items[0].metadata.name}'`
  kubectl logs -n ${LINKIS_KUBE_NAMESPACE} ${POD_NAME} -f
}

logs_ldh() {
  POD_NAME=`kubectl get pods -n ldh -l app=ldh    -o jsonpath='{.items[0].metadata.name}'`
  kubectl logs -n ldh ${POD_NAME} -f

}

logs_mysql() {

  POD_NAME=`kubectl get pods -n mysql -l app=mysql    -o jsonpath='{.items[0].metadata.name}'`
  kubectl logs  -n mysql ${POD_NAME} -f
}


if [ "${COMPONENT_NAME}" == "ldh" ]; then
  logs_ldh ${COMPONENT_NAME}
elif [ "${COMPONENT_NAME}" == "mysql" ]; then
  logs_mysql ${COMPONENT_NAME}
else
   logs ${COMPONENT_NAME}
fi


