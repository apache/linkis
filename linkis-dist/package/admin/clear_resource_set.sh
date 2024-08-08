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
# description:
# Clean up the resource script, call linkismanager to reset the resource interface,
# parameters: serviceInstance, username,
# demo: sh clear_resource_set.sh bdpdws110002:9102   hadoop
# demo: sh clear_resource_set.sh "" hduser0102
#

cd "$(dirname "$0")"
cd ..
INSTALL_HOME="$(pwd)"

# Set LINKIS_HOME
if [ -z "$LINKIS_HOME" ]; then
  export LINKIS_HOME="$INSTALL_HOME"
fi

# Set LINKIS_CONF_DIR
if [ -z "$LINKIS_CONF_DIR" ]; then
  export LINKIS_CONF_DIR="$LINKIS_HOME/conf"
fi

# Read configuration
linkisMainConf="$LINKIS_CONF_DIR/linkis.properties"
gatewayUrl=$(grep '^wds.linkis.gateway.url' "$linkisMainConf" | cut -d "=" -f2)

echo "gatewayUrl: $gatewayUrl"

# Parse command-line arguments
serviceInstance=$1
username=$2

# Construct request URL
requestUrl="$gatewayUrl/api/rest_j/v1/linkisManager/reset-resource?serviceInstance=$serviceInstance&username=$username"
echo "requestUrl: $requestUrl"
# Execute request
response=$(curl --silent --location --request GET "$requestUrl" -H "Token-Code:LINKIS-AUTH-eTaYLbQpmIulPyrXcMl" -H "Token-User:hadoop")

# Print response
echo "Response: $response"
