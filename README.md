Linkis
============

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

English | [中文](docs/zh_CN/README.md)

## Introduction

Linkis builds a layer of computation middleware between the upper-level application and the underlying engine. The upper-level applications can connect to the bottom layer computation and storage engine by the standard interfaces such as REST/WS/JDBC provided by Linkis to connect to MySQL/Spark/Hive/Presto/Flink, etc, and achieves the intercommunication of user resources such as unified variables, scripts, UDFs, functions and resource files.

As a computation middleware, it provides powerful connectivity, reuse, expansion, and management capabilities. And by decoupling the application layer and the engine layer, the complex network call relationship is simplified, and the overall complexity and development, operation and maintenance costs are reduced.

Since Linkis was open sourced in 2019, it has accumulated nearly 700 trial companies and 1000+ sandbox trial users, which involved in multiple industries such as the Internet, finance, and communications. Many companies have used them as a unified entrance for the underlying computation storage engine components of the big data platform.


![linkis-intro-01](https://user-images.githubusercontent.com/11496700/84615498-c3030200-aefb-11ea-9b16-7e4058bf6026.png)

![linkis-intro-03](https://user-images.githubusercontent.com/11496700/84615483-bb435d80-aefb-11ea-81b5-67f62b156628.png)
<br>
<br>

Based on the concept of the computation middleware architecture of Linkis, we have built a large amount of applications and systems on top of it. Currently available open-source project: 
 
 - [**DataSphere Studio - Data Application Development& Management Portal**](https://github.com/WeBankFinTech/DataSphereStudio)
 
 - [**Qualitis - Data Quality Tool**](https://github.com/WeBankFinTech/Qualitis)
 
 - [**Scriptis - Data Development IDE Tool**](https://github.com/WeBankFinTech/Scriptis)
 
 - [**Visualis - Data Visualization Tool**](https://github.com/WeBankFinTech/Visualis)

 - [**Schedulis - Workflow Task Scheduling Tool**](https://github.com/WeBankFinTech/Schedulis)

 There will be more tools released as open-source projects, please stay tuned!

## Features

- **Support for abundant underlying computation storage engine**.

    **Currently supported computation storage engines**: Spark, Hive, Python, Presto, ElasticSearch, MLSQL, TiSpark, JDBC, Shell, etc.
    
    **Supporting computation storage engine**: Flink, Impala, Clickhouse, etc.
    
    **Supported script languages**: SparkSQL, Scala, Pyspark, R, Python, JDBC, HiveQL and Shell, etc.;
  
- **Powerful computation governance capabilities**: Linkis 1.0 is divided into three categories in total: public enhancement service, computation governance service, and microservice governance service. The three categories are as follows:
                                                    
    1. Public enhancement service are the material library service, context service, data source service and public service provided by Linkis 0.X;
                                                    
    2. The microservice governance service are Spring Cloud Gateway, Eureka and Open Feign already provided by Linkis 0.X, and Linkis 1.0 will also provide support for Nacos;
                                                    
    3. Computation governance service are the core focus of Linkis 1.0. which divided three stages named submission -> preparation -> execution, to comprehensively upgrade Linkis's ability to execute, manage and control the user tasks.


- **Support for full stack computation storage engine architecture**. As a computation middleware, it will receive, execute and manage tasks and requests from users for various computation storage engines, including batch tasks, interactive query tasks, real-time streaming tasks and storage tasks, and provide powerful computation governance capabilities such as reuse, warm-up , current-limiting, engine-switching, full life cycle management of engines;

- **More powerful resource management capabilities**. ResourceManager not only extends the resource management capabilities of Linkis0.X for Yarn and Linkis EngineManager, but also provides tag-based multi-level resource allocation and recycling capabilities, allowing ResourceManager have powerful resource management capabilities across mutil Yarn clusters and mutil computation resource types.

- **labeling in full-process**.Based on multi-level combined tags, Linkis1.0 provides cross-IDC and cross-cluster computation task routing management and control capabilities, and multi-tenant isolation capabilities for EngineConnManager and EngineConn;

- **Unified Context Service**.Unified user and system resource files (JAR, ZIP, Properties, etc.), unified management of parameters and variables across users, systems, and calculation engines, one setting and automatic reference everywhere;

- **Unified materials**. System and user-level material management, which can be shared and transferred across users and systems, and support automatic management of the entire life cycle.


# Documentations：

### Linkis1.0 documentations

[**Linkis1.0 Quick Deploy**](https://github.com/WeBankFinTech/Linkis/wiki/%E5%A6%82%E4%BD%95%E5%BF%AB%E9%80%9F%E5%AE%89%E8%A3%85%E4%BD%BF%E7%94%A8Linkis-1.0-RC1)

[**Linkis1.0 User Documentation**](https://github.com/WeBankFinTech/Linkis/wiki/Linkis1.0%E7%94%A8%E6%88%B7%E4%BD%BF%E7%94%A8%E6%96%87%E6%A1%A3)

[**Quickly understand the new architecture of Linkis1.0**](https://github.com/WeBankFinTech/Linkis/wiki/Linkis1.0%E4%B8%8ELinkis0.X%E7%9A%84%E5%8C%BA%E5%88%AB%E7%AE%80%E8%BF%B0)

### Linkis0.X documentations

[Linkis, make big data easier](docs/en_US/ch3/Linkis_Introduction.md)

[Linkis Quick Deploy](docs/en_US/ch1/deploy.md)

[Linkis Quick Start & Java SDK documentation](docs/en_US/ch3/Linkis_Java_SDK_doc.md)

[HTTP APIs for frontend applications](docs/en_US/ch3/Linkis_HTTP_API_Doc.md)

[WebSocket APIs for frontend applications](docs/en_US/ch3/Linkis_WebSocket_API_Doc.md)

[How to adapt Linkis with a new computation or storage engine](docs/en_US/ch3/How_to_adapt_Linkis_with_a_new_computation_or_storage_engine.md)

----

# Architecture：

![architecture]()

----

# RoadMap

### 1. Implement Orchestrator architecture and support for rich computation and orchestration strategies:

- Support multi-active
- Support active and standby
- Support transaction
- Support playback
- Support mixed computation of multiple data sources
- Support heterogeneous computation of multiple data sources

### 2. Linkis1.0 management console optimization

- Unified data source module optimization
- UDF module optimization
- JobHistory is planned to support the display of detailed Metrics information of all orchestration tasks of one job in Orchestrator.

### 3. Linkis1.0 supports Flink engine and completes the new architecture adaptation of all unadapted engines

- Optimize Presto engine for new architecture
- Optimize ElasticSearch engine for new architecture
- Optimize Impala engine for new architecture

If you have any needs, please submit an issue, and we will reply to you in time.

# Contributing

We welcome all community partners to contribute new computation storage engines and other codes to us!

## Communication

If you desire immediate response, please kindly raise issues to us or scan the below QR code by WeChat and QQ to join our group:
<br>
![introduction05](https://user-images.githubusercontent.com/11496700/84615565-f2197380-aefb-11ea-8288-c2d7b0410933.png)

## License

Linkis is under the Apache 2.0 license. See the [LICENSE ](http://www.apache.org/licenses/LICENSE-2.0)file for details.