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

SET FOREIGN_KEY_CHECKS=0;
SET REFERENTIAL_INTEGRITY FALSE;

DROP TABLE IF EXISTS linkis_ps_udf_user_load CASCADE;
CREATE TABLE IF NOT EXISTS linkis_ps_udf_user_load (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  udf_id bigint(20) NOT NULL,
  user_name varchar(50) NOT NULL,
  PRIMARY KEY (id)
) ;

DROP TABLE IF EXISTS linkis_ps_udf_baseinfo CASCADE;
CREATE TABLE IF NOT EXISTS linkis_ps_udf_baseinfo (
  id numeric(20) NOT NULL AUTO_INCREMENT,
  create_user varchar(50) NOT NULL,
  udf_name varchar(255) NOT NULL,
  udf_type numeric(11) DEFAULT '0',
  tree_id bigint(20) NOT NULL,
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  sys varchar(255) NOT NULL DEFAULT 'ide' COMMENT 'source system',
  cluster_name varchar(255) NOT NULL,
  is_expire numeric(1) DEFAULT NULL,
  is_shared numeric(1) DEFAULT NULL,
  PRIMARY KEY (id)
) ;

DROP TABLE IF EXISTS linkis_ps_udf_tree CASCADE;
CREATE TABLE IF NOT EXISTS linkis_ps_udf_tree (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  parent bigint(20) NOT NULL,
  name varchar(100) DEFAULT NULL COMMENT 'Category name of the function. It would be displayed in the front-end',
  user_name varchar(50) NOT NULL,
  description varchar(255) DEFAULT NULL,
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  category varchar(50) DEFAULT NULL COMMENT 'Used to distinguish between udf and function',
  PRIMARY KEY (id)
) ;

DROP TABLE IF EXISTS linkis_ps_udf_version CASCADE;
CREATE TABLE IF NOT EXISTS linkis_ps_udf_version (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  udf_id bigint(20) NOT NULL,
  path varchar(255) NOT NULL COMMENT 'Source path for uploading files',
  bml_resource_id varchar(50) NOT NULL,
  bml_resource_version varchar(20) NOT NULL,
  is_published bit(1) DEFAULT NULL COMMENT 'is published',
  register_format varchar(255) DEFAULT NULL,
  use_format varchar(255) DEFAULT NULL,
  description varchar(255) NOT NULL COMMENT 'version desc',
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  md5 varchar(100) DEFAULT NULL,
  PRIMARY KEY (id)
) ;

DROP TABLE IF EXISTS linkis_ps_udf_shared_info CASCADE;
CREATE TABLE IF NOT EXISTS linkis_ps_udf_shared_info (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  udf_id bigint(20) NOT NULL,
  user_name varchar(50) NOT NULL,
  PRIMARY KEY (id)
) ;


DROP TABLE IF EXISTS linkis_ps_udf_manager CASCADE;
CREATE TABLE IF NOT EXISTS linkis_ps_udf_manager (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  user_name varchar(20) DEFAULT NULL,
  PRIMARY KEY (id)
) ;