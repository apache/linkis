CREATE TABLE `linkis_resources` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `resource_id` varchar(50) NOT NULL COMMENT '资源id，资源的uuid',
  `is_private` TINYINT(1) DEFAULT 0 COMMENT '资源是否私有，0表示私有，1表示公开',
  `resource_header` TINYINT(1) DEFAULT 0 COMMENT '分类，0表示未分类，1表示已分类',
	`downloaded_file_name` varchar(200) DEFAULT NULL COMMENT '下载时的文件名',
	`sys` varchar(100) NOT NULL COMMENT '所属系统',
	`create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
	`owner` varchar(200) NOT NULL COMMENT '资源所属者',
	`is_expire` TINYINT(1) DEFAULT 0 COMMENT '是否过期，0表示不过期，1表示过期',
	`expire_type` varchar(50) DEFAULT 'date' COMMENT '过期类型，date指到指定日期过期，TIME指时间',
	`expire_time` varchar(50) DEFAULT '1' COMMENT '过期时间，默认一天',
	`max_version` int(20) DEFAULT 10 COMMENT '默认为10，指保留最新的10个版本',
	`update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
	`updator` varchar(50) DEFAULT NULL COMMENT '更新者',
	`enable_flag` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态，1：正常，0：冻结',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4;

-- 修改expire_type的默认值为NULL
alter table linkis_resources alter column expire_type set default null;

-- 修改expire_time的默认值为NULL
alter table linkis_resources alter column expire_time set default null;


CREATE TABLE `linkis_resources_version` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `resource_id` varchar(50) NOT NULL COMMENT '资源id，资源的uuid',
  `file_md5` varchar(32) NOT NULL COMMENT '文件的md5摘要',
  `version` varchar(200) NOT NULL COMMENT '资源版本（v 加上 五位数字）',
	`size` int(10) NOT NULL COMMENT '文件大小',
	`resource` varchar(2000) NOT NULL COMMENT '资源内容（文件信息 包括 路径和文件名）',
	`description` varchar(2000) DEFAULT NULL COMMENT '描述',
	`start_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
	`end_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '结束时间',
	`client_ip` varchar(200) NOT NULL COMMENT '客户端ip',
	`updator` varchar(50) DEFAULT NULL COMMENT '修改者',
	`enable_flag` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态，1：正常，0：冻结',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 添加start_byte 和 end_byte 字段
ALTER TABLE `linkis_resources_version` ADD COLUMN `start_byte` BIGINT(20) UNSIGNED NOT NULL DEFAULT 0 AFTER `size`;

ALTER TABLE `linkis_resources_version` ADD COLUMN `end_byte` BIGINT(20) UNSIGNED NOT NULL DEFAULT 0 AFTER `start_byte`;

-- version字段修改
alter table `linkis_resources_version` modify column `version` varchar(20) not null;

-- 给resource_id 和 version 加上联合唯一约束
alter table `linkis_resources_version` add unique key `resource_id_version`(`resource_id`, `version`);


CREATE TABLE `linkis_resources_permission` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `resource_id` varchar(50) NOT NULL COMMENT '资源id，资源的uuid',
  `permission` varchar(10) NOT NULL COMMENT '权限代码',
	`create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
	`system` varchar(50) NOT NULL COMMENT '创建者',
	`update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
	`updator` varchar(50) NOT NULL COMMENT '更新者',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `linkis_resources_download_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
	`start_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
	`end_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '结束时间',
	`client_ip` varchar(200) NOT NULL COMMENT '客户端ip',
	`state` TINYINT(1) NOT NULL COMMENT '下载状态，0下载成功，1下载失败',
	`resources_version_id` int(20) DEFAULT NULL COMMENT '版本id',
	`downloader` varchar(50) NOT NULL COMMENT '下载者',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- 删除resources_version_id 字段
alter table `linkis_resources_download_history` drop column `resources_version_id`;

-- 添加resource_id 字段
alter table `linkis_resources_download_history` add column `resource_id` varchar(50) not null after `state`;

-- 添加version字段
alter table `linkis_resources_download_history` add column `version` varchar(20) not null after `resource_id`;

create table dws_bml_resources_contentType (
	`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `ext` varchar(8) not null comment '文件后缀名',
  `content_type` varchar(16) not null comment '文件content-type',
  `range` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否需要断点续传，0表示不需要，1表示需要',
	PRIMARY KEY (`id`),
  UNIQUE KEY `whitelist_contentType_uindex` (`content_type`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建资源任务表,包括上传,更新,下载
CREATE TABLE `linkis_resources_task` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `resource_id` varchar(50) DEFAULT NULL COMMENT '资源id，资源的uuid',
  `version` varchar(20) DEFAULT NULL COMMENT '当前操作的资源版本号',
  `operation` varchar(20) NOT NULL COMMENT '操作类型.upload = 0, update = 1',
  `state` varchar(20) NOT NULL DEFAULT 'Schduled' COMMENT '任务当前状态:Schduled, Running, Succeed, Failed,Cancelled',
  `submit_user` varchar(20) NOT NULL DEFAULT '' COMMENT '任务提交用户名',
  `system` varchar(20) NOT NULL DEFAULT '' COMMENT '子系统名 wtss',
  `instance` varchar(50) NOT NULL COMMENT '物料库实例',
  `client_ip` varchar(50) DEFAULT NULL COMMENT '请求IP',
  `extra_params` text COMMENT '额外关键信息.如批量删除的资源IDs及versions,删除资源下的所有versions',
  `err_msg` varchar(2000) DEFAULT NULL COMMENT '任务失败信息.e.getMessage',
  `start_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `last_update_time` datetime NOT NULL COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `resource_id_version` (`resource_id`,`version`, `operation`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
