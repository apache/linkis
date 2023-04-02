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

LDH_POD_NAME=`kubectl get pods -n ldh -l app=ldh    -o jsonpath='{.items[0].metadata.name}'`
kubectl cp -n ldh ${LDH_POD_NAME}:/opt/ldh/ ./ldh

ECM_POD_NAME=`kubectl get pods -n linkis -l app.kubernetes.io/instance=linkis-demo-cg-engineconnmanager -o jsonpath='{.items[0].metadata.name}'`
kubectl cp ./ldh  -n linkis ${ECM_POD_NAME}:/opt/ ;


kubectl exec -it -n linkis ${ECM_POD_NAME} -- bash -c "chmod +x /opt/ldh/1.3.0/spark-3.2.1-bin-hadoop3.2/bin/*"
kubectl exec -it -n linkis ${ECM_POD_NAME} -- bash -c "ln -s /opt/ldh/1.3.0/spark-3.2.1-bin-hadoop3.2 /opt/ldh/current/spark"
kubectl exec -it -n linkis ${ECM_POD_NAME} -- bash -c "ln -s /opt/ldh/1.3.0/hadoop-3.3.4 /opt/ldh/current/hadoop"
kubectl exec -it -n linkis ${ECM_POD_NAME} -- bash -c "ln -s /opt/ldh/1.3.0/apache-hive-3.1.3-bin /opt/ldh/current/hive"


kubectl exec -it -n linkis ${ECM_POD_NAME} -- bash -c "echo 'export SPARK_HOME=/opt/ldh/current/spark' |sudo tee --append /etc/profile"
kubectl exec -it -n linkis ${ECM_POD_NAME} -- bash -c "echo 'export PATH=\$SPARK_HOME/bin:\$PATH' |sudo tee --append  /etc/profile"
kubectl exec -it -n linkis ${ECM_POD_NAME} -- bash -c "source /etc/profile"

# add ecm dns for ldh pod
ECM_POD_IP=`kubectl get pods -n linkis -l app.kubernetes.io/instance=linkis-demo-cg-engineconnmanager -o jsonpath='{.items[0].status.podIP}'`

ECM_POD_SUBDOMAIN=`kubectl get pods -n linkis -l app.kubernetes.io/instance=linkis-demo-cg-engineconnmanager -o jsonpath='{.items[0].spec.subdomain}'`

ECM_DNS="${ECM_POD_IP}   ${ECM_POD_NAME}.${ECM_POD_SUBDOMAIN}.linkis.svc.cluster.local"

kubectl exec -it -n ldh ${LDH_POD_NAME} -- bash -c "echo ${ECM_DNS} |sudo tee --append  /etc/hosts"


rm -rf ldh;