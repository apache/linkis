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

shellDir=`dirname $0`
workDir=`cd ${shellDir}/..;pwd`
source ${workDir}/bin/common.sh
source ${workDir}/deploy-config/linkis-env.sh
source ${workDir}/deploy-config/db.sh

# --- Begin Check service function by zwk

function checkTrino(){
    TrinoDISAddress="echo $TRINO_DISCOVERY_URL|awk -F'\"' '{print $1}'|awk -F';' '{print $1}'"
    curl $TrinoDISAddress  > /dev/null 2>&1
    isSuccess "execute cmd: Trino service check"
}

function checkElasticSearch(){
    ESRestfulAddress="echo $ES_RESTFUL_URL|awk -F'\"' '{print $1}'|awk -F';' '{print $1}'"
    curl $ESRestfulAddress> /dev/null 2>&1
    isSuccess "execute cmd: ElasticSearch service check"
}

function checkFlink(){
    FlinkRestfulAddress="echo $Flink_RESTFUL_URL|awk -F'\"' '{print $1}'|awk -F';' '{print $1}'"
    curl $FlinkRestfulAddress> /dev/null 2>&1
    isSuccess "execute cmd: Flink service check"
}

# Check Optional connection engines by zwk
if [ "$ENABLE_TRINO" == "true" ]; then
  checkTrino
fi

if [ "$ENABLE_ES" == "true" ]; then
  checkElasticSearch
fi

if [ "$ENABLE_FLINK" == "true" ]; then
  checkFlink
fi
