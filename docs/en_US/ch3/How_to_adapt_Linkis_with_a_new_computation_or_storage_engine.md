# How to adapt Linkis with a new computation or storage engine

## 1. Introduction

Except using the engines developed by Linkis directly, backend developers can also develop their own applications based on their requirements. 
Divided into Entrance, EngineManager and Engine modules, one can easily split an application to adapt to Linkis. 
The purpose and archetecture of these three modules please refer to Linkis Archetect Design Docs[UJES架构设计文档](../ch4/Linkis-UJES设计文档.md).


## 2. Declaration

Linkis uses Spring framework as the underlying technique. So the Spring development specs must be obeyed.

Linkis has an elastic underlying achitecture and provides common implementations for almost all of its top-level interfaces. If customized classes are needed by users, they can be directly injected and replace the current implementations.


### 2.1 Entrance module adaption

**1) maven dependency**

```xml
<dependency>
  <groupId>com.webank.wedatasphere.Linkis</groupId>
  <artifactId>Linkis-ujes-entrance</artifactId>
  <version>0.9.1</version>
</dependency>
```

**2)Interfaces to be implemented**

There is no compulsary interface in Entrance. Below interfaces can be implemented on demand.
- EntranceParser. Used to parse request maps from frontend to a persistable Task. Class AbstractEntranceParser is already provided and only parseToTask method needs to be overrided. Linkis provides CommonEntranceParser as the default implementation.
- EngineRequester. Used to build a RequestEngine object, which can be used to request a new engine from the EngineManager.
- Scheduler. Used to schedule tasks. The default implementation provides parallel mode for multi-user situations and FIFO mode for single user pattern. It is not suggested to be customized without special purposes.

### 2.2 EngineManager module adaption

**1) maven dependency**

```xml
<dependency>
  <groupId>com.webank.wedatasphere.Linkis</groupId>
  <artifactId>Linkis-ujes-enginemanager</artifactId>
  <version>0.9.1</version>
</dependency>
```

**2)Interfaces to be implemented**

Below interfaces are required to be implemented in EngineManager:
- EngineCreator. Method createProcessEngineBuilder needs to be overridden in the existing AbstractEngineCreator to create an EngineBuilder.
Here ProcessEngineBuilder has already provided a class called JavaProcessEngineBuilder, which is an abstract class accomplishes configurations of classpath, JavaOpts, GC file path and log path, and opening DEBUG port in test mode. To implement JavaProcessEngineBuilder, only extra classpath and JavaOpts are needed to be specified.
- EngineResourceFactory. Method getRequestResource needs to be overridden in the existing AbstractEngineResourceFactory to declare user customized resource requirements.
- resources. A Spring bean used to register resources to RM. Users need to specify an instance of ModuleInfo for dependency injection.

Below interfaces/beans are optional in EngineManager:
- hooks. A Spring bean used to add pre and post hooks around the Engine startup procedure. Users need to declare an Spring bean in type EngineHook[] hooks to make new hooks effective. For details please refer to com.webank.wedatasphere.linkis.enginemanager.impl.EngineManagerSpringConfiguration.


### 2.3 Engine module adaption

**1) maven dependency**

```xml
<dependency>
  <groupId>com.webank.wedatasphere.Linkis</groupId>
  <artifactId>Linkis-ujes-engine</artifactId>
  <version>0.9.1</version>
</dependency>
```


**2)Interfaces to be implemented**

Below interfaces are required to be implemented in Engine：
- EngineExecutorFactory. Used to build an EngineExecutor from a Map by implementing method createExecutor. This map contains evironment variables and engine arguments.
- EngineExecutor. The actual executor to execute the code submitted from the entrance. 
    Methods need to be implemented: 
      1. getActualUsedResources(the resource an engine acually used)
      2. executeLine(execute a line of the code parsed by CodeParser)
      3. executeCompletely(the suplementary method for executeLine. If executeLine returns ExecuteIncomplete, new code will be submitted with the previous code together to the engine)

Below interfaces/beans are optional in Engine:
- engineHooks: Array[EngineHook], a Spring bean used to add pre and post hooks around the Engine startup procedure. Currently the system provides 2 hooks: CodeGeneratorEngineHook for UDF/Function loading and ReleaseEngineHook for releasing spare engines. The system registers engineHooks=Array(ReleaseEngineHook) only by default.
- CodeParser. Used to parse code into lines and submit one line only for each execution loop. The system registers a CodeParser returns all the code at once by default. 
- EngineParser. Used to convert a RequestTask to a Job that is acceptable by Scheduler. If not specified, the system registers an EngineParser that converts RequestTask to CommonEngineJob.
