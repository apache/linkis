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
## Temporarily solve the problem that the spark submit client fails
## and resolve the domain name of the yarn callback ecm when executing the spark on yarn task

WORK_DIR=`cd $(dirname $0); pwd -P`

## copy spark resource from ldh to linkis-cg-engineconnmanager

LDH_POD_NAME=`kubectl get pods -n ldh -l app=ldh -o jsonpath='{.items[0].metadata.name}'`
ECM_POD_NAME=`kubectl get pods -n linkis -l app.kubernetes.io/instance=linkis-demo-cg-engineconnmanager -o jsonpath='{.items[0].metadata.name}'`

kubectl exec -n ldh ${LDH_POD_NAME} -- tar -C /opt -cf - ldh | \
kubectl exec -i -n linkis ${ECM_POD_NAME} -- tar -C /opt -xf - --no-same-owner

kubectl exec -n linkis ${ECM_POD_NAME} -- bash -c "sudo mkdir -p /appcom/Install && sudo chmod 0777 /appcom/Install && ln -s /opt/ldh/current/spark /appcom/Install/spark"
kubectl exec -n linkis ${ECM_POD_NAME} -- bash -c "echo 'export SPARK_HOME=/opt/ldh/current/spark' |sudo tee --append /etc/profile"
kubectl exec -n linkis ${ECM_POD_NAME} -- bash -c "echo 'export PATH=\$SPARK_HOME/bin:\$PATH' |sudo tee --append  /etc/profile"
kubectl exec -n linkis ${ECM_POD_NAME} -- bash -c "source /etc/profile"

# add ecm dns for ldh pod
ECM_POD_IP=`kubectl get pods -n linkis -l app.kubernetes.io/instance=linkis-demo-cg-engineconnmanager -o jsonpath='{.items[0].status.podIP}'`

ECM_POD_SUBDOMAIN=`kubectl get pods -n linkis -l app.kubernetes.io/instance=linkis-demo-cg-engineconnmanager -o jsonpath='{.items[0].spec.subdomain}'`

ECM_DNS="${ECM_POD_IP}   ${ECM_POD_NAME}.${ECM_POD_SUBDOMAIN}.linkis.svc.cluster.local"

kubectl exec -n ldh ${LDH_POD_NAME} -- bash -c "echo ${ECM_DNS} |sudo tee --append  /etc/hosts"
