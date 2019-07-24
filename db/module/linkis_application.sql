SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for linkis_develop_application
-- ----------------------------
DROP TABLE IF EXISTS `linkis_develop_application`;
CREATE TABLE `linkis_develop_application` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) DEFAULT NULL,
  `source` varchar(50) DEFAULT NULL COMMENT 'Source of the development application',
  `version` varchar(50) DEFAULT NULL,
  `description` text,
  `user_id` bigint(20) DEFAULT NULL,
  `is_published` bit(1) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `org_id` bigint(20) DEFAULT NULL COMMENT 'Organization ID',
  `visibility` bit(1) DEFAULT NULL,
  `is_transfer` bit(1) DEFAULT NULL COMMENT 'Reserved word',
  `initial_org_id` bigint(20) DEFAULT NULL,
  `json_path` varchar(255) DEFAULT NULL COMMENT 'Path of the jason file which is used for data development in the front-end.',
  `isAsh` bit(1) DEFAULT NULL COMMENT 'If it is active',
  `pic` varchar(255) DEFAULT NULL,
  `star_num` int(11) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;


-- ----------------------------
-- Table structure for linkis_project_list
-- ----------------------------
DROP TABLE IF EXISTS `linkis_project_list`;
CREATE TABLE `linkis_project_list` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) DEFAULT NULL COMMENT 'Project service name which needs to be initialized',
  `is_project_need_init` bit(1) DEFAULT NULL,
  `url` varchar(255) DEFAULT NULL COMMENT 'URL used to initialize a project',
  `is_user_need_init` bit(1) DEFAULT NULL,
  `is_project_inited` bit(1) DEFAULT NULL,
  `json` text COMMENT 'Data provided by project to the front-end would be jsonized after initialization.',
  `level` tinyint(255) DEFAULT NULL COMMENT 'Marks the importance of the project. When encounter initialization failure, if a user tried to log in, the project would report an error if its level is greater than 4, otherwise, grey the corresponding function button',
  `user_init_url` varchar(255) DEFAULT NULL COMMENT 'URL used to initialize a user',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;


-- ----------------------------
-- Table structure for linkis_project_user
-- ----------------------------
DROP TABLE IF EXISTS `linkis_project_user`;
CREATE TABLE `linkis_project_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `project_id` bigint(20) DEFAULT NULL,
  `json` varchar(255) DEFAULT NULL COMMENT 'Data returned by initializing a user would be jsonized',
  `user_id` bigint(20) DEFAULT NULL,
  `is_init_success` bit(1) DEFAULT NULL,
  `is_new_feature` bit(1) DEFAULT NULL COMMENT 'If this project is a new function to the user',
  PRIMARY KEY (`id`),
  UNIQUE KEY `project_id` (`project_id`,`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ----------------------------
-- Table structure for linkis_user
-- ----------------------------
DROP TABLE IF EXISTS `linkis_user`;
CREATE TABLE `linkis_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `email` varchar(255) DEFAULT NULL,
  `username` varchar(255) NOT NULL,
  `password` varchar(255) DEFAULT NULL,
  `admin` tinyint(1) DEFAULT NULL COMMENT 'If it is an administrator',
  `active` tinyint(1) DEFAULT NULL COMMENT 'If it is active',
  `name` varchar(255) DEFAULT NULL COMMENT 'User name',
  `description` varchar(255) DEFAULT NULL,
  `department` varchar(255) DEFAULT NULL,
  `avatar` varchar(255) DEFAULT NULL COMMENT 'Path of the avator',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` bigint(20) DEFAULT '0',
  `update_time` timestamp NOT NULL DEFAULT '1970-01-01 08:00:01',
  `update_by` bigint(20) DEFAULT '0',
  `is_first_login` bit(1) DEFAULT NULL COMMENT 'If it is the first time to log in',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

