ALTER TABLE `linkis_ps_job_history_group_history` CHANGE COLUMN `params` `params` TEXT NULL COMMENT 'job params' AFTER `params`;
ALTER TABLE `linkis_ps_job_history_group_history` ADD COLUMN `result_location` VARCHAR(500) NULL DEFAULT NULL COMMENT 'File path of the resultsets' AFTER `result_location`;

RENAME TABLE `linkis_ps_udf_shared_user` TO `linkis_ps_udf_shared_info`;

ALTER TABLE `linkis_ps_udf_shared_group` CHANGE COLUMN `user_name` `shared_group` VARCHAR(50) NOT NULL AFTER `udf_id`;

RENAME TABLE `linkis_ps_udf_user_load_info` TO `linkis_ps_udf_user_load`;



DROP TABLE IF EXISTS `linkis_ps_udf_baseinfo`;
CREATE TABLE `linkis_ps_udf_baseinfo` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `create_user` varchar(50) NOT NULL,
  `udf_name` varchar(255) NOT NULL,
  `udf_type` int(11) DEFAULT '0',
  `tree_id` bigint(20) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `sys` varchar(255) NOT NULL DEFAULT 'ide' COMMENT 'source system',
  `cluster_name` varchar(255) NOT NULL,
  `is_expire` bit(1) DEFAULT NULL,
  `is_shared` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- linkis_ps_udf_version definition
DROP TABLE IF EXISTS `linkis_ps_udf_version`;
CREATE TABLE `linkis_ps_udf_version` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `udf_id` bigint(20) NOT NULL,
  `path` varchar(255) NOT NULL COMMENT 'Source path for uploading files',
  `bml_resource_id` varchar(50) NOT NULL,
  `bml_resource_version` varchar(20) NOT NULL,
  `is_published` bit(1) DEFAULT NULL COMMENT 'is published',
  `register_format` varchar(255) DEFAULT NULL,
  `use_format` varchar(255) DEFAULT NULL,
  `description` varchar(255) NOT NULL COMMENT 'version desc',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `md5` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

SET FOREIGN_KEY_CHECKS=0;


ALTER TABLE `linkis_cg_manager_label_resource`         ADD UNIQUE INDEX  `label_id` (`label_id`);

ALTER TABLE `linkis_cg_manager_label_service_instance` ADD KEY           `label_serviceinstance` (`label_id`,`service_instance`);
ALTER TABLE `linkis_cg_manager_label_service_instance` ADD INDEX         `label_serviceinstance` (`label_id`, `service_instance`);