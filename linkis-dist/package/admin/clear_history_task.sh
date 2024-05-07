#!/bin/bash
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
delete_day_num=90
if [[ $1 =~ ^[0-9]+$ ]]; then
    if [ $1 -gt 0 ]; then
      delete_day_num=$1
    fi
fi

MYSQL_EXEC_CMD="mysqlsec --dpmc $LINKIS_DB_CONFIG_PATH -h$MYSQL_HOST -P$MYSQL_PORT  $MYSQL_DB -ss -e "
if [ "$is_mysqlsec" == "false" ]; then
    echo "使用 mysql 进行数据处理"
    MYSQL_EXEC_CMD="mysql -h$MYSQL_HOST -P$MYSQL_PORT -u$MYSQL_USER -p$MYSQL_PASSWORD $MYSQL_DB -ss -e "
fi
delete_day=`date -d "-$delete_day_num days"  "+%Y-%m-%d"`
delte_time="$delete_day 00:00:00"
echo "start to delete linkis_ps_job_history_group_history before $delte_time"
parm="created_time <=\"$delte_time\" "

count=$($MYSQL_EXEC_CMD "SELECT count(1) FROM linkis_ps_job_history_group_history where $parm limit 1" 2>&1)
if [ $? -ne 0 ]; then
    echo "执行 count 查询出错，错误信息为: $count"
else
    echo "count 查询执行成功，结果为: $count"
fi

maxid=$($MYSQL_EXEC_CMD "SELECT MAX(id) FROM linkis_ps_job_history_group_history where $parm limit 1" 2>&1)
if [ $? -ne 0 ]; then
    echo "执行 maxid 查询出错，错误信息为: $maxid"
else
    echo "maxid 查询执行成功，结果为: $maxid"
fi

while [ $count -gt 0 ];do
    $MYSQL_EXEC_CMD "DELETE FROM linkis_ps_job_history_group_history  where id <= $maxid limit 5000;"
    count=$($MYSQL_EXEC_CMD "SELECT count(1) FROM linkis_ps_job_history_group_history where $parm limit 1" 2>&1)
    if [ $? -ne 0 ]; then
        echo "执行 count 查询出错，错误信息为: $count"
    else
        echo "删除成功，剩余数量: $count"
    fi
    sleep 1s
done