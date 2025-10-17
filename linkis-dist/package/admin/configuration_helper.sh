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


cd `dirname $0`
cd ..
INSTALL_HOME=`pwd`


function print_usage(){
  echo "Usage: configuration_helper.sh [add | get | delete] [engineType] [version] [creator] [configKey] [configValue option]"
  echo "get eq: sh  configuration_helper.sh get spark 2.4.3 test wds.linkis.rm.instance hadoop"
  echo "delete eq: sh  configuration_helper.sh delete spark 2.4.3 test wds.linkis.rm.instance hadoop"
  echo "add eq: sh  configuration_helper.sh add spark 2.4.3 test wds.linkis.rm.instance hadoop 6"
  echo "add eq: sh  configuration_helper.sh add spark 2.4.3 test wds.linkis.rm.instance hadoop 6 force"
  echo "add tips: add with force will ignore check error"
  echo "Most commands print help when invoked w/o parameters."
}

if [ $# -lt 6 ]; then
  print_usage
  exit 2
fi

# set LINKIS_HOME
if [ "$LINKIS_HOME" = "" ]; then
  export LINKIS_HOME=$INSTALL_HOME
fi

# set LINKIS_CONF_DIR
if [ "$LINKIS_CONF_DIR" = "" ]; then
  export LINKIS_CONF_DIR=$LINKIS_HOME/conf
fi
linkisMainConf=$LINKIS_CONF_DIR/linkis.properties
gatewayUrl=$(grep wds.linkis.gateway.url $linkisMainConf | cut -d"=" -f2)
echo "gatewayUrl: $gatewayUrl"
engineType=$2
version=$3
creator=$4
configKey=$5
user=$6
configValue=$7
COMMAND=$1
if [ "$8" = "force" ]; then
 force=true
fi

get()
{
  requestUrl="$gatewayUrl/api/rest_j/v1/configuration/keyvalue?creator=$creator&engineType=$engineType&version=$version&configKey=$configKey"
  curl --location --request GET $requestUrl -H "Token-Code:LINKIS-UNAVAILABLE-TOKEN" -H "Token-User:$user"
}

delete()
{
  requestUrl="$gatewayUrl/api/rest_j/v1/configuration/keyvalue"
  requestBody="{\"engineType\":\"$engineType\",\"version\":\"$version\",\"creator\":\"$creator\",\"configKey\":\"$configKey\"}"
  curl -i -X DELETE $requestUrl  -H "Accept: application/json"  -H "Content-Type: application/json" -H "Token-Code:LINKIS-UNAVAILABLE-TOKEN" -H "Token-User:$user" -d "$requestBody"
}

add()
{
  requestUrl="$gatewayUrl/api/rest_j/v1/configuration/keyvalue"
  requestBody="{\"engineType\":\"$engineType\",\"version\":\"$version\",\"creator\":\"$creator\",\"configKey\":\"$configKey\",\"configValue\":\"$configValue\",\"force\":\"$force\",\"user\":\"$user\"}"
  curl -i -X POST $requestUrl  -H "Accept: application/json"  -H "Content-Type: application/json" -H "Token-Code:LINKIS-UNAVAILABLE-TOKEN" -H "Token-User:hadoop" -d "$requestBody"
}

case $COMMAND in
  add|get|delete)
    $COMMAND
    ;;
  *)
    print_usage
    exit 2
    ;;
esac
