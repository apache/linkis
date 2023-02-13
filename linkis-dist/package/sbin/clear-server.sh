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

source ${LINKIS_HOME}/conf/db.sh
echo 'DELETE FROM  `linkis_cg_manager_label_value_relation` where label_value_key="alias";
DELETE FROM  `linkis_cg_manager_label_value_relation` where label_value_key="serviceName";
DELETE FROM  `linkis_cg_manager_label_value_relation` where label_value_key="instance";
DELETE FROM  `linkis_cg_manager_label` where label_key="engineInstance" or label_key="emInstance";

DELETE FROM  `linkis_cg_manager_label_service_instance`;
DELETE FROM  `linkis_cg_manager_linkis_resources`;
DELETE FROM  `linkis_cg_manager_service_instance_metrics`;
DELETE FROM  `linkis_cg_manager_lock` ;
DELETE FROM  `linkis_cg_manager_label_resource`;
DELETE FROM  `linkis_cg_manager_service_instance`;
DELETE FROM  `linkis_cg_manager_engine_em`;
' > /tmp/clearserver.command
mysql -h ${MYSQL_HOST} -P${MYSQL_PORT} -u${MYSQL_USER} -p${MYSQL_PASSWORD} ${MYSQL_DB} < /tmp/clearserver.command
rm -f /tmp/clearserver.command
