<h2 align="center">
  Apache Linkis Web Next
</h2>

<p align="center">
  <strong>Linkis 构建了一层计算中间件，方便上层应用与底层数据引擎之间的连接、治理和编排 </strong>
</p>
<p align="center">
  <a href="https://linkis.apache.org/">https://linkis.apache.org/</a>
</p>

<p align="center">
  <a href="https://linkis.apache.org/docs/latest/introduction/" >
    <img src="https://img.shields.io/badge/document-English-blue.svg" alt="EN docs" />
  </a>
  <a href="https://linkis.apache.org/zh-CN/docs/latest/introduction/">
    <img src="https://img.shields.io/badge/文档-简体中文-blue.svg" alt="简体中文文档" />
  </a>
</p>

<p align="center">
    <a target="_blank" href="https://search.maven.org/search?q=g:org.apache.linkis%20AND%20a:linkis">
        <img src="https://img.shields.io/maven-central/v/org.apache.linkis/linkis.svg?label=maven%20central" />
    </a>
    <a target="_blank" href="https://github.com/apache/linkis/blob/master/LICENSE">
        <img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg?label=license" />
    </a>
    <a target="_blank" href="https://www.oracle.com/technetwork/java/javase/downloads/index.html">
        <img src="https://img.shields.io/badge/JDK-8-green.svg" />
    </a>
    <a target="_blank" href="https://github.com/apache/linkis/actions">
        <img src="https://github.com/apache/linkis/actions/workflows//build-backend.yml/badge.svg" />
    </a>

   <a target="_blank" href='https://github.com/apache/linkis'>
        <img src="https://img.shields.io/github/forks/apache/linkis.svg" alt="github forks"/>
   </a>
   <a target="_blank" href='https://github.com/apache/linkis'>
        <img src="https://img.shields.io/github/stars/apache/linkis.svg" alt="github stars"/>
   </a>
   <a target="_blank" href='https://github.com/apache/linkis'>
        <img src="https://img.shields.io/github/contributors/apache/linkis.svg" alt="github contributors"/>
   </a>
  <a target="_blank" href="https://badges.toozhao.com/stats/01G7TRNN1PH9PMSCYWDF3EK4QT">
       <img src="https://badges.toozhao.com/badges/01G7TRNN1PH9PMSCYWDF3EK4QT/green.svg" />
  </a>
  
</p>
<br/>

---
[English](README.md) | [中文](README_CN.md)

# 介绍

Linkis 在上层应用程序和底层引擎之间构建了一层计算中间件。通过使用 Linkis 提供的 REST/WebSocket/JDBC 等标准接口，
上层应用可以方便地连接访问 MySQL/Spark/Hive/Presto/Flink 等底层引擎，同时实现变量、脚本、函数和资源文件等用户资源的跨上层应用互通。  

Linkis Web 提供了一个可视化的后台管理系统，便于对用户资源进行管理。然而，原有的 Linkis Web 项目使用了 Vue2 及相关技术进行开发，而 Vue2 已停止维护，缺乏社区生态支持，并且项目本身文件结构不清晰，模块划分不合理，所使用的组件库也缺乏维护，并且交互界面的用户体验也有待优化。

在此背景下，选择在原有的项目上继续进行迭代可能留下各种难以解决的问题。所以最终考虑在 Linkis Web Next 子项目下重构该项目，将核心技术栈从 Vue2 + Webpack 升级到 Vue3 + Vite，获得更多的社区生态支持并大大提高项目启动和构建速率，同时解决各种历史遗留问题。再使用内部长期维护的开源组件库 Fes-Design，就能使组件库支持的问题能够得到及时的反馈和修复，保障项目迭代的顺利进行。

当前已重构完成的模块如下所示：

| 已重构完成模块
| ---------------------------------------
| Login Page
| Nav Bar
| Side Bar
| Global History Management
| Parameter Config
| Global Variables
| Microservice Management
| Resource Management
| ECM Management
| Base Data Management (SubModule Error Management)
| Data Source Management (SubModule Data Source Access Permissions)
| UDF Function Management (SubModule UDF User Management)

后续仍需继续重构Base Data Management、Data Source Management、UDF Function Management中的子模块。

# 启动

```shell
# 首先要在 `src/.env` 中配置基本信息
cd linkis/linkis-web-next
npm install
npm run dev

# 打包构建
npm run build
```

# 贡献

我们非常欢迎和期待更多的贡献者参与共建 Linkis, 不论是代码、文档，或是其他能够帮助到社区的贡献形式。  
代码和文档相关的贡献请参照[贡献指引](https://linkis.apache.org/zh-CN/community/how-to-contribute).

# 联系我们

- 对 Linkis 的任何问题和建议，可以提交 issue，以便跟踪处理和经验沉淀共享
- 通过邮件方式 [dev@linkis.apache.org](mailto:dev@linkis.apache.org)
- 可以扫描下面的二维码，加入我们的微信群，以获得更快速的响应

<img src="https://linkis.apache.org/Images/wedatasphere_contact_01.png" width="256"/>

# 谁在使用 Linkis

我们创建了一个 issue [[Who is Using Linkis]](https://github.com/apache/linkis/issues/23) 以便用户反馈和记录谁在使用 Linkis.  
Linkis 自 2019 年开源发布以来，累计已有 700 多家试验企业和 1000+沙盒试验用户，涉及金融、电信、制造、互联网等多个行业。
