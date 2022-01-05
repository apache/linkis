/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
DROP TABLE IF EXISTS `linkis_ps_error_code`;
CREATE TABLE `linkis_ps_error_code` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `error_code` varchar(50) NOT NULL,
  `error_desc` varchar(1024) NOT NULL,
  `error_regex` varchar(1024) DEFAULT NULL,
  `error_type` int(3) DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (1,'10001','会话创建失败，%s队列不存在，请检查队列设置是否正确','queue (\\S+) is not exists in YARN',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (2,'10001','会话创建失败，用户%s不能提交应用到队列：%s，请检查队列设置是否正确','User (\\S+) cannot submit applications to queue (\\S+)',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (3,'20001','Session创建失败，当前申请资源%s，队列可用资源%s,请检查资源配置是否合理','您本次向任务队列（[a-zA-Z_0-9\\.]+）请求资源（(.+)），任务队列最大可用资源（.+），任务队列剩余可用资源（(.+)）您已占用任务队列资源（.+）',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (4,'20002','Session创建失败，服务器资源不足，请稍后再试','远程服务器没有足够资源实例化[a-zA-Z]+ Session，通常是由于您设置【驱动内存】或【客户端内存】过高导致的，建议kill脚本，调低参数后重新提交！等待下次调度...',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (5,'20003','Session创建失败，队列资源不足，请稍后再试','request resources from ResourceManager has reached 560++ tries, give up and mark it as FAILED.',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (6,'20003','文件%s不存在','Caused by:\\s*java.io.FileNotFoundException',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (7,'20083','Java进程内存溢出','OutOfMemoryError',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (8,'30001','%s无权限访问，请申请开通数据表权限','Permission denied:\\s*user=[a-zA-Z0-9_]+,\\s*access=[A-Z]+\\s*,\\s*inode="([a-zA-Z0-9/_\\.]+)"',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (9,'40001','数据库%s不存在，请检查引用的数据库是否有误','Database ''([a-zA-Z_0-9]+)'' not found',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (10,'40001','数据库%s不存在，请检查引用的数据库是否有误','Database does not exist: ([a-zA-Z_0-9]+)',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (11,'40002','表%s不存在，请检查引用的表是否有误','Table or view not found: ([`\\.a-zA-Z_0-9]+)',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (12,'40002','表%s不存在，请检查引用的表是否有误','Table not found ''([a-zA-Z_0-9]+)''',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (13,'40003','字段%s不存在，请检查引用的字段是否有误','cannot resolve ''`(.+)`'' given input columns',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (14,'40003','字段%s不存在，请检查引用的字段是否有误',' Invalid table alias or column reference ''(.+)'':',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (15,'40004','分区字段%s不存在，请检查引用的表%s是否为分区表或分区字段有误','([a-zA-Z_0-9]+) is not a valid partition column in table ([`\\.a-zA-Z_0-9]+)',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (16,'40004','分区字段%s不存在，请检查引用的表是否为分区表或分区字段有误','Partition spec \\{(\\S+)\\} contains non-partition columns',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (17,'40004','分区字段%s不存在，请检查引用的表是否为分区表或分区字段有误','table is not partitioned but partition spec exists:\\{(.+)\\}',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (18,'50001','括号不匹配，请检查代码中括号是否前后匹配','extraneous input ''\\)''',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (20,'50002','非聚合函数%s必须写在group by中，请检查代码的group by语法','expression ''(\\S+)'' is neither present in the group by',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (21,'50002','非聚合函数%s必须写在group by中，请检查代码的group by语法','grouping expressions sequence is empty,\\s?and ''(\\S+)'' is not an aggregate function',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (22,'50002','非聚合函数%s必须写在group by中，请检查代码的group by语法','Expression not in GROUP BY key ''(\\S+)''',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (23,'50003','未知函数%s，请检查代码中引用的函数是否有误','Undefined function: ''(\\S+)''',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (24,'50003','未知函数%s，请检查代码中引用的函数是否有误','Invalid function ''(\\S+)''',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (25,'50004','字段%s存在名字冲突，请检查子查询内是否有同名字段','Reference ''(\\S+)'' is ambiguous',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (26,'50004','字段%s存在名字冲突，请检查子查询内是否有同名字段','Ambiguous column Reference ''(\\S+)'' in subquery',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (27,'50005','字段%s必须指定表或者子查询别名，请检查该字段来源','Column ''(\\S+)'' Found in more than One Tables/Subqueries',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (28,'50006','表%s在数据库%s中已经存在，请删除相应表后重试','Table or view ''(\\S+)'' already exists in database ''(\\S+)''',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (29,'50006','表%s在数据库中已经存在，请删除相应表后重试','Table (\\S+) already exists',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (30,'50006','表%s在数据库中已经存在，请删除相应表后重试','Table already exists',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (31,'50006','表%s在数据库中已经存在，请删除相应表后重试','AnalysisException: (S+) already exists',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (32,'64001','找不到导入文件地址：%s','java.io.FileNotFoundException: (\\S+) \\(No such file or directory\\)',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (33,'64002','导出为excel时临时文件目录权限异常','java.io.IOException: Permission denied(.+)at org.apache.poi.xssf.streaming.SXSSFWorkbook.createAndRegisterSXSSFSheet',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (34,'64003','导出文件时无法创建目录：%s','java.io.IOException: Mkdirs failed to create (\\S+) (.+)',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (35,'50008','导入模块错误，系统没有%s模块，请联系运维人员安装','ImportError: No module named (S+)',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (36,'50009','插入目标表字段数量不匹配,请检查代码！','requires that the data to be inserted have the same number of columns as the target table',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (38,'50011','数据类型不匹配，请检查代码！','due to data type mismatch: differing types in',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (39,'50012','字段%s引用有误，请检查字段是否存在！','Invalid column reference (S+)',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (40,'50013','字段%s提取数据失败','Can''t extract value from (S+): need struct type but got string',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (41,'50014','括号或者关键字不匹配，请检查代码！','mismatched input ''(\\S+)'' expecting',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (42,'50015','group by 位置2不在select列表中，请检查代码！','GROUP BY position (S+) is not in select list',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (43,'50016','代码中存在NoneType空类型变量，请检查代码','''NoneType'' object',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (44,'50017','数组越界','IndexError:List index out of range',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (45,'50013','字段提取数据失败请检查字段类型','Can''t extract value from (S+): need struct type but got string',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (46,'50018','插入数据未指定目标表字段%s，请检查代码！','Cannot insert into target table because column number/types are different ''(S+)''',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (47,'50019','表别名%s错误，请检查代码！','Invalid table alias ''(\\S+)''',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (48,'50020','UDF函数未指定参数，请检查代码！','UDFArgumentException Argument expected',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (49,'50021','聚合函数%s不能写在group by 中，请检查代码！','aggregate functions are not allowed in GROUP BY',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (50,'50007','您的代码有语法错误，请您修改代码之后执行','SyntaxError',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (51,'40002','表不存在，请检查引用的表是否有误','Table not found',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (52,'40003','函数使用错误，请检查您使用的函数方式','No matching method',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (53,'50032','用户主动kill任务','is killed by user',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (54,'60001','python代码变量%s未定义','name ''(S+)'' is not defined',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (55,'60002','python udf %s 未定义','Undefined function:s+''(S+)''',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (56,'50007','您的sql代码可能有语法错误，请检查sql代码','FAILED: ParseException',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (57,'50007','您的sql代码可能有语法错误，请检查sql代码','org.apache.spark.sql.catalyst.parser.ParseException',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (58,'60003','脚本语法有误','ParseException:',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (59,'60010','您可能没有相关权限','Permission denied',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (60,'61027','python执行不能将%s和%s两种类型进行连接','cannot concatenate ''(S+)'' and ''(S+)''',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (61,'60020','pyspark执行失败，可能是语法错误或stage失败','Py4JJavaError: An error occurred',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (62,'61028','python代码缩进对齐有误','unexpected indent',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (63,'60078','个人库超过限制','is exceeded',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (64,'69582','python代码缩进有误','unexpected indent',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (65,'69583','python代码反斜杠后面必须换行','unexpected character after line',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (66,'60091','导出Excel表超过最大限制1048575','Invalid row number',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (67,'60092','python save as table未指定格式，默认用parquet保存，hive查询报错','parquet.io.ParquetDecodingException',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (68,'11011','远程服务器内存资源不足','errCode: 11011',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (69,'11012','远程服务器CPU资源不足','errCode: 11012',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (70,'11013','远程服务器实例资源不足','errCode: 11013',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (71,'11014','队列CPU资源不足','errCode: 11014',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (72,'11015','队列内存资源不足','errCode: 11015',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (73,'11016','队列实例数超过限制','errCode: 11016',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (74,'11017','超出全局计算引擎实例限制','errCode: 11017',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (75,'60035','资源不足，启动引擎失败','资源不足',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (76,'60075','获取Yarn队列信息异常,可能是您设置的yarn队列不存在','获取Yarn队列信息异常',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (78,'95001','找不到变量值，请确认您是否设置相关变量','not find variable substitution for',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (79,'95002','不存在的代理用户，请检查你是否申请过平台层（bdp或者bdap）用户','failed to change current working directory ownership',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (80,'95003','请检查提交用户在WTSS内是否有该代理用户的权限，代理用户中是否存在特殊字符，是否用错了代理用户，OS层面是否有该用户，系统设置里面是否设置了该用户为代理用户','没有权限执行当前任务',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (81,'95004','平台层不存在您的执行用户，请在ITSM申请平台层（bdp或者bdap）用户','使用chown命令修改',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (82,'95005','未配置代理用户，请在ITSM走WTSS用户变更单，为你的用户授权改代理用户','请联系系统管理员为您的用户添加该代理用户',0);
INSERT INTO linkis_ps_error_code (id,error_code,error_desc,error_regex,error_type) VALUES (83,'95006','Could not open input file for reading%does not exist','JobServer中不存在您的脚本文件，请将你的脚本文件放入对应的JobServer路径中',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('60079','所查库表无权限','Authorization failed:No privilege',0);