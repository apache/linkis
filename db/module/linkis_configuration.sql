SET FOREIGN_KEY_CHECKS=0;


-- ----------------------------
-- Table structure for linkis_application
-- ----------------------------
DROP TABLE IF EXISTS `linkis_application`;
CREATE TABLE `linkis_application` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) DEFAULT NULL COMMENT 'Can be one of the following: execute_application_name(in table linkis_task), request_application_name(i.e. creator), general configuration',
  `chinese_name` varchar(50) DEFAULT NULL,
  `description` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;


-- ----------------------------
-- Table structure for linkis_config_key_tree
-- ----------------------------
DROP TABLE IF EXISTS `linkis_config_key_tree`;
CREATE TABLE `linkis_config_key_tree` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `key_id` bigint(20) DEFAULT NULL,
  `tree_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `key_id` (`key_id`),
  KEY `tree_id` (`tree_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;


-- ----------------------------
-- Table structure for linkis_config_key_user
-- ----------------------------
DROP TABLE IF EXISTS `linkis_config_key_user`;
CREATE TABLE `linkis_config_key_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `application_id` bigint(20) DEFAULT NULL COMMENT 'Same as id in tale linkis_application, except that it cannot be the id of creator',
  `key_id` bigint(20) DEFAULT NULL,
  `user_name` varchar(50) DEFAULT NULL,
  `value` varchar(200) DEFAULT NULL COMMENT 'Value of the key',
  PRIMARY KEY (`id`),
  UNIQUE KEY `application_id_2` (`application_id`,`key_id`,`user_name`),
  KEY `key_id` (`key_id`),
  KEY `application_id` (`application_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ----------------------------
-- Table structure for linkis_config_key
-- ----------------------------
DROP TABLE IF EXISTS `linkis_config_key`;
CREATE TABLE `linkis_config_key` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `key` varchar(50) DEFAULT NULL COMMENT 'Set key, e.g. spark.executor.instances',
  `description` varchar(200) DEFAULT NULL,
  `name` varchar(50) DEFAULT NULL,
  `application_id` bigint(20) DEFAULT NULL COMMENT 'Correlate with id in table linkis_application',
  `default_value` varchar(200) DEFAULT NULL COMMENT 'Adopted when user does not set key',
  `validate_type` varchar(50) DEFAULT NULL COMMENT 'Validate type, one of the following: None, NumInterval, FloatInterval, Include, Regex, OPF, Custom Rules',
  `validate_range` varchar(100) DEFAULT NULL COMMENT 'Validate range',
  `is_hidden` tinyint(1) DEFAULT NULL COMMENT 'Whether it is hidden from user. If set to 1(true), then user cannot modify, however, it could still be used in back-end',
  `is_advanced` tinyint(1) DEFAULT NULL COMMENT 'Whether it is an advanced parameter. If set to 1(true), parameters would be displayed only when user choose to do so',
  `level` tinyint(1) DEFAULT NULL COMMENT 'Basis for displaying sorting in the front-end. Higher the level is, higher the rank the parameter gets',
  `unit` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `application_id` (`application_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;


-- ----------------------------
-- Table structure for linkis_config_tree
-- ----------------------------
DROP TABLE IF EXISTS `linkis_config_tree`;
CREATE TABLE `linkis_config_tree` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `parent_id` bigint(20) DEFAULT NULL COMMENT 'Parent ID',
  `name` varchar(50) DEFAULT NULL COMMENT 'Application name or category name under general configuration',
  `description` varchar(200) DEFAULT NULL,
  `application_id` bigint(20) DEFAULT NULL COMMENT 'Same as id(in table linkis_application), except that it cannot be the id of creator',
  PRIMARY KEY (`id`),
  KEY `application_id` (`application_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;
