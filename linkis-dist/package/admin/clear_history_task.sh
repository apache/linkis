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
# description:  clear linkis_ps_job_history_group_history  3 month record
#
if [ -f ${LINKIS_CONF_DIR}/db.sh ]
then
   export LINKIS_DB_CONFIG_PATH=${LINKIS_CONF_DIR}/db.sh
else
   if [ -f ${LINKIS_HOME}/conf/db.sh ]
   then
      export LINKIS_DB_CONFIG_PATH=${LINKIS_HOME}/conf/db.sh
   else
      echo "can not find db.sh"
      exit
   fi
fi
source ${LINKIS_DB_CONFIG_PATH}

delete_day=`date -d "-90 days"  "+%Y-%m-%d"`
delte_time="$delete_day 00:00:00"
echo "start to delete linkis_ps_job_history_group_history before $delte_time"
parm="created_time <=\"$delte_time\" "

count=`mysql -h$MYSQL_HOST -P$MYSQL_PORT -u$MYSQL_USER  -p$MYSQL_PASSWORD  $MYSQL_DB -ss -e "SELECT count(1) FROM linkis_ps_job_history_group_history where $parm limit 1 "`
maxid=`mysql -h$MYSQL_HOST -P$MYSQL_PORT -u$MYSQL_USER  -p$MYSQL_PASSWORD  $MYSQL_DB -ss -e "SELECT MAX(id) FROM linkis_ps_job_history_group_history where $parm limit 1 "`
echo "will delete count:$count"
echo "maxid:$maxid"

while [ $count -gt 1 ];do
   mysql -h$MYSQL_HOST -P$MYSQL_PORT -u$MYSQL_USER  -p$MYSQL_PASSWORD  $MYSQL_DB -ss -e "DELETE FROM linkis_ps_job_history_group_history where id <= $maxid limit 5000;"
   count=`mysql -h$MYSQL_HOST -P$MYSQL_PORT -u$MYSQL_USER  -p$MYSQL_PASSWORD  $MYSQL_DB -ss -e "SELECT count(1) FROM linkis_ps_job_history_group_history where $parm limit 1 "`
   echo "count change ： $count"
   sleep 1s
done