#!/bin/bash
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
#脚本使用说明：
#HDFS文件 LOG/RESULT 目录递归
#递归规则：
#1.目录：
#/appcom/logs/linkis/log/2023-11-27    -> /appcom/logs/linkis/archive/log/2023-11-27//LINKISCLI.har
#/appcom/logs/linkis/result/2023-11-27   -> /appcom/logs/linkis/archive/result/2023-11-27//LINKISCLI.har
#2.优先递归DOPS(一周之前所有数据)
#3.递归其他应用数据(两个月之前所有数据)

HDFS_BASE_DIR="/appcom/logs/linkis"
HDFS_CHECK_DIR="${HDFS_BASE_DIR}/archive"
# 检查目录是否存在
hadoop fs -test -d "${HDFS_CHECK_DIR}"
# 获取检查结果的返回码
CHECK_RESULT=$?
if [ ${CHECK_RESULT} -eq 0 ]; then
  echo "目录已存在: ${HDFS_CHECK_DIR}"
else
  echo "目录不存在，正在创建: ${HDFS_CHECK_DIR}"
  hadoop fs -mkdir -p "${HDFS_CHECK_DIR}"
fi

#DOPS 获取日志超过7天的日期列表:/appcom/logs/linkis/log/2023-11-27
#2023-11-27
DOPS_LOG_DATE_LIST=$(hadoop fs -ls $HDFS_BASE_DIR/log/ | awk '{print $8}'| awk -F'/' '{print $NF}'| awk -v cutoff_date=$(date -d "7 days ago" "+%Y-%m-%d") '$1 < cutoff_date')
for LOG_DATE in $DOPS_LOG_DATE_LIST
do
# log目录
DOPS_LOG_DIR=$(hdfs dfs -ls $HDFS_BASE_DIR/log/$LOG_DATE | grep DOPS | awk '{print $8}')
	for LOG_DIR in $DOPS_LOG_DIR
	do
	  # 获取应用名称:DOPS_CLEAR,DOPS_OTHER
	  APP_NAME=$(echo "$LOG_DIR" | awk -F'/' '{print $NF}')
	  # 归档log
	  hadoop archive -archiveName $APP_NAME.har -p  $LOG_DIR /appcom/logs/linkis/archive/log/$LOG_DATE
	  # 删除log
	  hadoop fs -rm -r  $LOG_DIR
	done
done

#DOPS 获取结果集超过7天的日期列表
DOPS_RESULT_DATE_LIST=$(hadoop fs -ls $HDFS_BASE_DIR/result/ | awk '{print $8}'| awk -F'/' '{print $NF}'| awk -v cutoff_date=$(date -d "7 days ago" "+%Y-%m-%d") '$1 < cutoff_date')
for RESULT_DATE in $DOPS_RESULT_DATE_LIST
do
# result目录
DOPS_RESULT_DIR=$(hdfs dfs -ls $HDFS_BASE_DIR/result/$RESULT_DATE | grep DOPS | awk '{print $8}')
	for RESULT_DIR in $DOPS_RESULT_DIR
	do
	  # 获取应用名称
	  APP_NAME=$(echo "$RESULT_DIR" | awk -F'/' '{print $NF}')
	  # 归档result
	  hadoop archive -archiveName $APP_NAME.har -p  $RESULT_DIR /appcom/logs/linkis/archive/result/$RESULT_DATE
	  # 删除result
	  hadoop fs -rm -r  $RESULT_DIR
	done
done

#Other 应用获取LOG超过2个月的日期列表
LOG_DATE_LIST=$(hadoop fs -ls /appcom/logs/linkis/log/ | awk '{print $8}'| awk -F'/' '{print $NF}'| awk -v cutoff_date=$(date -d "2 months ago" "+%Y-%m-%d") '$1 < cutoff_date')
for LOG_DATE in $LOG_DATE_LIST
do
# 其他应用LOG目录
LOG_DIR=$(hdfs dfs -ls $HDFS_BASE_DIR/log/$LOG_DATE  | awk '{print $8}')
	for APP_LOG in $LOG_DIR
	do
	  # 获取应用名称
	  APP_NAME=$(echo "$APP_LOG" | awk -F'/' '{print $NF}')
	  # 归档LOG
	  hadoop archive -archiveName $APP_NAME.har -p  $APP_LOG /appcom/logs/linkis/archive/log/$LOG_DATE
	  # 删除旧LOG
	  hadoop fs -rm -r  $APP_LOG
	done

# 检测目录是否为空
hdfs_file_count=$(hdfs dfs -count $HDFS_BASE_DIR/log/$LOG_DATE | awk '{print $2}')
if [ $hdfs_file_count -eq 0 ]; then
    echo "HDFS目录为空，删除目录：$LOG_DATE"
    hdfs dfs -rm -r $HDFS_BASE_DIR/log/$LOG_DATE
fi
done


#Other 应用获取RESULT超过2个月的日期列表
RESULT_DATE_LIST=$(hadoop fs -ls /appcom/logs/linkis/result/ | awk '{print $8}'| awk -F'/' '{print $NF}'| awk -v cutoff_date=$(date -d "2 months ago" "+%Y-%m-%d") '$1 < cutoff_date')
for RESULT_DATE in $RESULT_DATE_LIST
do
# RESULT目录
RESULT_DIR=$(hdfs dfs -ls $HDFS_BASE_DIR/result/$RESULT_DATE | awk '{print $8}')
	for APP_DIR in $RESULT_DIR
	do
	  # 获取应用名称
	  APP_NAME=$(echo "$APP_DIR" | awk -F'/' '{print $NF}')
	  # 归档RESULT
	  hadoop archive -archiveName $APP_NAME.har -p  $APP_DIR /appcom/logs/linkis/archive/result/$RESULT_DATE
	  # 删除旧RESULT
	  hadoop fs -rm -r  $APP_DIR
	done

# 检测目录是否为空
hdfs_file_count=$(hdfs dfs -count $HDFS_BASE_DIR/result/$RESULT_DATE | awk '{print $2}')
if [ $hdfs_file_count -eq 0 ]; then
    echo "HDFS目录为空，删除目录：$RESULT_DATE"
    hdfs dfs -rm -r $HDFS_BASE_DIR/result/$RESULT_DATE
fi
done