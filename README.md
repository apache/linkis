## 1 Introduction 

WilLink is a collection of middleware services for financial-level big data platforms. Connected to WilLink foreground system, a user can obtain the capabilities of financial-level multi-tenant, resource management and permission isolation. Besides, unified variables, functions and source file management across multiple foreground systems are realized. In addition, it empowers users to get lifecycle management capabilities for high-concurrency, high-performance and high-availability jobs.

Based on the microservices architecture, WilLink also supports plug-in connection to various back-end big data computing and storage engines, allowing users’ data development/display tool system instantly upgrading to financial level. 

WilLink, the link between user and data platform. 

## 2 Features

- Unified Job Execution Services:  A distributed REST/WebSocket container designed for receiving data analysis and exploration requests submitted by foreground system.

  Currently supported compute engines: Spark, Python, TiSpark, Hive and Shell, etc.

  Currently supported script languages: SparkSQL, Spark Scala, PySpark, R, Python, HQL and Shell, etc.

- Resource Management Services: Provide real-time control of each user's resources usage and dynamic resources charts and thus make it easy for users to view and manage their own resources. 

  Currently supported resources type: Yarn queue resources, server(CPU and memory), number of concurrent users, etc.

- Application Management Services: Manage all user applications across all systems, including offline batch applications, interactive query applications as well as live streaming applications, providing powerful reusability for offline and interactive application and providing application lifecycle management that automatically frees up user idle applications.

- Unified Storage Services: The generic IO architecture can quickly connected with various storage systems and provide a uniform call entry. Besides, it is highly integrated, easy to use and supports all common data formats.

- Unified Context Services: Unify user and system resources files (JAR, ZIP, Properties). User, system, parameters and variables of compute engine are managed in a unified manner. Therefore, once there is a change, the rest that associated with it will change accordingly.

- Material Library: System and user-level material management, it is capable of sharing, transferring materials and supports automatic lifecycle management. 

- Metadata Services: Real-time display of dataset table structure and partition. 

## 3 QuickStart

Read the Quick Start.

## 4 Architecture

## 5 RoadMap

## 6 Contributing

## 7 Adopters

## 8 Communication

## 9 License

Scriptest is under the Apache 2.0 license. See the [LICENSE ](http://www.apache.org/licenses/LICENSE-2.0)file for details.

