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

ACTION=$1

LINKIS_KUBE_NAMESPACE=linkis
LINKIS_INSTANCE_NAME=linkis-demo

LINKIS_PORT_MAP_WEB="8087:8087"
LINKIS_PORT_MAP_MG_EUREKA="5001:5005"
LINKIS_PORT_MAP_MG_GATEWAY="5002:5005"

LINKIS_PORT_MAP_PS_CS="5003:5005"
LINKIS_PORT_MAP_PS_PUBLICSERVICE="5004:5005"
LINKIS_PORT_MAP_PS_METADATAQUERY="5005:5005"
LINKIS_PORT_MAP_PS_DATASOURCEMANAGER="5006:5005"

LINKIS_PORT_MAP_CG_LINKISMANAGER="5007:5005"
LINKIS_PORT_MAP_CG_ENTRANCE="5008:5005"
LINKIS_PORT_MAP_CG_ENGINECONNMANAGER="5009:5005"
LINKIS_PORT_MAP_CG_ENGINEPLUGIN="5010:5005"

start_port_forward() {
  component_name=$1
  port_map=$2
  echo "- starting port-forwad for [${component_name}] with mapping [local->${port_map}->pod] ..."
  POD_NAME=`kubectl get pods -n ${LINKIS_KUBE_NAMESPACE} -l app.kubernetes.io/instance=${LINKIS_INSTANCE_NAME}-${component_name} -o jsonpath='{.items[0].metadata.name}'`
  kubectl port-forward -n ${LINKIS_KUBE_NAMESPACE} pod/${POD_NAME} ${port_map} --address='0.0.0.0' >/dev/null &
}

stop_port_forward() {
  component_name=$1
  port_map=$2
  echo "- stopping port-forward for [${component_name}] with mapping [local->${port_map}->pod] ..."

  pid=`ps aux |grep "port-forward" | grep " ${LINKIS_KUBE_NAMESPACE} " | grep "${component_name}" | grep "${port_map}" | awk -F ' ' '{print $2}'`
  if [ "X$pid" != "X" ]; then
    kill -9 $pid
  fi
}

start_port_forward_all() {
  start_port_forward web                    ${LINKIS_PORT_MAP_WEB}
  
  start_port_forward mg-eureka              ${LINKIS_PORT_MAP_MG_EUREKA}
  start_port_forward mg-gateway             ${LINKIS_PORT_MAP_MG_GATEWAY}
  
  start_port_forward ps-cs                  ${LINKIS_PORT_MAP_PS_CS}
  start_port_forward ps-publicservice       ${LINKIS_PORT_MAP_PS_PUBLICSERVICE}
  start_port_forward ps-metadataquery       ${LINKIS_PORT_MAP_PS_METADATAQUERY}
  start_port_forward ps-data-source-manager ${LINKIS_PORT_MAP_PS_DATASOURCEMANAGER}
  
  start_port_forward cg-linkismanager       ${LINKIS_PORT_MAP_CG_LINKISMANAGER}
  start_port_forward cg-entrance            ${LINKIS_PORT_MAP_CG_ENTRANCE}
  start_port_forward cg-engineconnmanager   ${LINKIS_PORT_MAP_CG_ENGINECONNMANAGER}
  start_port_forward cg-engineplugin        ${LINKIS_PORT_MAP_CG_ENGINEPLUGIN}
}

stop_port_forward_all() {
  stop_port_forward web                    ${LINKIS_PORT_MAP_WEB}
  
  stop_port_forward mg-eureka              ${LINKIS_PORT_MAP_MG_EUREKA}
  stop_port_forward mg-gateway             ${LINKIS_PORT_MAP_MG_GATEWAY}
  
  stop_port_forward ps-cs                  ${LINKIS_PORT_MAP_PS_CS}
  stop_port_forward ps-publicservice       ${LINKIS_PORT_MAP_PS_PUBLICSERVICE}
  stop_port_forward ps-metadataquery       ${LINKIS_PORT_MAP_PS_METADATAQUERY}
  stop_port_forward ps-data-source-manager ${LINKIS_PORT_MAP_PS_DATASOURCEMANAGER}
  
  stop_port_forward cg-linkismanager       ${LINKIS_PORT_MAP_CG_LINKISMANAGER}
  stop_port_forward cg-entrance            ${LINKIS_PORT_MAP_CG_ENTRANCE}
  stop_port_forward cg-engineconnmanager   ${LINKIS_PORT_MAP_CG_ENGINECONNMANAGER}
  stop_port_forward cg-engineplugin        ${LINKIS_PORT_MAP_CG_ENGINEPLUGIN}
}

case $ACTION in
  "start")
    start_port_forward_all
    ;;
  "stop")
    stop_port_forward_all
    ;;
  "list")
    ps aux |grep "port-forward" | grep " ${LINKIS_KUBE_NAMESPACE} " | grep "${LINKIS_INSTANCE_NAME}"
    ;;
  *)
    echo "invalid arguments, only start,stop,list are accepted"
    exit -1
    ;;
esac
