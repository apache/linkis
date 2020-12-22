INSERT INTO `linkis_application` (`id`, `name`, `chinese_name`, `description`) VALUES (0, 'presto', NULL, NULL);

SELECT @application_id := id from linkis_application where name = 'presto';
INSERT INTO `linkis_config_tree` (`id`, `parent_id`, `name`, `description`, `application_id`) VALUES (0, '0', 'presto引擎设置', NULL, @application_id);
INSERT INTO `linkis_config_tree` (`id`, `parent_id`, `name`, `description`, `application_id`) VALUES (0, '0', 'presto资源设置', NULL, @application_id);


INSERT INTO `linkis_config_key` (`id`, `key`, `description`, `name`, `application_id`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`) VALUES (0, 'wds.linkis.instance', '范围：1-20，单位：个', 'presto引擎最大并发数', @application_id, '20', 'NumInterval', '[1,20]', '0', '0', '1');
INSERT INTO `linkis_config_key` (`id`, `key`, `description`, `name`, `application_id`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`) VALUES (0, 'presto.session.query_max_total_memory', '取值范围：1-100，单位：GB/MB', 'presto单次任务最大使用内存', @application_id, '20G', 'Regex', '^([1-9]\\d{0,2}|1000)(GB|MB|gb|mb)$', '0', '0', '1');
INSERT INTO `linkis_config_key` (`id`, `key`, `description`, `name`, `application_id`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`) VALUES (0, 'presto.session.query_max_run_time', '单位：s（秒）/m（分）', 'presto单次任务最大耗时', @application_id, '30m', 'Regex', '^([1-9]\\d{0,2}|1000)(m|s)$', '0', '0', '1');

#------------------------presto---------------------
SELECT @key_id := k.id from linkis_config_key k left join linkis_application a on k.application_id = a.id WHERE k.`name` = 'presto引擎最大并发数' and a.name = 'IDE' ;
SELECT @tree_id := t.id from linkis_config_tree t left join linkis_application a on t.application_id = a.id  WHERE t.`name` = 'presto引擎设置' and a.name = 'presto' ;
INSERT INTO `linkis_config_key_tree` (`id`, `key_id`, `tree_id`) VALUES (0, @key_id, @tree_id);

SELECT @key_id := k.id from linkis_config_key k left join linkis_application a on k.application_id = a.id WHERE k.`key` = 'presto.session.query_max_total_memory' and a.name = 'IDE' ;
SELECT @tree_id := t.id from linkis_config_tree t left join linkis_application a on t.application_id = a.id  WHERE t.`name` = 'presto资源设置' and a.name = 'presto' ;
INSERT INTO `linkis_config_key_tree` (`id`, `key_id`, `tree_id`) VALUES (0, @key_id, @tree_id);

SELECT @key_id := k.id from linkis_config_key k left join linkis_application a on k.application_id = a.id WHERE k.`key` = 'presto.session.query_max_run_time' and a.name = 'IDE';
SELECT @tree_id := t.id from linkis_config_tree t left join linkis_application a on t.application_id = a.id  WHERE t.`name` = 'presto资源设置' and a.name = 'presto' ;
INSERT INTO `linkis_config_key_tree` (`id`, `key_id`, `tree_id`) VALUES (0, @key_id, @tree_id);