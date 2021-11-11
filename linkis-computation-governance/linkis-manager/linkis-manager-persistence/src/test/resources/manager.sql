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
 
-- 0.实例表：engine/EM,每个enginemanager有一个唯一标签记录它的资源，engine会继承这个em的标签
-- 1.所有 linkis_manager 开头分模块
-- 2.错误码统一,
-- 3.配置文件保持唯一
-- 4.微服务名字唯一
-- 5.启动/停止脚本 传参提供类名即可

-- 针对 entrance 作为EM的情况,instance可以插入多条记录,unique key 一定是 instance，ip和端口 ,name 作为类型
CREATE TABLE `linkis_cg_manager_service_instance`(
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `instance` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `name` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `owner` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `mark` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `update_time` datetime DEFAULT now(),
  `create_time` datetime DEFAULT now(),
  `updator` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `creator` varchar(255) COLLATE utf8_bin DEFAULT NULL,  
  unique key instance(`instance`),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- select * from linkis_cg_manager_service_instance_metrics where service_instance_id in (select id from linkis_cg_manager_service_instance where instance={instance} and name=#{name})

-- select * from linkis_cg_manager_service_instance_metrics where service_instance_id in  (select id from linkis_cg_manager_service_instance where instance=#{} )

-- metric表 service_instance_metrics: service_instance_id status{运行状态，负载情况}  
CREATE TABLE `linkis_cg_manager_service_instance_metrics` (
  `instance` varchar(16) NOT NULL,
  `instance_status` int(11) DEFAULT NULL,
  `overload` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `heartbeat_msg` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `healthy_status` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `update_time` datetime DEFAULT now(),
  `create_time` datetime DEFAULT now(), 
  PRIMARY KEY (`instance`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


CREATE TABLE `linkis_cg_manager_metrics_history` (
  `instance_status` int(20) DEFAULT NULL,
  `overload` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `heartbeat_msg` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `healthy_status` int(20) COLLATE utf8_bin DEFAULT NULL,
  `create_time` datetime DEFAULT now(),
  `creator` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `ticketID` varchar(255) COLLATE utf8_bin DEFAULT NULL,# 通过时间戳做id类似于yarn的appid
  `serviceName` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `instance` varchar(255) COLLATE utf8_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- 一个 EM可能有多个 engine 

-- 引擎与EM关系表
CREATE TABLE `linkis_cg_manager_engine_em` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `engine_instance` varchar(16) DEFAULT NULL,
  `em_instance` varchar(16) DEFAULT NULL,
  `update_time` datetime DEFAULT now(),
  `create_time` datetime DEFAULT now(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- 标签表：
CREATE TABLE `linkis_cg_manager_label` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `label_key` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `label_value` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `label_feature` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `label_value_size` int(20) DEFAULT NULL,
  `update_time` datetime DEFAULT now(),
  `create_time` datetime DEFAULT now(),
  unique key label_key_value(`label_key`, `label_value`),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/**
key  content
把三次查询合成一个查询
AMap---list<int> 交集labelIDs

BMap---list<int> 交集labelIDs

CMap---list<int> 交集labelIDs
*/
-- 标签值-关系表
CREATE TABLE `linkis_cg_manager_label_value_relation` (
  `label_value_key` varchar(255) COLLATE utf8_bin  NOT NULL, 
  `label_value_content` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `label_id` int(20) DEFAULT NULL,
  `update_time` datetime DEFAULT now(),
  `create_time` datetime DEFAULT now(),
   unique key label_value_key_label_id(`label_value_key`, `label_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE TABLE `linkis_manager_lable_user` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `label_id` int(20) DEFAULT NULL,
  `update_time` datetime DEFAULT now(),
  `create_time` datetime DEFAULT now(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


-- 标签/实例关系表
CREATE TABLE `linkis_cg_manager_label_service_instance` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `label_id` int(20) DEFAULT NULL,
  `service_instance` varchar(16) DEFAULT NULL,
  `update_time` datetime DEFAULT now(),
  `create_time` datetime DEFAULT now(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- select * from linkis_cg_manager_linkis_resources where id in (select resource_id from linkis_cg_manager_label_resource A join linkis_manager_lable_user B on A.label_id=B.label_id AND B.username=#{userName})
-- select * from linkis_cg_manager_linkis_resources where resourceType=#{resourceType} and id in (select resource_id from linkis_cg_manager_label_resource A join linkis_cg_manager_label_service_instance B on A.label_id = B.label_id and B.service_instance_id=#{instanceId})

-- select * from linkis_cg_manager_linkis_resources where id in (select resource_id from linkis_cg_manager_label_resource A join linkis_cg_manager_label_service_instance B join on A.label_id = B.label_id and B.service_instance_id=#{instanceId})

-- delete from linkis_cg_manager_label_resource where label_id in (select label_id from linkis_cg_manager_label_service_instance where service_instance_id = #{instanceId})

-- delete from linkis_cg_manager_linkis_resources where id in (select resource_id from linkis_cg_manager_label_resource A join linkis_cg_manager_label_service_instance B on A.label_id=B.label_id and B.service_instance_id==#{instanceId} )

-- 标签资源表
-- label_resource:
CREATE TABLE `linkis_cg_manager_label_resource` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `label_id` int(20) DEFAULT NULL,
  `resource_id` int(20) DEFAULT NULL,
  `update_time` datetime DEFAULT now(),
  `create_time` datetime DEFAULT now(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- delete from linkis_cg_manager_label_resource where resource_id in (select id from linkis_cg_manager_linkis_resources where ticketId=#{ticketId})
-- select id from linkis_cg_manager_linkis_resources where ticketId is null and id in ( select resource_id from linkis_cg_manager_label_resource A join linkis_cg_manager_label_service_instance B on A.label_id=B.label_id and B.service_instance_id=#{instanceId})
-- 资源表
CREATE TABLE `linkis_cg_manager_linkis_resources` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `max_resource` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `min_resource` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `used_resource` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `left_resource` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `expected_resource` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `locked_resource` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `resourceType` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `ticketId` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `update_time` datetime DEFAULT now(),
  `create_time` datetime DEFAULT now(),
  `updator` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `creator` varchar(255) COLLATE utf8_bin DEFAULT NULL,  
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

--  锁表 lock
CREATE TABLE `linkis_cg_manager_lock` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `lock_object` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `time_out`  longtext     DEFAULT NULL,
  `update_time` datetime DEFAULT now(),
  `create_time` datetime DEFAULT now(), 
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
