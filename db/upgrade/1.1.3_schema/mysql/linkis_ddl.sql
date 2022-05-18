

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
