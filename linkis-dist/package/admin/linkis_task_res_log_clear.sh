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

#!/bin/bash
expiredDays=365
resultSetRootDir=/tmp/linkis
logRootDir=/tmp/linkis
userResultSetDir=$(hdfs dfs -ls $resultSetRootDir | awk '{print $8}')
realLogRootDir=$logRootDir/log
echo userResultSetDirs: $userResultSetDir
echo realLogRootDir: $realLogRootDir

if [ -z $LINKIS_LOG_DIR ];then
 expiredFileRecordDir=${LINKIS_HOME}/expiredFileRecord
else
 expiredFileRecordDir=$LINKIS_LOG_DIR/expiredFileRecord
fi

function createExpiredFileRecoredDir(){
 if [ ! -d $expiredFileRecordDir ];then
  mkdir -p $expiredFileRecordDir
 fi
}

createExpiredFileRecoredDir
expireDate=$(date -d -${expiredDays}day +%Y-%m-%d)
expireResultSetFile=$expiredFileRecordDir/linkis_expire_resultset_dir_${expireDate}.txt
expireLogFile=$expiredFileRecordDir/linkis_expire_log_dir_${expireDate}.txt

hdfs dfs -ls $realLogRootDir | awk '$8 ~ /.*linkis\/log\/[0-9|\-|\_]*/ {cmd = "date -d -12month +%Y-%m-%d";cmd | getline oneMonthAgo;if($6 < oneMonthAgo) print $8}' >> $expireLogFile

for i in $userResultSetDir
do
  hdfs dfs -ls $i/linkis | awk '$8 ~ /.*linkis\/[0-9\-]{10}/ {cmd = "date -d -12month +%Y-%m-%d";cmd | getline oneMonthAgo;if($6 < oneMonthAgo) print $8}' >> $expireResultSetFile
done

cat $expireLogFile | xargs -n 1000 hdfs dfs -rm -r -f

cat $expireResultSetFile | xargs -n 1000 hdfs dfs -rm -r -f


