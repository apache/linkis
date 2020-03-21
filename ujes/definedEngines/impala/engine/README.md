第一步：下载impala 源码 https://github.com/cloudera/Impala.git

第二步：common/thrift 目录

第三步：生成java代码
generate_error_codes.py
thrift -r --gen java ErrorCodes.thrift
thrift -r --gen java ExecStats.thrift
thrift -r --gen java Metrics.thrift
thrift -r --gen java RuntimeProfile.thrift
thrift -r --gen java Status.thrift
thrift -r --gen java Types.thrift
thrift -r --gen java TCLIService.thrift（hive-1-api目录）

移除：删除语句"include "beeswax.thrift"和"service ImpalaService extends beeswax.BeeswaxService"这个类的定义（impala源码未编译缺失hive_metestore.thrift，impala引擎暂不用该文件，所以直接移除。）
thrift -r --gen java ImpalaService.thrift

第四步：
这一步会生成三个包的java文件，直接拷贝到impala-engine引擎包即可：
1.org.apache.impala.thrift
2.org.apache.hive.service.cli.thrift
3.com.cloudera.beeswax