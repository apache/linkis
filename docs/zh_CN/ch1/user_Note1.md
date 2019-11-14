作为用户在使用和部署过程中遇到的问题以及部分问题解决方案汇总【CDH版本】

问题一：基于开源代码编译、部署成功，执行HQL加条件走MR报错的解决方案总结【HQL】

问题描述：

按照社区版（标准）部署成功，执行hql全表查询，无问题，不走MR（非TEZ），加上where条件，走MR（非TEZ），出现Hadoop major version number: 3.0.0-cdh6.1.1 不兼容的情况，因为我们cdh版本是6.1.1的，hadoop版本是3.0.0，得出结论：现有环境部署跟社区版搭建不兼容。

解决方案：

思路：由于我们通过第三方客户端dbeaver连接我们集群的hive，所有复杂操作都可正常执行：
第三方客户端dbeave连接3.0.0-cdh6.1.1集群hive所依赖的jar包如下截图：
![Image text](https://github.com/zhanghaicheng1/Linkis/blob/master/docs/zh_CN/images/user_Image/dbeave_hive_lib.png)
考虑我们集群版本较新，部分依赖jar包阿里私服、maven仓库以及我们公司私服都没有相关jar包，
结合dbeave带来的启发。考虑用较低版本的jar包来代替我们现有环境的依赖。
为此我们更换兼容我们公司现有环境对linkis进行编译、部署，发现还有部分jar包冲突的问题，为此我们更换jar包，在实践的过程中总结如下具体解决方案：
1、首先删除所有组件里面：jetty-all相关jar包 ----->jetty-all-7.6.0.v20120127.jar
2、其次把hive-em的这些包干掉：jasper-* ---> cdh部署需要删掉这些包[在部署过程中因为这个问题跳了好几次坑，建议将所有组件中包含此包都给干掉]
3、还有一个类似问题就是原生态hadoop下启动hive-1.1.0-cdh5.10.0报错提示：
Exception in thread "main" java.lang.NoClassDefFoundError: org/apache/hadoop/mapred/MRVersi
参考链接：https://blog.csdn.net/bornzhu/article/details/80272906
将下载的包拷贝到hive em下面，重启hive em即可。
4、所有组件lib里面：curator-client-2.7.0.jar--->curator-client-4.0.1.jar。

问题二：udf函数使用问题【SPARK】

问题描述：

1、sql无法识别scala创建的udf；  
2、scala创建的脚本udf（方法）没有注册，可以直接调用。

解决方案：目前暂未解决，已提交issue，确定为bug。


问题三：hive执行提示未在资源管理器（RM）里面注册的问题【HQL】

问题描述：

当前部署成功查询，过几天（间隔性）再去查询hql，提示如下错误：
![Image text](https://github.com/zhanghaicheng1/Linkis/blob/master/docs/zh_CN/images/user_Image/hive_engineManager_error.png)

解决方案：
重启linkis-ujes-hive-engineManager组件即可解决问题。


问题四：执行sparksql提示60035资源不足，启动引擎失败【SPAKR_SQL】

问题描述：

ERROR feign.FeignException: status 404 reading RPCReceiveRemote#receiveAndReply(Message); content:
{"timestamp":"2019-11-13T05:28:06.507+0000","status":404,"error":"Not Found","message":"/api/rest_j/v1/rpc/receiveAndReply","path":"/api/rest_j/v1/rpc/receiveAndReply"}
![Image text](https://github.com/zhanghaicheng1/Linkis/blob/master/docs/zh_CN/images/user_Image/sparksql-error.png)

解决方案：
把spark-em的这些包干掉：jasper-*，其他类似问题可以通过相同方案来解决。


问题五：spark 访问sql 提示 HIVE_STATS_JDBC_TIMEOUT.png

问题描述：
ERROR [sparkEngineEngine-Thread-2] com.webank.wedatasphere.linkis.engine.executors.SparkSqlExecutor 59 error - execute sparkSQL failed! java.lang.NoSuchFieldError: HIVE_STATS_JDBC_TIMEOUT
![Image text](https://github.com/zhanghaicheng1/Linkis/blob/master/docs/zh_CN/images/user_Image/HIVE_STATS_JDBC_TIMEOUT.png)

解决方案：

将集群spark组件中jars目录的spark-hive_2.11-2.4.0-cdh6.1.1.jar拷贝到spark em lib目录下面重启spark em，此问题得到解决。



至此问题得到解决！
欢迎大家补充，一起为开源贡献自己的力量。