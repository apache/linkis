/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
SET FOREIGN_KEY_CHECKS = 0;
SET REFERENTIAL_INTEGRITY FALSE;

DROP TABLE IF EXISTS linkis_ps_instance_info;
CREATE TABLE linkis_ps_instance_info (
  id int(11) NOT NULL AUTO_INCREMENT,
  instance varchar(128) DEFAULT NULL ,
  name varchar(128)   DEFAULT NULL,
  update_time datetime(3)  DEFAULT CURRENT_TIMESTAMP,
  create_time datetime(3) DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ;


DROP TABLE IF EXISTS linkis_ps_instance_label;
CREATE TABLE linkis_ps_instance_label (
  id int(20) NOT NULL AUTO_INCREMENT,
  label_key varchar(32)     NOT NULL,
  label_value varchar(255)     NOT NULL,
  label_feature varchar(16)  NOT NULL,
  label_value_size int(20) NOT NULL,
  update_time datetime(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP,
  create_time datetime(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ;

DROP TABLE IF EXISTS linkis_ps_instance_label_relation;
CREATE TABLE linkis_ps_instance_label_relation (
  id int(20) NOT NULL AUTO_INCREMENT,
  label_id int(20) DEFAULT NULL ,
  service_instance varchar(128)  NOT NULL ,
  update_time datetime(3)  DEFAULT CURRENT_TIMESTAMP   ,
  create_time datetime(3) DEFAULT CURRENT_TIMESTAMP ,
  PRIMARY KEY (id)
) ;


DROP TABLE IF EXISTS linkis_ps_instance_label_value_relation;
CREATE TABLE linkis_ps_instance_label_value_relation (
  id int(20) NOT NULL AUTO_INCREMENT,
  label_value_key varchar(255)  NOT NULL    ,
  label_value_content varchar(255)  DEFAULT NULL ,
  label_id int(20) DEFAULT NULL ,
  update_time datetime DEFAULT CURRENT_TIMESTAMP ,
  create_time datetime DEFAULT CURRENT_TIMESTAMP ,
  PRIMARY KEY (id)
) ;
