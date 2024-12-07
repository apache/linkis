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

# description:  Stop all Server
#
# Modified for Linkis 1.0.0
#Actively load user env

#Configure the ip addresses of each service cluster. Multiple ip addresses are separated by commas (,)
#eureka cluster,
EUREKA_SERVER_IPS=127.0.0.1

#gateway cluster
GATEWAY_SERVER_IPS=127.0.0.1

#publicservice server cluster
PUBLICSERVICE_SERVER_IPS=127.0.0.1

#linkis manager server cluster
MANAGER_SERVER_IPS=127.0.0.1

#entrance cluster
ENTRANCE_SERVER_IPS=127.0.0.1

#ecm cluster
ENGINECONNMANAGER_SERVER_IPS=127.0.0.1

#ssh port
SSH_PORT=22