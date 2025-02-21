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
source /appcom/Install/linkis/conf/linkis-env.sh

if [ "$LINKIS_LOG_DIR" = "" ]; then
  export LINKIS_LOG_DIR="/data/logs/bdpe-ujes"
fi
# Set LINKIS_HOME
if [ -z "$LINKIS_HOME" ]; then
  export LINKIS_HOME="$INSTALL_HOME"
fi
linkisRemoteEnginePath="$LINKIS_HOME/admin/tools/remote-engine.sh"

server_name=$1
job_id=$2
log_path=$3
option_flag=$4
module_name=$5
task_path="$LINKIS_LOG_DIR/task"
job_path=$task_path/$job_id

if [ ! -d $job_path ]; then
  mkdir $job_path
fi

if [ "$option_flag" == "0" ]; then
  # 获取远端ec日志
  ssh -q hadoop@$server_name 'bash -s' $job_id $log_path $yyy_mm $yyyy_mm_dd "$created_time_date" <$linkisRemoteEnginePath

  # 符合远程的ec日志复制到本地
  scp -q -r hadoop@$server_name:$log_path/"$job_id"_engineconn.log $job_path/engineconn_"$server_name".log

  # 删除远端临时日志
  ssh -q hadoop@$server_name "rm -f $log_path/$2_engineconn.log"
elif [ "$option_flag" == "1" ]; then
  if [ $module_name == "linkis-cg-engineconn" ]; then
    echo "此版本暂分析不出来，请走人工排查！"
    exit 1
  else
    ssh -q hadoop@$server_name 'bash -s' $job_id $log_path $yyy_mm $yyyy_mm_dd $created_time_date 1 $module_name <$linkisRemoteEnginePath

    scp -q -r hadoop@$server_name:$log_path/"$job_id"_$module_name.log $job_path/$module_name"_"$server_name.log

    ssh -q hadoop@$server_name "rm -f $log_path/$2_$module_name.log"
  fi
else
  echo "暂不支持的日志分析！"
fi
