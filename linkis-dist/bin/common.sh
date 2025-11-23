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

#Actively load user env
source ~/.bash_profile

export local_host="`hostname --fqdn`"

ipaddr=127.0.0.1

## color
RED='\033[0;31m'
NC='\033[0m' # No Color
GREEN='\033[0;32m'
#used as: echo -e "Apache ${RED}Linkis ${NC} Test \n"

function isLocal(){
    if [ "$1" == "127.0.0.1" ];then
        return 0
    elif [ "$1" == "" ]; then
        return 0
    elif [ "$1" == "localhost" ]; then
        return 0
    elif [ "$1" == $local_host ]; then
        return 0
    elif [ `echo "$ipaddr" | grep  "^$1$" |wc -l` -gt 0 ]; then
        return 0
    fi
        return 1
}

function executeCMD(){
   isLocal $1
   flag=$?
   if [ $flag == "0" ];then
      echo "Is local execution:$2"
      eval $2
   else
      echo "Is remote execution:$2"
      ssh -p $SSH_PORT $1 $2
   fi

}
function copyFile(){
   isLocal $1
   flag=$?
   src=$2
   dest=$3
   if [ $flag == "0" ];then
      echo "Is local cp "
      cp -r "$src" "$dest"
   else
      echo "Is remote cp "
      scp -r -P $SSH_PORT  "$src" $1:"$dest"
   fi

}

function echoErrMsgAndExit() {
    echo -e "${RED}Failed${NC} to $1"
    echo ""
    exit 1
}

function echoSuccessMsg() {
    echo -e "${GREEN}Succeed${NC} to $1"
    echo ""
}

function isSuccess(){
if [ $? -ne 0 ]; then
    echoErrMsgAndExit "$1"
else
    echoSuccessMsg "$1"
fi
}

function isSuccessWithoutExit(){
if [ $? -ne 0 ]; then
    echo -e "WARN failed to $1 , but installation will continue,some function may not work properly"
else
    echoSuccessMsg "$1"
fi
}