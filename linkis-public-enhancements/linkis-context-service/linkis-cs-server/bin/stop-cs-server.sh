#!/bin/bash
#
# Copyright 2019 WeBank
#
# Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


cd `dirname $0`
cd ..
HOME=`pwd`

export SERVER_PID=$HOME/bin/linkis.pid

function wait_for_server_to_die() {
  local pid
  local count
  pid=$1
  timeout=$2
  count=0
  timeoutTime=$(date "+%s")
  let "timeoutTime+=$timeout"
  currentTime=$(date "+%s")
  forceKill=1

  while [[ $currentTime -lt $timeoutTime ]]; do
    $(kill ${pid} > /dev/null 2> /dev/null)
    if kill -0 ${pid} > /dev/null 2>&1; then
      sleep 3
    else
      forceKill=0
      break
    fi
    currentTime=$(date "+%s")
  done

  if [[ forceKill -ne 0 ]]; then
    $(kill -9 ${pid} > /dev/null 2> /dev/null)
  fi
}

if [[ ! -f "${SERVER_PID}" ]]; then
    echo "server $SERVER_NAME is not running"
else
    pid=$(cat ${SERVER_PID})
    if [[ -z "${pid}" ]]; then
      echo "server $SERVER_NAME is not running"
    else
      wait_for_server_to_die $pid 40
      $(rm -f ${SERVER_PID})
      echo "server $SERVER_NAME is stopped."
    fi
fi