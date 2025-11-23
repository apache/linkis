<h2 align="center">
  Apache Linkis Web Next
</h2>

<p align="center">
  <strong> Linkis builds a computation middleware layer to facilitate connection, 
    governance and orchestration between the upper applications and the underlying data engines. </strong>
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

# Introduction

 Linkis builds a layer of computation middleware between upper applications and underlying engines. By using standard interfaces such as REST/WS/JDBC provided by Linkis, the upper applications can easily access the underlying engines such as MySQL/Spark/Hive/Presto/Flink, etc., and achieve the intercommunication of user resources like unified variables, scripts, UDFs, functions and resource files at the same time.

 Linkis Web provides a web management system to facilitate the management of user resources. However, the original Linkis Web project was developed using Vue2 and related technologies, but Vue2 has stopped maintenance and lacks community support. In addition, the file structure of the project itself is unclear, the module division is unreasonable, the component library used is also lacking maintenance, and the user experience of the interactive interface needs to be optimized.

 As mentioned above, still iterating on the original project may leave various difficult problems. Therefore, we finally considered refactoring the project under the Linkis Web Next project, upgrading the core technology stack from Vue2 + Webpack to Vue3 + Vite, obtaining more community support, greatly improving the project starting and building rate, and solving various historical problems. Besides, using the internally maintained open-source UI component library Fes-Design, the problems can be promptly fed back and repaired.

The modules that have been refactored are as follows:

| The modules that have been refactored
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

Sub-modules in Base Data Management, Data Source Management, and UDF Function Management will still need to be refactored in the future.

# Start

```shell
# configure the project basic information in the `src/.env` firstly
cd linkis/linkis-web-next
npm install
npm run dev

# build 
npm run build
```

# Contributing

Contributions are always welcomed, we need more contributors to build Linkis together. either code, or doc, or other supports that could help the community.  
For code and documentation contributions, please follow the [contribution guide](https://linkis.apache.org/community/how-to-contribute).

# Contact Us

- Any questions or suggestions please kindly submit an [issue](https://github.com/apache/linkis/issues).  
- By mail [dev@linkis.apache.org](mailto:dev@linkis.apache.org)
- You can scan the QR code below to join our WeChat group to get more immediate response

<img src="https://linkis.apache.org/Images/wedatasphere_contact_01.png" width="256"/>

# Who is Using Linkis

We opened an issue [[Who is Using Linkis]](https://github.com/apache/linkis/issues/23) for users to feedback and record who is using Linkis.  
Since the first release of Linkis in 2019, it has accumulated more than **700** trial companies and **1000+** sandbox trial users, which involving diverse industries, from finance, banking, tele-communication, to manufactory, internet companies and so on.
