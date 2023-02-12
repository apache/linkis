source ${LINKIS_HOME}/conf/db.sh
echo 'delete from  `linkis_cg_manager_label_value_relation` where label_value_key="alias";
delete from  `linkis_cg_manager_label_value_relation` where label_value_key="serviceName";
delete from  `linkis_cg_manager_label_value_relation` where label_value_key="instance";
delete from  `linkis_cg_manager_label` where label_key="engineInstance" or label_key="emInstance";

DELETE FROM  `linkis_cg_manager_label_service_instance`;
DELETE FROM  `linkis_cg_manager_linkis_resources`;
DELETE FROM  `linkis_cg_manager_service_instance_metrics`;
DELETE FROM  `linkis_cg_manager_lock` ;
DELETE FROM  `linkis_cg_manager_label_resource`;
DELETE FROM  `linkis_cg_manager_service_instance`;
DELETE FROM  `linkis_cg_manager_service_instance_metrics`;
DELETE FROM  `linkis_cg_manager_engine_em`;
' > /tmp/clearserver.command
mysql -h ${MYSQL_HOST} -P${MYSQL_PORT} -u${MYSQL_USER} -p${MYSQL_PASSWORD} ${MYSQL_DB} < /tmp/clearserver.command
rm -f /tmp/clearserver.command
