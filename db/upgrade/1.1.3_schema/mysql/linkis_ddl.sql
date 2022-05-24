

-- cs table
ALTER TABLE `linkis_ps_cs_context_map` ADD COLUMN `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'update unix timestamp' AFTER `keywords`;
ALTER TABLE `linkis_ps_cs_context_map` ADD COLUMN `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time' AFTER `update_time`;

ALTER TABLE `linkis_ps_cs_context_map_listener` ADD COLUMN `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'update unix timestamp' AFTER `key_id`;
ALTER TABLE `linkis_ps_cs_context_map_listener` ADD COLUMN `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time' AFTER `update_time`;

ALTER TABLE `linkis_ps_cs_context_history` ADD COLUMN `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'update unix timestamp' AFTER `keyword`;
ALTER TABLE `linkis_ps_cs_context_history` ADD COLUMN `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time' AFTER `update_time`;

ALTER TABLE `linkis_ps_cs_context_id` ADD COLUMN `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'update unix timestamp' AFTER `backup_instance`;
ALTER TABLE `linkis_ps_cs_context_id` ADD COLUMN `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time' AFTER `update_time`;

ALTER TABLE `linkis_ps_cs_context_listener` ADD COLUMN `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'update unix timestamp' AFTER `context_id`;
ALTER TABLE `linkis_ps_cs_context_listener` ADD COLUMN `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time' AFTER `update_time`;


-- manager ec record
DROP TABLE IF EXISTS `linkis_cg_ec_resource_info_record`;
CREATE TABLE `linkis_cg_ec_resource_info_record` (
	`id` INT(20) NOT NULL AUTO_INCREMENT,
	`label_value` VARCHAR(255) NOT NULL,
	`create_user` VARCHAR(128) NOT NULL,
	`service_instance` varchar(128) COLLATE utf8_bin DEFAULT NULL,
	`ecm_instance` varchar(128) COLLATE utf8_bin DEFAULT NULL,
	`ticket_id` VARCHAR(100) NOT NULL,
	`log_dir_suffix` varchar(128) COLLATE utf8_bin DEFAULT NULL,
	`request_times` INT(8),
	`request_resource` VARCHAR(255),
	`used_times` INT(8),
	`used_resource` VARCHAR(255),
	`release_times` INT(8),
	`released_resource` VARCHAR(255),
	`release_time` datetime DEFAULT NULL,
	`used_time` datetime DEFAULT NULL,
	`create_time` datetime DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY (`id`),
	KEY (`ticket_id`),
	UNIQUE KEY `label_value_ticket_id` (`ticket_id`,`label_value`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

