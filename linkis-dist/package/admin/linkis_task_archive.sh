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
#4.2025-03  优化成直接删除不执行归档，保留三个月数据即可

HDFS_BASE_DIR="/appcom/logs/linkis"
#HDFS_CHECK_DIR="${HDFS_BASE_DIR}/archive"
## 检查目录是否存在
#hadoop fs -test -d "${HDFS_CHECK_DIR}"
## 获取检查结果的返回码
#CHECK_RESULT=$?
#if [ ${CHECK_RESULT} -eq 0 ]; then
#  echo "目录已存在: ${HDFS_CHECK_DIR}"
#else
#  echo "目录不存在，正在创建: ${HDFS_CHECK_DIR}"
#  hadoop fs -mkdir -p "${HDFS_CHECK_DIR}"
#fi

#LINKIS
#清理log
echo "开始扫描 Linkis Log 目录（超过三个月的目录将被自动清理）"
LOG_DATE_LIST=$(hadoop fs -ls $HDFS_BASE_DIR/log/ | awk '{print $8}'| awk -F'/' '{print $NF}'| awk -v cutoff_date=$(date -d "3 months ago" "+%Y-%m-%d") '$1 < cutoff_date')
for LOG_DATE in $LOG_DATE_LIST
do
    echo "目录超三个月，删除log目录：$LOG_DATE"
    hadoop fs -rm -r $HDFS_BASE_DIR/log/$LOG_DATE
done

#清理result
echo "开始扫描 Linkis Result 目录（超过三个月的目录将被自动清理）"
RESULT_DATE_LIST=$(hadoop fs -ls $HDFS_BASE_DIR/result/ | awk '{print $8}'| awk -F'/' '{print $NF}'| awk -v cutoff_date=$(date -d "3 months ago" "+%Y-%m-%d") '$1 < cutoff_date')
for RESULT_DATE in $RESULT_DATE_LIST
do
    echo "目录超三个月，删除result目录：$RESULT_DATE"
    hadoop fs -rm -r $HDFS_BASE_DIR/result/$RESULT_DATE
done

#清理DOPS log
echo "开始扫描 Dops Log 目录（超过七天的目录将被自动清理）"
DOPS_LOG_DATE_LIST=$(hadoop fs -ls $HDFS_BASE_DIR/log/ | awk '{print $8}'| awk -F'/' '{print $NF}'| awk -v cutoff_date=$(date -d "7 days ago" "+%Y-%m-%d") '$1 < cutoff_date')
for LOG_DATE in $DOPS_LOG_DATE_LIST
do
	  DOPS_LOG_DIR=$(hdfs dfs -ls $HDFS_BASE_DIR/log/$LOG_DATE | grep DOPS | awk '{print $8}')
    for LOG_DIR in $DOPS_LOG_DIR
    do
    	  echo "DOPS目录超7天，删除log目录：$LOG_DIR"
    	  hadoop fs -rm -r  $LOG_DIR
    done
done

#清理DOPS result
echo "开始扫描 Dops Result 目录（超过七天的目录将被自动清理）"
DOPS_RESULT_DATE_LIST=$(hadoop fs -ls $HDFS_BASE_DIR/result/ | awk '{print $8}'| awk -F'/' '{print $NF}'| awk -v cutoff_date=$(date -d "7 days ago" "+%Y-%m-%d") '$1 < cutoff_date')
for RESULT_DATE in $DOPS_RESULT_DATE_LIST
do
    # result目录
    DOPS_RESULT_DIR=$(hdfs dfs -ls $HDFS_BASE_DIR/result/$RESULT_DATE | grep DOPS | awk '{print $8}')
    for RESULT_DIR in $DOPS_RESULT_DIR
    do
        echo "DOPS目录超7天，删除result目录：$RESULT_DIR"
        hadoop fs -rm -r  $RESULT_DIR
    done
done