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
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `udf_id` bigint(20) NOT NULL,
  `user_name` varchar(50) NOT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

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
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
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
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `user_name` varchar(20) DEFAULT NULL,
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
    ) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

DELETE FROM linkis_ps_udf_user_load;
-- ----------------------------
-- Default Tokens
-- ----------------------------
INSERT INTO linkis_ps_udf_user_load (id,udf_id, user_name) VALUES(1,3, 'hadoop');

DELETE FROM linkis_ps_udf_shared_info;
INSERT INTO linkis_ps_udf_shared_info (id,udf_id, user_name) VALUES(1,3, 'hadoop');

DELETE FROM linkis_ps_udf_manager;
INSERT INTO linkis_ps_udf_manager (id,user_name) VALUES (1, 'hadoop');

DELETE FROM linkis_ps_udf_baseinfo;
INSERT INTO linkis_ps_udf_baseinfo (create_user,udf_name,udf_type,tree_id,create_time,update_time,sys,cluster_name,is_expire,is_shared) VALUES
	 ('hadoop','pyUdfTest',1,14,'2022-09-08 11:43:20','2022-09-08 11:43:20','IDE','all',NULL,NULL),
	 ('hadoop','jarUdf',0,14,'2022-09-08 14:53:56','2022-09-08 14:53:56','IDE','all',NULL,NULL),
	 ('hadoop','test',3,13,'2022-09-08 14:54:30','2022-09-08 14:54:30','IDE','all',NULL,NULL),
	 ('hadoop','scalaUdf1',4,13,'2022-09-08 14:55:57','2022-09-08 14:55:57','IDE','all',NULL,NULL);

DELETE FROM linkis_ps_udf_tree;
INSERT INTO linkis_ps_udf_tree (parent,name,user_name,description,create_time,update_time,category) VALUES
	 (-1,'系统函数','sys','','2022-07-14 18:58:50','2022-07-14 18:58:50','udf'),
	 (-1,'BDAP函数','bdp','','2022-07-14 18:58:50','2022-07-14 18:58:50','udf'),
	 (-1,'共享函数','share','','2022-07-14 18:58:50','2022-07-14 18:58:50','udf'),
	 (-1,'过期函数','expire','','2022-07-14 18:58:50','2022-07-14 18:58:50','udf'),
	 (-1,'个人函数','hadoop','','2022-07-14 18:58:50','2022-07-14 18:58:50','udf'),
	 (-1,'系统函数','sys','','2022-07-14 20:28:34','2022-07-14 20:28:34','function'),
	 (-1,'BDAP函数','bdp','','2022-07-14 20:28:35','2022-07-14 20:28:35','function'),
	 (-1,'共享函数','share','','2022-07-14 20:28:35','2022-07-14 20:28:35','function'),
	 (-1,'过期函数','expire','','2022-07-14 20:28:35','2022-07-14 20:28:35','function'),
	 (-1,'个人函数','hadoop','','2022-07-14 20:28:35','2022-07-14 20:28:35','function');
INSERT INTO linkis_ps_udf_tree (parent,name,user_name,description,create_time,update_time,category) VALUES
	 (-1,'个人函数','','','2022-07-29 09:46:18','2022-07-29 09:46:18','udf'),
	 (-1,'个人函数','','','2022-07-29 09:46:19','2022-07-29 09:46:19','function'),
	 (10,'baoyang','hadoop','testBaoYang','2022-07-29 16:30:36','2022-07-29 16:30:36','function'),
	 (5,'pySpark','hadoop','','2022-09-08 11:43:20','2022-09-08 11:43:20','udf');

DELETE FROM linkis_ps_udf_version;
INSERT INTO linkis_ps_udf_version (udf_id,`path`,bml_resource_id,bml_resource_version,is_published,register_format,use_format,description,create_time,md5) VALUES
	 (1,'file:///home/hadoop/logs/linkis/hadoop/hadoops/udf/udfPy.py','ede1985f-b594-421f-9e58-7e3d7d8603ef','v000001',0,'udf.register("pyUdfTest",test)','int pyUdfTest(api)','测试使用','2022-09-08 11:43:20','0774ebbaef1efae6e7554ad569235d2f'),
	 (1,'file:///home/hadoop/logs/linkis/hadoop/hadoops/udf/udfPy.py','ede1985f-b594-421f-9e58-7e3d7d8603ef','v000002',0,'udf.register("pyUdfTest",test)','int pyUdfTest(api)','测试使用','2022-09-08 11:43:26','0774ebbaef1efae6e7554ad569235d2f'),
	 (2,'file:///home/hadoop/logs/linkis/hadoop/hadoops/udf/activation.jar','0de8c361-22ce-4402-bf6f-098b4021deca','v000001',0,'create temporary function jarUdf as "test"','string jarUdf(name)','','2022-09-08 14:53:56','8ae38e87cd4f86059c0294a8fe3e0b18'),
	 (3,'file:///home/hadoop/logs/linkis/hadoop/hadoops/udf/udfPy.py','f69e2fc3-c64a-4ff3-ba3c-ab49f5b3651d','v000001',0,NULL,'string test(name)','','2022-09-08 14:54:30','0774ebbaef1efae6e7554ad569235d2f'),
	 (4,'file:///home/hadoop/logs/linkis/hadoop/hadoops/udf/scalaUdf.scala','fe124e5e-4fdd-4509-aa93-10c3748ba34a','v000001',0,NULL,'String scalaUdf1(Name)','','2022-09-08 14:55:57','0774ebbaef1efae6e7554ad569235d2f');
