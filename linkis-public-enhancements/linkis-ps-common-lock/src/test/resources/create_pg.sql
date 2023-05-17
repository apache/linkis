/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

DROP TABLE IF EXISTS "linkis_ps_common_lock";
CREATE TABLE linkis_ps_common_lock (
	id bigserial NOT NULL,
	lock_object varchar(255) NULL,
    time_out text NULL,
	update_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	create_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT linkis_ps_common_lock_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_lock_object ON linkis_ps_common_lock (lock_object);

DELETE FROM linkis_ps_common_lock;
insert into linkis_ps_common_lock("lock_object","time_out","update_time","create_time") values ('hadoop-warehouse',1000000,now(),now());

