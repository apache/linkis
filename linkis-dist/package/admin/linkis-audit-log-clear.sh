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

#清理linkis本地审计日志，上传至hdfs
#清理条件：按月清理，超过一个月的审计日志将被清理上传
#本地目录：/data/bdp/audit/linkis/
#hdfs目录：/appcom/logs/linkis/audit/

# 获取当前月份
current_date=$(date +"%Y-%m")

# 设置本地目录路径
local_dir="/data/bdp/audit/linkis/"

# 设置HDFS目录路径
hdfs_dir="/appcom/logs/linkis/audit/"

# 遍历本地目录
for dir in "$local_dir"*
do
    # 检查是否为目录
    if [ -d "$dir" ]; then
        # 获取目录名称
        dir_name=$(basename "$dir")

        # 检查目录名称是否为合法日期格式，并且超过一个月
        if [[ $dir_name =~ ^[0-9]{4}-[0-9]{2}$ && "$dir_name" < "$current_date" ]]; then
            # 遍历日志文件
            for log_file in "$dir"/linkis-audit-*.log
            do
                # 检查日志文件名称是否符合指定格式
                if [[ $(basename "$log_file") =~ ^linkis-audit-[0-9]{4}-[0-9]{2}-[0-9]{2}-[0-9]+\.log$ ]]; then
                    # 提取年月信息
                    year_month=$(basename "$log_file" | grep -oE '[0-9]{4}-[0-9]{2}')
                    
                    # 构建HDFS目标路径
                    hdfs_target_dir="$hdfs_dir$year_month/"
                    
                    # 检查HDFS目录是否存在，不存在则创建
                    hdfs dfs -test -d "$hdfs_target_dir" || hdfs dfs -mkdir -p "$hdfs_target_dir"
                    
                    # 将日志文件上传至HDFS目录
                    hdfs dfs -put "$log_file" "$hdfs_target_dir"
                    
                    echo "Uploaded $log_file to $hdfs_target_dir"
                    
                    # 上传完成后删除本地文件
                    rm "$log_file"
                fi
            done
            # 删除上传完成后的本地目录
            rmdir "$dir"
        fi
    fi
done