DROP TABLE IF EXISTS `linkis_engine_conn_plugin_bml_resources`;
CREATE TABLE `linkis_engine_conn_plugin_bml_resources` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `engine_conn_type` varchar(100) NOT NULL COMMENT '引擎类型',
  `version` varchar(100) COMMENT '版本',
  `file_name` varchar(255) COMMENT '文件名',
  `file_size` bigint(20)  DEFAULT 0 NOT NULL COMMENT '文件大小',
  `last_modified` bigint(20)  COMMENT '文件更新时间',
  `bml_resource_id` varchar(100) NOT NULL COMMENT '所属系统',
  `bml_resource_version` varchar(200) NOT NULL COMMENT '资源所属者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;