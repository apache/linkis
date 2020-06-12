CREATE TABLE IF NOT EXISTS `linkis_datasource` (
   `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
   `datasource_name` VARCHAR(100) NOT NULL COMMENT 'Data source name',
   `datasource_type_id` BIGINT(20) DEFAULT NULL COMMENT 'Data source type id',
   `datasource_desc` VARCHAR(200) DEFAULT NULL COMMENT 'Data source description',
   `create_identify` VARCHAR(20) DEFAULT 'BDP' COMMENT 'Example: project name',
	`create_system` VARCHAR(20) DEFAULT 'BDP' COMMENT 'Create system',
	`create_user` VARCHAR(50) DEFAULT NULL COMMENT 'Creator',
	`parameter` TEXT COMMENT 'Connect parameters',
	`create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	`modify_user` VARCHAR(50) DEFAULT NULL COMMENT 'Modify user',
	`modify_time` DATETIME DEFAULT NULL COMMENT 'Modify time',
	`datasource_env_id` BIGINT(20) DEFAULT NULL,
	PRIMARY KEY (`id`)
 ) ENGINE=InnoDB AUTO_INCREMENT=140 DEFAULT CHARSET=utf8;


CREATE TABLE IF NOT EXISTS `linkis_datasource_env` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`env_name` VARCHAR(100) NOT NULL COMMENT 'Environment name',
    `env_desc` VARCHAR(200) DEFAULT NULL COMMENT 'Description',
	`create_user` VARCHAR(50) DEFAULT NULL COMMENT 'Creator',
	`parameter` TEXT NOT NULL COMMENT 'Connect parameters',
	`create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	`modify_user` VARCHAR(50) DEFAULT NULL COMMENT 'Modify user',
	`modify_time` DATETIME DEFAULT NULL COMMENT 'Modify time',
	PRIMARY KEY (`id`)
 ) ENGINE=InnoDB AUTO_INCREMENT=108 DEFAULT CHARSET=utf8;


CREATE TABLE IF NOT EXISTS `linkis_datasource_type_key` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`key` VARCHAR(50) DEFAULT NULL COMMENT 'Key of variable',
	`description` VARCHAR(200) DEFAULT NULL COMMENT 'Description',
	`name` VARCHAR(50) DEFAULT NULL COMMENT 'Option name of column in page',
	`data_source_type_id` BIGINT(20) DEFAULT NULL COMMENT 'Type id',
	`require` TINYINT(1) DEFAULT '0',
	`scope` VARCHAR(50) DEFAULT NULL COMMENT 'Scope',
	`default_value` VARCHAR(200) DEFAULT NULL COMMENT 'Default value',
	`value_type` VARCHAR(50) DEFAULT NULL COMMENT 'Value type',
	`value_regex` VARCHAR(100) DEFAULT NULL COMMENT 'Value regex',
	`ref_id` BIGINT(20) DEFAULT NULL COMMENT 'Related id',
	`ref_value` VARCHAR(100) DEFAULT NULL COMMENT 'Related value',
	PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE IF NOT EXISTS `linkis_datasource_type` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`icon` VARCHAR(50) DEFAULT NULL COMMENT 'Icon',
	`description` VARCHAR(200) DEFAULT NULL COMMENT 'Description',
	`name` VARCHAR(50) DEFAULT NULL COMMENT 'Name',
	PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE IF NOT EXISTS `linkis_datasource_type_env` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`data_source_type_id` BIGINT(20) DEFAULT NULL COMMENT 'Type id',
	`env_id` BIGINT(20) DEFAULT NULL COMMENT 'Environment id',
	PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


INSERT INTO `linkis_datasource_type`(`icon`, `name`) VALUES('0x001', 'ElasticSearch');
INSERT INTO `linkis_datasource_type`(`icon`, `name`) VALUES('0x001', 'Hive');
INSERT INTO `linkis_datasource_type`(`icon`, `name`) VALUES('0x001', 'MySql');