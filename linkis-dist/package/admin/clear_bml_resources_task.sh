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
MYSQL_EXEC_CMD="mysqlsec --dpmc $LINKIS_DB_CONFIG_PATH -h$MYSQL_HOST -P$MYSQL_PORT  $MYSQL_DB -ss -e "
if [ "$is_mysqlsec" == "false" ]; then
    echo "使用 mysql 进行数据处理"
    MYSQL_EXEC_CMD="mysql -h$MYSQL_HOST -P$MYSQL_PORT -u$MYSQL_USER -p$MYSQL_PASSWORD $MYSQL_DB -ss -e "
fi
echo "start to delete linkis_ps_bml_resources_task version data"

#查询版本信息大于10的resouce_id,并进行遍历处理
$MYSQL_EXEC_CMD"SELECT resource_id FROM (SELECT resource_id, COUNT(version) AS version_num FROM linkis_ps_bml_resources_task GROUP BY resource_id) a WHERE a.version_num > 10" | while IFS=$'\t' read -r resource_id; do
    #统计resource_id版本数量
    total_count=$($MYSQL_EXEC_CMD"SELECT COUNT(*) FROM linkis_ps_bml_resources_task WHERE resource_id = \"$resource_id\"")
    while [ $total_count -gt 10 ];do
      echo "Resource_id: $resource_id, Total count: $total_count"
      #拼接需要删除的id
      ids_to_delete=$($MYSQL_EXEC_CMD"SELECT GROUP_CONCAT(id) FROM (SELECT id FROM linkis_ps_bml_resources_task WHERE resource_id = \"$resource_id\" ORDER BY version DESC LIMIT 5000 OFFSET 10) t")
      echo "$resource_id,will to delete: $ids_to_delete"
      if [ -n "$ids_to_delete" ]; then
          #执行删除操作
          $MYSQL_EXEC_CMD"DELETE FROM linkis_ps_bml_resources_task WHERE resource_id = \"$resource_id\" AND id IN ($ids_to_delete)"
          echo "($ids_to_delete) delete over"
          #更新resource_id版本数量
          total_count=$($MYSQL_EXEC_CMD"SELECT COUNT(*) FROM linkis_ps_bml_resources_task WHERE resource_id = \"$resource_id\"")
          echo "Resource_id: $resource_id, left count: $total_count"
      fi
   done
done
