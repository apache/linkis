-- ----------------------------
-- Table structure for linkis_mg_gateway_auth_token
-- ----------------------------
DROP TABLE IF EXISTS `linkis_mg_gateway_auth_token`;
CREATE TABLE `linkis_mg_gateway_auth_token` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `token_name` varchar(128) NOT NULL,
  `legal_users` text,
  `legal_hosts` text,
  `business_owner` varchar(32),
  `create_time` DATE DEFAULT NULL,
  `update_time` DATE DEFAULT NULL,
  `elapse_day` BIGINT DEFAULT NULL,
  `update_by` varchar(32),
  PRIMARY KEY (`id`),
  UNIQUE KEY `token_name` (`token_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
INSERT INTO `linkis_mg_gateway_auth_token`(
  `token_name`,
  `legal_users`,
  `legal_hosts`,
  `business_owner`,
  `create_time`,
  `update_time`,
  `elapse_day`,
  `update_by`
) VALUES
('QML-AUTH','*','*','BDP',curdate(),curdate(),-1,'LINKIS'),
('BML-AUTH','*','*','BDP',curdate(),curdate(),-1,'LINKIS'),
('WS-AUTH','*','*','BDP',curdate(),curdate(),-1,'LINKIS'),
('dss-AUTH','*','*','BDP',curdate(),curdate(),-1,'LINKIS'),
('QUALITIS-AUTH','*','*','BDP',curdate(),curdate(),-1,'LINKIS'),
('VALIDATOR-AUTH','*','*','BDP',curdate(),curdate(),-1,'LINKIS'),
('LINKISCLI-AUTH','*','*','BDP',curdate(),curdate(),-1,'LINKIS'),
('test1','hduser05,hduser06,hduser07','127.0.0.1,127.0.0.1,127.0.0.1',"test",curdate(),curdate(),-1,'LINKIS'),
('test2','*','127.0.0.1,127.0.0.1,127.0.0.1',"test",curdate(),curdate(),-1,'LINKIS'),
('test3','hduser05,hduser06,hduser07','*',"test",curdate(),curdate(),-1,'LINKIS'),
('test4','*','*',"test",curdate(),curdate(),0,'LINKIS');