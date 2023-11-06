<h2 align="center">
  Apache Linkis Web
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

Linkis Web provides a management system for easy management of user resources.

# Start

```shell
# configure the project basic information in the `src/.env` firstly
cd linkis/linkis-web-next
npm install
npm run dev

# build 
npm run build
```

# File Structure

> ├── README.md
> ├── README_CN.md
> ├── index.html
> ├── node_modules
> ├── package-lock.json
> ├── package.json
> ├── pom.xml
> ├── public
> │   ├── favicon.ico`
> │   ├── sidebar
> │   └── vite.svg
> ├── release-docs
> │   ├── LICENSE
> │   ├── NOTICE
> │   └── licenses
> ├── src
> │   ├── App.vue
> │   ├── assets
> │   ├── components
> │   ├── config
> │   ├── dss
> │   ├── env.d.ts
> │   ├── helper
> │   ├── index.d.ts
> │   ├── layout.vue
> │   ├── locales
> │   ├── main.ts
> │   ├── pages
> │   ├── router
> │   ├── scriptis
> │   ├── service
> │   ├── style
> │   ├── util
> │   └── vite-env.d.ts
> ├── tsconfig.json
> ├── tsconfig.node.json
> └── vite.config.ts

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
