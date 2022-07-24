Linkis
==========

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![codecov](https://codecov.io/gh/apache/incubator-linkis/branch/master/graph/badge.svg)](https://codecov.io/gh/apache/incubator-linkis/branch/master)
[![Page Views Count](https://badges.toozhao.com/badges/01G7TRNN1PH9PMSCYWDF3EK4QT/green.svg)](https://badges.toozhao.com/stats/01G7TRNN1PH9PMSCYWDF3EK4QT "Get your own page views count badge on badges.toozhao.com")

[English](README.md) | [中文](README_CN.md)

# Introduction

 Linkis builds a layer of computation middleware between upper applications and underlying engines. By using standard interfaces such as REST/WS/JDBC provided by Linkis, the upper applications can easily access the underlying engines such as MySQL/Spark/Hive/Presto/Flink, etc., and achieve the intercommunication of user resources like unified variables, scripts, UDFs, functions and resource files at the same time.

As a computation middleware, Linkis provides powerful connectivity, reuse, orchestration, expansion, and governance capabilities. By decoupling the application layer and the engine layer, it simplifies the complex network call relationship, and thus reduces the overall complexity and saves the development and maintenance costs as well.

Since the first release of Linkis in 2019, it has accumulated more than **700** trial companies and **1000+** sandbox trial users, which involving diverse industries, from finance, banking, tele-communication, to manufactory, internet companies and so on. Lots of companies have already used Linkis as a unified entrance for the underlying computation and storage engines of the big data platform.


![linkis-intro-01](https://user-images.githubusercontent.com/7869972/148767375-aeb11b93-16ca-46d7-a30e-92fbefe2bd5e.png)

![linkis-intro-03](https://user-images.githubusercontent.com/7869972/148767380-c34f44b2-9320-4633-9ec8-662701f41d15.png)

# Features

- **Support for diverse underlying computation storage engines**.  
    Currently supported computation/storage engines: Spark, Hive, Flink, Python, Pipeline, Sqoop, openLooKeng, JDBC, Shell, etc.      
    Computation/storage engines to be supported: Presto (planned 1.2.0), ElasticSearch (planned 1.2.0), etc.
    Supported scripting languages: SparkSQL, HiveQL, Python, Shell, Pyspark, R, Scala and JDBC, etc. 

- **Powerful task/request governance capabilities**. With services such as Orchestrator, Label Manager and customized Spring Cloud Gateway, Linkis is able to provide multi-level labels based, cross-cluster/cross-IDC fine-grained routing, load balance, multi-tenancy, traffic control, resource control, and orchestration strategies like dual-active, active-standby, etc.  

- **Support full stack computation/storage engine**. As a computation middleware, it will receive, execute and manage tasks and requests for various computation storage engines, including batch tasks, interactive query tasks, real-time streaming tasks and storage tasks;

- **Resource management capabilities**.  ResourceManager is not only capable of managing resources for Yarn and Linkis EngineManger, but also able to provide label-based multi-level resource allocation and recycling, allowing itself to have powerful resource management capabilities across mutiple Yarn clusters and mutiple computation resource types;

- **Unified Context Service**. Generate Context ID for each task/request,  associate and manage user and system resource files (JAR, ZIP, Properties, etc.), result set, parameter variable, function, etc., across user, system, and computing engine. Set in one place, automatic reference everywhere;

- **Unified materials**. System and user-level unified material management, which can be shared and transferred across users and systems.

# Supported Engine Types

| **Engine Name** | **Suppor Component Version<br/>(Default Dependent Version)** | **Linkis Version Requirements** | **Included in Release Package<br/> By Default** | **Description** |
|:---- |:---- |:---- |:---- |:---- |
|Spark|Apache 2.0.0~2.4.7, <br/>CDH >= 5.4.0, <br/>(default Apache Spark 2.4.3)|\>=1.0.3|Yes|Spark EngineConn, supports SQL , Scala, Pyspark and R code|
|Hive|Apache >= 1.0.0, <br/>CDH >= 5.4.0, <br/>(default Apache Hive 2.3.3)|\>=1.0.3|Yes |Hive EngineConn, supports HiveQL code|
|Python|Python >= 2.6, <br/>(default Python2*)|\>=1.0.3|Yes |Python EngineConn, supports python code|
|Shell|Bash >= 2.0|\>=1.0.3|Yes|Shell EngineConn, supports Bash shell code|
|JDBC|MySQL >= 5.0, Hive >=1.2.1, <br/>(default Hive-jdbc 2.3.4)|\>=1.0.3|No|JDBC EngineConn, already supports MySQL and HiveQL, can be extended quickly Support other engines with JDBC Driver package, such as Oracle|
|Flink |Flink >= 1.12.2, <br/>(default Apache Flink 1.12.2)|\>=1.0.3|No |Flink EngineConn, supports FlinkSQL code, also supports starting a new Yarn in the form of Flink Jar Application |
|Pipeline|-|\>=1.0.3|No|Pipeline EngineConn, supports file import and export|
|openLooKeng|openLooKeng >= 1.5.0, <br/>(default openLookEng 1.5.0)|\>=1.1.1|No|openLooKeng EngineConn, supports querying data virtualization engine with Sql openLooKeng|
|Sqoop| Sqoop >= 1.4.6, <br/>(default Apache Sqoop 1.4.6)|\>=1.1.2|No|Sqoop EngineConn, support data migration tool Sqoop engine|
|Impala|Impala >= 3.2.0, CDH >=6.3.0|ongoing|-|Impala EngineConn, supports Impala SQL code|
|Presto|Presto >= 0.180|ongoing|-|Presto EngineConn, supports Presto SQL code|
|ElasticSearch|ElasticSearch >=6.0|ongoing|-|ElasticSearch EngineConn, supports SQL and DSL code|
|MLSQL| MLSQL >=1.1.0|ongoing|-|MLSQL EngineConn, supports MLSQL code.|
|Hadoop|Apache >=2.6.0, <br/>CDH >=5.4.0|ongoing|-|Hadoop EngineConn, supports Hadoop MR/YARN application|
|TiSpark|1.1|ongoing|-|TiSpark EngineConn, supports querying TiDB with SparkSQL|


# Ecosystem

| Component | Description | Linkis 1.x(recommend 1.1.1) Compatible |
| --------------- | -------------------------------------------------------------------- | --------- |
| [**DataSphereStudio**](https://github.com/WeBankFinTech/DataSphereStudio/blob/master/README.md) | DataSphere Studio (DSS for short) is WeDataSphere, a one-stop data application development management portal.  | **DSS 1.0.1[released][Linkis recommend 1.1.1]** |
| [**Scriptis**](https://github.com/WeBankFinTech/Scriptis) | Support online script writing such as SQL, Pyspark, HiveQL, etc., submit to [Linkis](https://github.com/apache/incubator-linkis) to perform data analysis web tools.  | **In DSS 1.0.1[released]** |
| [**Schedulis**](https://github.com/WeBankFinTech/Schedulis) | Workflow task scheduling system based on Azkaban secondary development, with financial-grade features such as high performance, high availability and multi-tenant resource isolation. | **Schedulis0.6.2 [released]** |
| [**Qualitis**](https://github.com/WeBankFinTech/Qualitis) | Data quality verification tool, providing data verification capabilities such as data integrity and correctness  |**Qualitis 0.9.1 [released]** |
| [**Streamis**](https://github.com/WeBankFinTech/Streamis) | Streaming application development management tool. It supports the release of Flink Jar and Flink SQL, and provides the development, debugging and production management capabilities of streaming applications, such as: start-stop, status monitoring, checkpoint, etc.| **Streamis 0.1.0 [released][Linkis recommend 1.1.0]** |
| [**Exchangis**](https://github.com/WeBankFinTech/Exchangis) | A data exchange platform that supports data transmission between structured and unstructured heterogeneous data sources, the upcoming Exchangis1. 0, will be connected with DSS workflow | **Exchangis 1.0.0 [developing]**|
| [**Visualis**](https://github.com/WeBankFinTech/Visualis) | A data visualization BI tool based on the second development of Davinci, an open source project of CreditEase, provides users with financial-level data visualization capabilities in terms of data security. |  **Visualis 1.0.0[developing]**|
| [**Prophecis**](https://github.com/WeBankFinTech/Prophecis) | A one-stop machine learning platform that integrates multiple open source machine learning frameworks. Prophecis' MLFlow can be connected to DSS workflow through AppConn. | **Prophecis 0.3.0 [released]** |

# Download

Please go to the [Linkis Releases Page](https://linkis.apache.org/download/main) to download a compiled distribution or a source code package of Linkis.

# Compile and Deploy

> For more detailed guidance see:
>[[Backend Compile]](https://linkis.apache.org/zh-CN/docs/latest/development/linkis_compile_and_package)
>[[Management Console Build]](https://linkis.apache.org/zh-CN/docs/latest/development/web_build)

```shell

## compile backend
### Mac OS/Linux
./mvnw -N install
./mvnw  clean install -Dmaven.javadoc.skip=true -Dmaven.test.skip=true


### Windows
mvnw.cmd -N install
mvnw.cmd clean install -Dmaven.javadoc.skip=true -Dmaven.test.skip=true

## compile web
cd incubator-linkis/web
npm install
npm run build
```
 
Please refer to [Quick Deployment](https://linkis.apache.org/docs/latest/deployment/quick_deploy) to do the deployment.

# Examples and Guidance
- [User Manual](https://linkis.apache.org/docs/latest/user_guide/overview)
- [Engine Usage Documents](https://linkis.apache.org/docs/latest/engine_usage/overview) 
- [API Documents](https://linkis.apache.org/docs/latest/api/overview)

# Documentation & Vedio

- The documentation of linkis is in [Linkis-Website Git Repository](https://github.com/apache/incubator-linkis-website).
- Meetup videos on [Bilibili](https://space.bilibili.com/598542776?from=search&seid=14344213924133040656).

# Architecture
Linkis services could be divided into three categories: computation governance services, public enhancement services and microservice governance services.
- The computation governance services, support the 3 major stages of processing a task/request: submission -> preparation -> execution;
- The public enhancement services, including the material library service, context service, and data source service;
- The microservice governance services, including Spring Cloud Gateway, Eureka and Open Feign.

Below is the Linkis architecture diagram. You can find more detailed architecture docs in [Linkis-Doc/Architecture](https://linkis.apache.org/docs/latest/architecture/overview).
![architecture](https://user-images.githubusercontent.com/7869972/148767383-f87e84ba-5baa-4125-8b6e-d0aa4f7d3a66.png)

Based on Linkis the computation middleware, we've built a lot of applications and tools on top of it in the big data platform suite [WeDataSphere](https://github.com/WeBankFinTech/WeDataSphere). Below are the currently available open-source projects. More projects upcoming, please stay tuned.

![wedatasphere_stack_Linkis](https://user-images.githubusercontent.com/7869972/148767389-049361df-3609-4c2f-a4e2-c904c273300e.png)

# Contributing

Contributions are always welcomed, we need more contributors to build Linkis together. either code, or doc, or other supports that could help the community.  
For code and documentation contributions, please follow the [contribution guide](https://linkis.apache.org/community/how-to-contribute).

# Contact Us


- Any questions or suggestions please kindly submit an [issue](https://github.com/apache/incubator-linkis/issues).  
- By mail [dev@linkis.apache.org](mailto:dev@linkis.apache.org)
- You can scan the QR code below to join our WeChat group to get more immediate response.

![wechatgroup](https://user-images.githubusercontent.com/7869972/176336986-d6b9be8f-d1d3-45f1-aa45-8e6adf5dd244.png)



# Who is Using Linkis

We opened an issue [[Who is Using Linkis]](https://github.com/apache/incubator-linkis/issues/23) for users to feedback and record who is using Linkis.  
Since the first release of Linkis in 2019, it has accumulated more than **700** trial companies and **1000+** sandbox trial users, which involving diverse industries, from finance, banking, tele-communication, to manufactory, internet companies and so on.
