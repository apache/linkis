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

POD_NAME=`kubectl get pods -n ldh -l app=ldh    -o jsonpath='{.items[0].metadata.name}'`


kubectl cp -n ldh $POD_NAME:/opt/ldh/ ./ldh


POD_NAME=`kubectl get pods -n linkis -l app.kubernetes.io/instance=linkis-demo-cg-engineconnmanager -o jsonpath='{.items[0].metadata.name}'`


kubectl cp ./ldh  -n linkis $POD_NAME:/opt/ ;

kubectl exec -it -n linkis ${POD_NAME} -- bash -c "chmod +x /opt/ldh/1.3.0/spark-2.4.3-bin-hadoop2.7/bin/*" ;

kubectl exec -it -n linkis ${POD_NAME} -- bash -c "ln -s /opt/ldh/1.3.0/spark-2.4.3-bin-hadoop2.7 /opt/ldh/current/spark" ;

kubectl exec -it -n linkis ${POD_NAME} -- bash -c "ln -s /opt/ldh/1.3.0/hadoop-2.7.2 /opt/ldh/current/hadoop" ;
kubectl exec -it -n linkis ${POD_NAME} -- bash -c "ln -s /opt/ldh/1.3.0/apache-hive-2.3.3-bin /opt/ldh/current/hive" ;

kubectl exec -it -n linkis ${POD_NAME} -- bash -c "sudo mkdir /home/hadoop; sudo chown hadoop:root /home/hadoop;  touch /home/hadoop/.bashrc" ;

kubectl exec -it -n linkis ${POD_NAME} -- bash -c "echo 'export SPARK_HOME=/opt/ldh/current/spark' >> /home/hadoop/.bashrc" ;

kubectl exec -it -n linkis ${POD_NAME} -- bash -c "echo 'export HADOOP_HOME=/opt/ldh/current/hadoop' >> /home/hadoop/.bashrc";

kubectl exec -it -n linkis ${POD_NAME} -- bash -c "echo 'export PATH=\$SPARK_HOME/bin:\$PATH' >> /home/hadoop/.bashrc";


kubectl exec -it -n linkis ${POD_NAME} -- bash -c "source /home/hadoop/.bashrc";

rm -rf ldh;


