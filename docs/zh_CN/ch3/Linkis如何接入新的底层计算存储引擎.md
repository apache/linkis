# Linkis如何接入新的底层计算存储引擎

## 1. 前言

开发人员在使用Linkis时，不但可以直接使用Linkis已经开发的执行引擎如：Spark、Hive、Python等，也可以根据自己的需要，开发实现自己的新计算存储引擎。

Linkis接入新计算存储引擎的方式非常简单，可以分成Entrance，EngineManager和Engine几个模块。

其中Entrance、EngineManager和Engine三个模块的作用和架构可以查看[UJES架构设计文档](../ch4/Linkis-UJES设计文档.md)。

## 2. 申明

Linkis底层使用了Spring框架，所以必须遵循Spring开发规范。

Linkis的底层架构非常灵活，所有的顶层接口几乎都提供了一些通用实现，如果用户想要使用自己编写的类，可以直接依赖注入覆盖掉通用实现。

### 2.1 Entrance模块接入

**1) maven依赖**

```xml
<dependency>
  <groupId>com.webank.wedatasphere.Linkis</groupId>
  <artifactId>Linkis-ujes-entrance</artifactId>
  <version>0.9.2</version>
</dependency>
```

**2) 必须要实现的接口**

新的Entrance没有必须要实例化的接口，以下接口可以根据需要进行定制化实现。

- EntranceParser。用于将前端传递过来的一个请求Map，转换成一个可被持久化的Task。该类已提供了AbstractEntranceParser，用户只需实现parseToTask方法即可，系统默认提供了CommonEntranceParser实现。
- EngineRequester。用于获得一个RequestEngine类，该类用于向EngineManager请求一个新的Engine。
- Scheduler。用于实现调度，默认已实现了多用户并发、单个用户内FIFO执行的调度模式，如无特殊需求，不建议实例化。

### 2.2 EngineManager模块接入

**1) maven依赖**

```xml
<dependency>
  <groupId>com.webank.wedatasphere.Linkis</groupId>
  <artifactId>Linkis-ujes-enginemanager</artifactId>
  <version>0.9.2</version>
</dependency>
```

**2) 需要实现的接口**

新的EngineManager必须实现以下接口:

- EngineCreator，已存在AbstractEngineCreator，需实现createProcessEngineBuilder方法，用于创建一个EngineBuilder。

  ProcessEngineBuilder已默认提供了一个JavaProcessEngineBuilder类，这个类是一个abstract类，已默认将必要的classpath、JavaOpts、GC文件路径、日志文件路径，以及测试模式下DEBUG端口的开启已做好了。
    
  如果创建的Engine是一个Java进程，建议继承JavaJavaProcessEngineBuilder，只需要加入新引擎额外的classpath和JavaOpts即可。
    
- EngineResourceFactory，已存在AbstractEngineResourceFactory，需实现getRequestResource方法，用于拿到用户的个性化资源请求。

- resources，这是一个spring实体bean，主要用于向RM注册资源，resources是ModuleInfo的实例，需要用户提供一个，以供依赖注入。

根据需要进行实现：

- hooks，这是一个spring实体bean，主要用于在创建并启动Engine的前后，加前置和后置hook。

  如果想使新的EngineHook生效，需要创建一个EngineHook[] hooks的spring bean，具体请参考com.webank.wedatasphere.linkis.enginemanager.impl.EngineManagerSpringConfiguration。

### 2.3 Engine模块接入

**1) maven依赖**

```xml
<dependency>
  <groupId>com.webank.wedatasphere.Linkis</groupId>
  <artifactId>Linkis-ujes-engine</artifactId>
  <version>0.9.2</version>
</dependency>
```

**2)需要实现的接口**


Engine必须实现的接口如下：

- EngineExecutorFactory。用于创建一个EngineExecutor，需实现createExecutor方法，具体为通过一个Map，Map会传入所有环境变量、用户引擎启动参数，用于创建一个EngineExecutor。
- EngineExecutor。实际真正的执行器，用于提交执行entrance提交过来的代码。

  需要实现以下方法：

    1. getActualUsedResources（该engine实际使用的资源）
    2. executeLine（执行一行通过CodeParser解析过的代码）
    3. executeCompletely（executeLine的补充方法，如果调用executeLine返回的是ExecuteIncomplete，这时会将新的Code和之前返回ExecuteIncomplete的代码同时传递给engine执行）


Engine非必须实现的接口或bean如下:

- engineHooks: Array[EngineHook]，是一个spring bean。EngineHook是engine创建的前置和后置hook，目前系统已提供了2个hook：CodeGeneratorEngineHook用于加载UDF和函数，ReleaseEngineHook用于释放空闲的engine，如果不指定，系统默认会提供engineHooks=Array(ReleaseEngineHook)
- CodeParser。用于解析代码，以便一行一行执行。如果不指定，系统默认提供一个直接返回所有代码的CodeParser。
- EngineParser，用于将一个RequestTask转换成可提交给Scheduler的Job，如果没有指定，系统会默认提供一个将RequestTask转换成CommonEngineJob的EngineParser。
