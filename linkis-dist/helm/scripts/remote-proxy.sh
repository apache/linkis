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

ACTION=$1

DEBUG=$2

LINKIS_KUBE_NAMESPACE=linkis
LINKIS_INSTANCE_NAME=linkis-demo

LINKIS_PORT_MAP_WEB="8088:8088"
LINKIS_PORT_MAP_MG_EUREKA="20303:20303"

LINKIS_PORT_MAP_MG_GATEWAY="9001:9001"

#debug port

LINKIS_DEBUG_PORT_MAP_MG_EUREKA="22101:5005"
LINKIS_DEBUG_PORT_MAP_MG_GATEWAY="22102:5005"

LINKIS_DEBUG_PORT_MAP_PS_PUBLICSERVICE="22103:5005"

LINKIS_DEBUG_PORT_MAP_CG_LINKISMANAGER="22104:5005"
LINKIS_DEBUG_PORT_MAP_CG_ENTRANCE="22105:5005"
LINKIS_DEBUG_PORT_MAP_CG_ENGINECONNMANAGER="22106:5005"
LINKIS_DEBUG_PORT_MAP_CG_ENGINEPLUGIN="22107:5005"


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
  DEBUG=$1

  start_port_forward web                     ${LINKIS_PORT_MAP_WEB}
  start_port_forward mg-eureka               ${LINKIS_PORT_MAP_MG_EUREKA}
  start_port_forward mg-gateway              ${LINKIS_PORT_MAP_MG_GATEWAY}

  if [ "${DEBUG}" == "true" ]; then

    start_port_forward mg-eureka              ${LINKIS_DEBUG_PORT_MAP_MG_EUREKA}
    start_port_forward mg-gateway             ${LINKIS_DEBUG_PORT_MAP_MG_GATEWAY}

    start_port_forward ps-publicservice       ${LINKIS_DEBUG_PORT_MAP_PS_PUBLICSERVICE}

    start_port_forward cg-linkismanager       ${LINKIS_DEBUG_PORT_MAP_CG_LINKISMANAGER}
    start_port_forward cg-entrance            ${LINKIS_DEBUG_PORT_MAP_CG_ENTRANCE}
    start_port_forward cg-engineconnmanager   ${LINKIS_DEBUG_PORT_MAP_CG_ENGINECONNMANAGER}
    start_port_forward cg-engineplugin        ${LINKIS_DEBUG_PORT_MAP_CG_ENGINEPLUGIN}
  fi
}

stop_port_forward_all() {
  DEBUG=$1

  stop_port_forward web                     ${LINKIS_PORT_MAP_WEB}
  stop_port_forward mg-eureka               ${LINKIS_PORT_MAP_MG_EUREKA}
  stop_port_forward mg-gateway              ${LINKIS_PORT_MAP_MG_GATEWAY}

  if [ "${DEBUG}" == "true" ]; then

    stop_port_forward mg-eureka              ${LINKIS_DEBUG_PORT_MAP_MG_EUREKA}
    stop_port_forward mg-gateway             ${LINKIS_DEBUG_PORT_MAP_MG_GATEWAY}

    stop_port_forward ps-publicservice       ${LINKIS_DEBUG_PORT_MAP_PS_PUBLICSERVICE}

    stop_port_forward cg-linkismanager       ${LINKIS_DEBUG_PORT_MAP_CG_LINKISMANAGER}
    stop_port_forward cg-entrance            ${LINKIS_DEBUG_PORT_MAP_CG_ENTRANCE}
    stop_port_forward cg-engineconnmanager   ${LINKIS_DEBUG_PORT_MAP_CG_ENGINECONNMANAGER}
    stop_port_forward cg-engineplugin        ${LINKIS_DEBUG_PORT_MAP_CG_ENGINEPLUGIN}

  fi


}

case $ACTION in
  "start")
    start_port_forward_all
    ;;
  "stop")
    stop_port_forward_all
    ;;
  "start-with-debug")
    start_port_forward_all true
    ;;
  "stop-with-debug")
    stop_port_forward_all true
    ;;
  "list")
    ps aux |grep "port-forward" | grep " ${LINKIS_KUBE_NAMESPACE} " | grep "${LINKIS_INSTANCE_NAME}"
    ;;
  *)
    echo "invalid arguments, only start,start-with-debug,stop,stop-with-debug,list are accepted"
    exit -1
    ;;
esac
