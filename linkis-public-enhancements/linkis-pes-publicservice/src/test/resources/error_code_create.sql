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
SET
    FOREIGN_KEY_CHECKS = 0;
SET
    REFERENTIAL_INTEGRITY FALSE;

DROP TABLE IF EXISTS `linkis_ps_error_code`;
CREATE TABLE `linkis_ps_error_code` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `error_code` varchar(50)   NOT NULL,
  `error_desc` varchar(1024)   NOT NULL,
  `error_regex` varchar(1024)   DEFAULT NULL,
  `error_type` int(3) DEFAULT '0',
  PRIMARY KEY (`id`)
);

DELETE FROM linkis_ps_error_code;
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('01002','Linkis服务负载过高，请联系管理员扩容','Unexpected end of file from server',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('01001','您的任务没有路由到后台ECM，请联系管理员','The em of labels',0);
