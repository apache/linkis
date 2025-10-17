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

job_id=$1
log_path=$2
yyy_mm=$3
yyyy_mm_dd=$4
created_time_date=$5
module_name=$7

# 远端引擎日志分组写入
function remote_engine_logs() {
  cd $log_path
  echo -e "$(cat ./stdout | grep "JobId-$job_id")" >"./"$job_id"_engineconn.log"
}

function remote_link_logs() {
  cd $log_path
  if [ ! -f "./$module_name.log" ]; then
    exit 1
  fi
  # 获取首行日志信息
  first_row=$(head -n 1 ./$module_name.log | cut -b 1-23)
  first_row_timestamp=$(date -d "$first_row" +%s)
  # 首行日期大于任务创建时间，代表已经滚动
  if [ "$first_row_timestamp" -gt "$created_time_date" ]; then
    # 链路日志已滚动 （grep "JobId-$1" 必须双引号，命令行可以单引号）
    # echo -e > 写入本地，必须带引号，否则不能换行
    echo -e "$(cat ./$module_name.log ./$yyy_mm/$module_name/* | grep "JobId-$job_id")" >"./"$job_id"_$module_name.log"
  else
    # 链路日志未滚动
    echo -e "$(cat ./$module_name.log | grep "JobId-$job_id")" >"./"$job_id"_$module_name.log"
  fi
}

if [ "$6" == "1" ]; then
  remote_link_logs $*
else
  remote_engine_logs $*
fi
