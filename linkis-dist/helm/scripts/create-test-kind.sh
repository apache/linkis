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
RESOURCE_DIR=${WORK_DIR}/resources

KIND_CLUSTER_NAME=test-helm

# create kind cluster
kind create cluster --name ${KIND_CLUSTER_NAME}

# load images
kind load docker-image linkis:1.3.0 --name ${KIND_CLUSTER_NAME}
kind load docker-image mysql:5.7 --name ${KIND_CLUSTER_NAME}

# deploy mysql
kubectl create ns mysql
kubectl apply -n mysql -f ${RESOURCE_DIR}/mysql.yaml

# install helm charts


