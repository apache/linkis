Linkis
============

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![codecov](https://codecov.io/gh/apache/incubator-linkis/branch/master/graph/badge.svg)](https://codecov.io/gh/apache/incubator-linkis/branch/master)
[![Page Views Count](https://badges.toozhao.com/badges/01G7TRNN1PH9PMSCYWDF3EK4QT/green.svg)](https://badges.toozhao.com/stats/01G7TRNN1PH9PMSCYWDF3EK4QT "Get your own page views count badge on badges.toozhao.com")

[English](README.md) | [中文](README_CN.md)

# 介绍

Linkis 在上层应用程序和底层引擎之间构建了一层计算中间件。通过使用Linkis 提供的REST/WebSocket/JDBC 等标准接口，上层应用可以方便地连接访问MySQL/Spark/Hive/Presto/Flink 等底层引擎，同时实现变量、脚本、函数和资源文件等用户资源的跨上层应用互通。  
作为计算中间件，Linkis 提供了强大的连通、复用、编排、扩展和治理管控能力。通过计算中间件将应用层和引擎层解耦，简化了复杂的网络调用关系，降低了整体复杂度，同时节约了整体开发和维护成本。  
Linkis 自2019年开源发布以来，已累计积累了700多家试验企业和1000+沙盒试验用户，涉及金融、电信、制造、互联网等多个行业。许多公司已经将Linkis 作为大数据平台底层计算存储引擎的统一入口，和计算请求/任务的治理管控利器。

![没有Linkis 之前](https://user-images.githubusercontent.com/7869972/148767370-06025750-090e-4fd6-bd32-aab2fbb01352.png)


![有了Linkis 之后](https://user-images.githubusercontent.com/7869972/148767358-b02ae982-4080-4efa-aa0f-768ca27902b7.png)

# 核心特点

- **丰富的底层计算存储引擎支持**。  
    **目前支持的计算存储引擎**：Spark、Hive、Python、Presto、ElasticSearch、MLSQL、TiSpark、JDBC和Shell等。  
    **正在支持中的计算存储引擎**：Flink(>=1.0.2版本已支持)、Impala等。  
    **支持的脚本语言**：SparkSQL, HiveQL, Python, Shell, Pyspark, R, Scala 和JDBC 等。    
- **强大的计算治理能力**。基于Orchestrator、Label Manager和定制的Spring Cloud Gateway等服务，Linkis能够提供基于多级标签的跨集群/跨IDC 细粒度路由、负载均衡、多租户、流量控制、资源控制和编排策略(如双活、主备等)支持能力。  
- **全栈计算存储引擎架构支持**。能够接收、执行和管理针对各种计算存储引擎的任务和请求，包括离线批量任务、交互式查询任务、实时流式任务和存储型任务；
- **资源管理能力**。 ResourceManager 具备对 Yarn 和 Linkis EngineManager 的资源管理能力，还将提供基于标签的多级资源分配和回收能力，让 ResourceManager 具备跨集群、跨计算资源类型的强大资源管理能力。
- **统一上下文服务**。为每个计算任务生成context id，跨用户、系统、计算引擎的关联管理用户和系统资源文件（JAR、ZIP、Properties等），结果集，参数变量，函数等，一处设置，处处自动引用；
- **统一物料**。系统和用户级物料管理，可分享和流转，跨用户、系统共享物料。

# 支持的引擎类型

| **引擎名** | **支持底层组件版本<br/>(默认依赖版本)** | **Linkis 版本要求** | **是否默认包含在发布包中** | **说明** |
|:---- |:---- |:---- |:---- |:---- |
|Spark|Apache 2.0.0~2.4.7, <br/>CDH >= 5.4.0, <br/>（默认Apache Spark 2.4.3）|\>=1.0.3|是|Spark EngineConn， 支持SQL, Scala, Pyspark 和R 代码|
|Hive|Apache >= 1.0.0, <br/>CDH >= 5.4.0, <br/>（默认Apache Hive 2.3.3）|\>=1.0.3|是|Hive EngineConn， 支持HiveQL 代码|
|Python|Python >= 2.6, <br/>（默认Python2*）|\>=1.0.3|是|Python EngineConn， 支持python 代码|
|Shell|Bash >= 2.0|\>=1.0.3|是|Shell EngineConn， 支持Bash shell 代码|
|JDBC|MySQL >= 5.0, Hive >=1.2.1, <br/>(默认Hive-jdbc 2.3.4)|\>=1.0.3|否|JDBC EngineConn， 已支持MySQL 和HiveQL，可快速扩展支持其他有JDBC Driver 包的引擎, 如Oracle|
|Flink |Flink >= 1.12.2, <br/>(默认Apache Flink 1.12.2)|\>=1.0.3|否|Flink EngineConn， 支持FlinkSQL 代码，也支持以Flink Jar 形式启动一个新的Yarn 应用程序|
|Pipeline|-|\>=1.0.3|否|Pipeline EngineConn， 支持文件的导入和导出|
|openLooKeng|openLooKeng >= 1.5.0, <br/>(默认openLookEng 1.5.0)|\>=1.1.1|否|openLooKeng EngineConn， 支持用Sql查询数据虚拟化引擎openLooKeng|
|Sqoop| Sqoop >= 1.4.6, <br/>(默认Apache Sqoop 1.4.6)|\>=1.1.2|否|Sqoop EngineConn， 支持 数据迁移工具 Sqoop 引擎|
|Impala|Impala >= 3.2.0, CDH >=6.3.0|ongoing|-|Impala EngineConn，支持Impala SQL 代码|
|Presto|Presto >= 0.180|ongoing|-|Presto EngineConn， 支持Presto SQL 代码|
|ElasticSearch|ElasticSearch >=6.0|ongoing|-|ElasticSearch EngineConn， 支持SQL 和DSL 代码|
|MLSQL| MLSQL >=1.1.0|ongoing|-|MLSQL EngineConn， 支持MLSQL 代码.|
|Hadoop|Apache >=2.6.0, <br/>CDH >=5.4.0|ongoing|-|Hadoop EngineConn， 支持Hadoop MR/YARN application|
|TiSpark|1.1|ongoing|-|TiSpark EngineConn， 支持用SparkSQL 查询TiDB|

# 生态组件

| 应用工具     | 描述                                                          | Linkis 1.X(推荐1.1.1) 兼容版本    | 
| --------------- | -------------------------------------------------------------------- | ---------- | 
| [**DataSphere Studio**](https://github.com/WeBankFinTech/DataSphereStudio/blob/master/README-ZH.md)  | DataSphere Studio（简称 DSS）数据应用开发管理集成框架    | **DSS 1.0.1[已发布][Linkis 推荐1.1.1]** |
| [**Scriptis**](https://github.com/WeBankFinTech/Scriptis)   | 支持在线写 SQL、Pyspark、HiveQL 等脚本，提交给[Linkis](https://github.com/apache/incubator-linkis)执行的数据分析 Web 工具。 | 在DSS 1.0.1中[已发布] |
| [**Schedulis**](https://github.com/WeBankFinTech/Schedulis) | 基于 Azkaban 二次开发的工作流任务调度系统,具备高性能，高可用和多租户资源隔离等金融级特性。  | **Schedulis0.6.2 [已发布]** |
| [**Qualitis**](https://github.com/WeBankFinTech/Qualitis)   | 数据质量校验工具，提供数据完整性、正确性等数据校验能力  | **Qualitis 0.9.0 [已发布]** |
| [**Streamis**](https://github.com/WeBankFinTech/Streamis)  | 流式应用开发管理工具。支持发布 Flink Jar 和 Flink SQL ，提供流式应用的开发调试和生产管理能力，如：启停、状态监控、checkpoint 等。 | **Streamis 0.1.0 [已发布][Linkis 推荐1.1.0]** |
| [**Exchangis**](https://github.com/WeBankFinTech/Exchangis) | 支持对结构化及无结构化的异构数据源之间的数据传输的数据交换平台，即将发布的 Exchangis1.0，将与 DSS 工作流打通 | **Exchangis 1.0.0 [开发中]** |
| [**Visualis**](https://github.com/WeBankFinTech/Visualis)   | 基于宜信开源项目 Davinci 二次开发的数据可视化 BI 工具，为用户在数据安全方面提供金融级数据可视化能力。 | **Visualis 1.0.0[开发中]** |
| [**Prophecis**](https://github.com/WeBankFinTech/Prophecis)     | 一站式机器学习平台，集成多种开源机器学习框架。Prophecis 的 MLFlow 通过 AppConn 可以接入到 DSS 工作流中。     | **Prophecis 0.3.0 [已发布]** |

# 下载

请前往[Linkis Releases 页面](https://linkis.apache.org/download/main) 下载Linkis 的已编译版本或源码包。

# 编译和安装部署

> 更详细的步骤参见:
>[后端编译打包](https://linkis.apache.org/zh-CN/docs/latest/development/linkis_compile_and_package)
>[管理台编译](https://linkis.apache.org/zh-CN/docs/latest/development/web_build)

```shell script
## 后端编译

### Mac OS/Linux 系统
./mvnw -N install
./mvnw  clean install -Dmaven.javadoc.skip=true -Dmaven.test.skip=true

### Windows系统
mvnw.cmd -N install
mvnw.cmd clean install -Dmaven.javadoc.skip=true -Dmaven.test.skip=true

## 管理台编译
cd incubator-linkis/web
npm install
npm run build
```

请参考[快速安装部署](https://linkis.apache.org/zh-CN/docs/latest/deployment/quick_deploy) 来部署Linkis

# 示例和使用指引

- [用户手册](https://linkis.apache.org/zh-CN/docs/latest/user_guide/overview),
- [各引擎使用指引](https://linkis.apache.org/zh-CN/docs/latest/engine_usage/overview) 
- [API 文档](https://linkis.apache.org/zh-CN/docs/latest/api/overview)

# 文档&视频

- 完整的Linkis文档代码存放在[linkis-website仓库中](https://github.com/apache/incubator-linkis-website)  

- Meetup 视频 [Bilibili](https://space.bilibili.com/598542776?from=search&seid=14344213924133040656).


# 架构概要
Linkis 基于微服务架构开发，其服务可以分为3类:计算治理服务、公共增强服务和微服务治理服务。  
- 计算治理服务，支持计算任务/请求处理流程的3个主要阶段:提交->准备->执行;
- 公共增强服务，包括上下文服务、物料管理服务及数据源服务等;
- 微服务治理服务，包括定制化的Spring Cloud Gateway、Eureka、Open Feign。

下面是Linkis 的架构概要图. 更多详细架构文档请见 [Linkis-Doc/Architecture](https://linkis.apache.org/zh-CN/docs/latest/architecture/overview).
![architecture](https://user-images.githubusercontent.com/7869972/148767383-f87e84ba-5baa-4125-8b6e-d0aa4f7d3a66.png)

基于Linkis 计算中间件，我们在大数据平台套件[WeDataSphere](https://github.com/WeBankFinTech/WeDataSphere) 中构建了许多应用和工具系统。下面是目前可用的开源项目。

![wedatasphere_stack_Linkis](https://user-images.githubusercontent.com/7869972/148767389-049361df-3609-4c2f-a4e2-c904c273300e.png)

- [**DataSphere Studio** - 数据应用集成开发框架](https://github.com/WeBankFinTech/DataSphereStudio)

- [**Scriptis** - 数据研发IDE工具](https://github.com/WeBankFinTech/Scriptis)

- [**Visualis** - 数据可视化工具](https://github.com/WeBankFinTech/Visualis)

- [**Schedulis** - 工作流调度工具](https://github.com/WeBankFinTech/Schedulis)

- [**Qualitis** - 数据质量工具](https://github.com/WeBankFinTech/Qualitis)

- [**MLLabis** - 容器化机器学习notebook 开发环境](https://github.com/WeBankFinTech/prophecis)

更多项目开源准备中，敬请期待。

# 贡献

我们非常欢迎和期待更多的贡献者参与共建Linkis, 不论是代码、文档，或是其他能够帮助到社区的贡献形式。  
代码和文档相关的贡献请参照[贡献指引](https://linkis.apache.org/zh-CN/community/how-to-contribute).

# 联系我们

- 对Linkis 的任何问题和建议，可以提交issue，以便跟踪处理和经验沉淀共享
- 通过邮件方式 [dev@linkis.apache.org](mailto:dev@linkis.apache.org) 
- 可以扫描下面的二维码，加入我们的微信群，以获得更快速的响应

![wechatgroup](https://user-images.githubusercontent.com/7869972/176336986-d6b9be8f-d1d3-45f1-aa45-8e6adf5dd244.png)



# 谁在使用Linkis

我们创建了一个 issue [[Who is Using Linkis]](https://github.com/apache/incubator-linkis/issues/23) 以便用户反馈和记录谁在使用Linkis.  
Linkis 自2019年开源发布以来，累计已有700多家试验企业和1000+沙盒试验用户，涉及金融、电信、制造、互联网等多个行业。
