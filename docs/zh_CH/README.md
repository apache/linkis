## 引言：

WilLink是一个金融级大数据平台中间件服务集合。对接了WilLink的前台系统，可以快速获得金融级多租户、资源管控、权限隔离能力，跨多个前台系统的统一变量、函数、资源文件管理能力，及高并发、高性能、高可用的大数据作业全生命周期管理能力。

基于微服务架构，支持插拔式的对接各种后台大数据计算存储引擎，让你的数据开发/展示工具系统瞬间升级金融级。

WilLink，做数据平台的中台。

---

## 特点：

**统一作业执行服务**：一个分布式的REST/WebSocket容器，用于接收前台系统提交的数据分析探索作业请求。

**目前支持的计算引擎有**：Spark、Python、TiSpark、Hive和Shell等。

**支持的脚本语言有**：SparkSQL、Spark Scala、Pyspark、R、Python、HQL和Shell等；

**资源管理服务**： 实时管控每个用户的资源使用情况，并提供实时、动态的资源图表，方便查看资源使用情况和管理用户自己的资源；

**目前已支持的资源类型**：Yarn队列资源、服务器（CPU和内存）、用户并发个数等。

** 应用管理服务**：管理所有系统的所有用户应用，包括离线批量应用、交互式查询应用和实时流式应用，为离线和交互式应用提供强大的复用能力，并提供应用全生命周期管理，自动释放用户多余的空闲应用；

**统一存储服务**：通用的IO架构，能快速对接各种存储系统，提供统一调用入口，支持所有常用格式数据，集成度高，简单易用；

**统一上下文服务**：统一用户和系统资源文件（JAR、ZIP、Properties等），用户、系统、计算引擎的参数和变量统一管理，一处设置，处处自动引用；

**物料库**：系统和用户级物料管理，可分享和流转，支持全生命周期自动管理；

**元数据服务**：实时的库表结构和分区情况展示。

---

## QuickStart：

Read the Quick Start.

---

## Architecture：

![introduction01](/images/introduction/introduction01.png)

**第五章：RoadMap**

**第六章：Contributing\(暂时没有\)**

**第七章：Adopters：\(暂缺\)**

**第八章：Communication**

**第九章：License：Scriptest is under the Apache 2.0 license. See the LICENSE file for details.**

