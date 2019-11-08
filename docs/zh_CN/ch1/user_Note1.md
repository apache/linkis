基于开源代码编译、部署成功，执行HQL加条件走MR报错的解决方案总结 

问题描述：

按照社区版（标准）部署成功，执行hql全表查询，无问题，不走MR（非TEZ），最近加上where条件，走MR（非TEZ），出现Hadoop major version number: 3.0.0-cdh6.1.1 不兼容的情况，因为我们cdh版本是6.1.1的，hadoop版本是3.0.0，得出结论：现有环境跟社区版搭建不兼容。

解决方案：

思路：由于我们通过第三方客户端dbeaver连接我们集群的hive，所有复杂操作都可正常执行，由此带来启发。为此我们更换兼容我们公司现有环境编译、部署，发现还有部分jar包冲突的问题，为此我们更换jar包，在实践的过程中总结如下具体解决方案：
1、首先删除jetty-all相关jar包 ----->jetty-all-7.6.0.v20120127.jar
2、其次把hive-em的这些包干掉：jasper-*
3、还有一个类似问题就是原生态hadoop下启动hive-1.1.0-cdh5.10.0报错提示：
Exception in thread "main" java.lang.NoClassDefFoundError: org/apache/hadoop/mapred/MRVersi
将下载的包拷贝到hive em下面，重启hive em即可。
参考链接：https://blog.csdn.net/bornzhu/article/details/80272906
4、所有组件lib里面：curator-client-2.7.0.jar--->curator-client-4.0.1.jar。
至此问题得到解决！
欢迎大家补充，一起为开源贡献自己的力量。