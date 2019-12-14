## Linkis使用文档


#### 1.概述
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Linkis项目是微众银行大数据平台自研的一种大数据作业提交方案及可扩展框架。项目使用方式方便，用户可以结合微众银行另一项开源项目——意书(这里要用http链接)直接进行使用，当然用户也可以根据规定的前端接口进行接入。Linkis也提供了客户端的实现，用户可以通过使用Linkis的sdk直接访问服务端。另外，Linkis作为一个可扩展性很强的框架,用户可以通过SDK的方式开发自己的应用。


#### 2.前端接入
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Linkis项目前端接口提供了两种方式，HTTP和WebSocket。Websocket方式相比于HTTP方式具有对服务器友好，信息推送更加及时等优势，但是WebSocket在用户使用的时候可能出现断开连接的情况，所以开源项目意书在对接Linkis时候，采用了WebSocket和HTTP结合的方式，正常情况下使用websocket与Linkis进行通信，出现WebSocket断开连接的时候，就会切换为HTTP的方式与后台进行交互。
##### 2.1接口规范
Linkis在前后端进行交互的时候，自定义了一套自己的接口规范。<br>
**1).URL规范**
```
/api/rest_j/v1/{applicationName}/.+
/api/rest_s/v1/{applicationName}/.+
```

- rest_j表示接口符合Jersey规范
- rest_s表示接口符合springMVC Rest规范
- v1为服务的版本号，**版本号会随着Linkis版本进行升级**
- {applicationName}为微服务名

**2).请求规范**
```json
{
 	"method":"/api/rest_j/v1/entrance/execute",
 	"data":{},
	"websocketTag":"37fcbd8b762d465a0c870684a0261c6e"
}
```

**3).响应规范**
```json
{"method":"/api/rest_j/v1/entrance/execute","status":0, "message":"成功！","data":{}}
```
- method：返回请求的Restful API URL，主要是websocket模式需要使用。
- status：返回状态信息，其中：-1表示没有登录，0表示成功，1表示错误，2表示验证失败，3表示没该接口的访问权限。
- data：返回具体的数据。
- message：返回请求的提示信息。如果status非0时，message返回的是错误信息，其中data有可能存在stack字段，返回具体的堆栈信息。


##### 2.2WebSocket接口描述


**1).建立连接**<br>
<br>
此接口是为了和Linkis建立一个WebSocket连接。
- `/api/rest_j/entrance/connect`
- 请求方式 **GET**
- 响应状态码 **101**<br>

**2).请求执行任务**<br>
<br>
请求执行任务是将用户的作业提交到Linkis进行执行的接口
- 接口 `/api/rest_j/entrance/execute`
- 提交方式 `POST`<br>
- 请求JSON示例
```json
{
 	"method":"/api/rest_j/v1/entrance/execute",
 	"data":{
		"params": {
			"variable":{
				"k1":"v1"
			},
			"configuration":{
				"special":{
					"k2":"v2"
				},
				"runtime":{
					"k3":"v3"
				},
				"startup":{
					"k4":"v4"
				}
			}
		},
		"executeApplicationName":"spark",
		"executionCode":"show tables",
		"runType":"sql",
		"source":{
			"scriptPath": "/home/Linkis/Linkis.sql"
		},
    "websocketTag":"37fcbd8b762d465a0c870684a0261c6e"
	}
}
```
- 请求体data中的参数描述如下<br>

|  参数名 | 参数定义 |  类型 | 备注   |
| ------------ | ------------ | ------------ | ------------ |
| executeApplicationName  | 用户所期望使用的引擎服务，如Spark、hive等|  String | 不可为空  |
| requestApplicationName  | 发起请求的系统名 |  String | 可以为空  |
| params  | 用户指定的运行服务程序的参数  |  Map | 必填，里面的值可以为空  |
| executionCode  | 用户提交的执行代码  |  String |不可为空  |
| runType  | 当用户执行如spark服务时，可以选择python、R、SQL等runType|  String | 不可为空  |
| scriptPath  | 用户提交代码脚本的存放路径  |  String | 如果是IDE的话，与executionCode不能同时为空  |
                                        表1 请求体参数描述

- 返回示例
```json
{
 "method": "/api/rest_j/v1/entrance/execute",
 "status": 0,
 "message": "请求执行成功",
 "data": {
   "execID": "030418IDEhivebdpdwc010004:10087IDE_johnnwang_21",
   "taskID": "123"  
 }
}
```
- execID是用户任务提交到UJES之后，为该任务生成的唯一标识的执行ID，为String类型，这个ID只在任务运行时有用，类似PID的概念。ExecID的设计为(requestApplicationName长度)(executeAppName长度1)(Instance长度2)${requestApplicationName}${executeApplicationName}${entranceInstance信息ip+port}${requestApplicationName}_${umUser}_${index}
- taskID 是表示用户提交task的唯一ID，这个ID由数据库自增生成，为Long 类型

**3).任务状态、日志、进度主动推送**<br>

提交执行之后，任务的状态、日志、进度等信息都会由服务器主动推送，用websocket方式去主动进行请求。
请求的接口和下文中的HTTP是保持一致的，唯一不一样的是，websocket的请求shema是ws://,而HTTP的请求schema是http://。

WebSocket中的接口返回的示例如下
- 日志
```json
{
  "method": "/api/rest_j/v1/entrance/${execID}/log",
  "status": 0,
  "message": "返回日志信息",
  "data": {
    "execID": "${execID}",
	"log": ["error日志","warn日志","info日志", "all日志"],
  "taskID":28594,
	"fromLine": 56
  },
  "websocketTag":"37fcbd8b762d465a0c870684a0261c6e"
}
```
- 状态
```json
{
  "method": "/api/rest_j/v1/entrance/${execID}/status",
  "status": 0,
  "message": "返回状态信息",
  "data": {
    "execID": "${execID}",
    "taskID":28594,
	  "status": "Running",
  },
  "websocketTag":"37fcbd8b762d465a0c870684a0261c6e"
}
```
- 进度
```json
{
  "method": "/api/rest_j/v1/entrance/${execID}/log",
  "status": 0,
  "message": "返回进度信息信息",
  "data": {
    "execID": "${execID}",
    "taskID":28594,
    "progress": 0.2,
  	"progressInfo": [
  		{
  			"id": "job-1",
  			"succeedTasks": 2,
  			"failedTasks": 0,
  			"runningTasks": 5,
  			"totalTasks": 10
  		},
  		{
  			"id": "job-2",
  			"succeedTasks": 5,
  			"failedTasks": 0,
  			"runningTasks": 5,
  			"totalTasks": 10
  		}
  	]
  },
  "websocketTag":"37fcbd8b762d465a0c870684a0261c6e"
}
```



##### 2.3HTTP接口描述
HTTP接口需要在提交执行之后,需要采用HTTP轮询的方式请求获取作业的状态、日志、进度等信息



**1).请求执行**

- 接口 `/api/rest_j/entrance/execute`
- 提交方式 `POST`<br>
- 请求JSON示例，请求的参数描述与表1一致
```json
{
		"params": {},
		"executeApplicationName":"spark",
		"executionCode":"show tables",
		"runType":"sql",
		"source":{
			"scriptPath": "/home/Linkis/Linkis.sql"
		}
}
```
- 响应返回与websocket返回一致，都会获取一个execID和taskID

**2).获取状态**<br>
<br>
建立连接是WebSocket特有的一个接口,是为了和Linkis建立一个WebSocket连接
- 接口 `/api/rest_j/entrance/${execID}/status`
- 提交方式 `GET`<br>
- 返回示例
```json
{
 "method": "/api/rest_j/v1/entrance/{execID}/status",
 "status": 0,
 "message": "获取状态成功",
 "data": {
   "execID": "${execID}",
   "status": "Running"
 }
}
```

**3).获取日志**<br>
<br>
建立连接是WebSocket特有的一个接口,是为了和Linkis建立一个WebSocket连接
- 接口 `/api/rest_j/entrance/${execID}/log?fromLine=${fromLine}&size=${size}`
- 提交方式 `GET`
- 请求参数fromLine是指从第几行开始获取，size是指该次请求获取几行日志
- 返回示例，其中返回的fromLine需要下次日志请求的参数
```json
{
  "method": "/api/rest_j/v1/entrance/${execID}/log",
  "status": 0,
  "message": "返回日志信息",
  "data": {
    "execID": "${execID}",
	"log": ["error日志","warn日志","info日志", "all日志"],
	"fromLine": 56
  }
}
```

**4).获取进度**<br>
<br>
建立连接是WebSocket特有的一个接口,是为了和Linkis建立一个WebSocket连接
- 接口 `/api/rest_j/entrance/${execID}/progress`
- 提交方式 `GET`<br>
- 返回示例
```json
{
  "method": "/api/rest_j/v1/entrance/{execID}/progress",
  "status": 0,
  "message": "返回进度信息",
  "data": {
    "execID": "${execID}",
	"progress": 0.2,
	"progressInfo": [
		{
			"id": "job-1",
			"succeedTasks": 2,
			"failedTasks": 0,
			"runningTasks": 5,
			"totalTasks": 10
		},
		{
			"id": "job-2",
			"succeedTasks": 5,
			"failedTasks": 0,
			"runningTasks": 5,
			"totalTasks": 10
		}
	]
  }
}
```
**5).kill任务**<br>
<br>
建立连接是WebSocket特有的一个接口,是为了和Linkis建立一个WebSocket连接
- 接口 `/api/rest_j/entrance/${execID}/kill`
- 提交方式 `POST`
- 返回示例，其中返回的fromLine需要下次日志请求的参数
```json
{
 "method": "/api/rest_j/v1/entrance/{execID}/kill",
 "status": 0,
 "message": "OK",
 "data": {
   "execID":"${execID}"
  }
}
```



### 3.客户端SDK接入
客户端SDK接入请查看 
* [Linkis快速使用文档](/docs/zh_CH/ch2/linkis快速使用文档.md)



#### 4. 多引擎类型支持
后台开发人员在使用Linkis的时候，不但可以直接使用Linkis已经开发的执行引擎，也可以根据自己的需求使用框架开发出自己的应用。Linkis的接入方式简单,可以分成Entrance，EngineManager和Engine几个模块。其中Entrance、EngineManager和Engine三个模块的作用和架构可以查看UJES架构设计文档(**真实链接**)

#### 约定

Linkis项目使用了Spring框架作为底层技术，所以一些类实例可以直接通过Spring的注解进行注入。Linkis框架提供了一些通用实现，如果用户想要使用自己编写的类，可以使用直接使用并覆盖掉通用实现。

##### 4.1Entrance模块接入
**1)maven依赖**
```xml
<dependency>
  <groupId>com.webank.wedatasphere.Linkis</groupId>
  <artifactId>Linkis-ujes-entrance</artifactId>
  <version>0.9.2</version>
</dependency>
```
**2)需要实现的接口**

Entrance没有必须要实例化的接口，以下接口可以根据需要进行实现。
- EntranceParser。用于将前端传递过来的一个请求Map，转换成一个可被持久化的Task。该类已提供了AbstractEntranceParser，用户只需实现parseToTask方法即可，系统默认提供了CommonEntranceParser实现。
-	EngineRequester。用于获得一个RequestEngine类，该类用于向EngineManager请求一个新的Engine。
-	Scheduler。用于实现调度，默认已实现了多用户并发、单个用户内FIFO执行的调度模式，如无特殊需求，不建议实例化。

##### 4.2EngineManager模块接入
**1)maven依赖**
```xml
<dependency>
  <groupId>com.webank.wedatasphere.Linkis</groupId>
  <artifactId>Linkis-ujes-enginemanager</artifactId>
  <version>0.9.2</version>
</dependency>
```

**2)需要实现的接口**

EngineManager需要对以下接口根据需要进行实现:
- EngineCreator，已存在AbstractEngineCreator，需实现createProcessEngineBuilder方法，用于创建一个EngineBuilder。
在这里，ProcessEngineBuilder已默认提供了一个JavaProcessEngineBuilder类，这个类是一个abstract类，已默认将必要的classpath、JavaOpts、GC文件路径、日志文件路径，以及测试模式下DEBUG端口的开启已做好了。现JavaProcessEngineBuilder，只需要加入额外的classpath和JavaOpts即可。
- EngineResourceFactory，已存在AbstractEngineResourceFactory，需实现getRequestResource方法，用于拿到用户的个性化资源请求。
- hooks，这是一个spring实体bean，主要用于在创建并启动Engine的前后，加前置和后置hook，需要用户提供一个Array[EngineHook]，以供依赖注入。
- resources，这是一个spring实体bean，主要用于像RM注册资源，resources是ModuleInfo的实例，需要用户提供一个，以供依赖注入。


##### 4.3Engine模块接入
**1)maven依赖**
```xml
<dependency>
  <groupId>com.webank.wedatasphere.Linkis</groupId>
  <artifactId>Linkis-ujes-engine</artifactId>
  <version>0.9.2</version>
</dependency>
```
**2)需要实现的接口**

Engine必须实现的接口如下：
-	EngineExecutorFactory。用于创建一个EngineExecutor，需实现createExecutor方法，具体为通过一个Map，创建一个EngineExecutor。
- EngineExecutor。实际真正的执行器，用于提交执行entrance提交过来的代码。需要实现getActualUsedResources（该engine实际使用的资源）、executeLine（执行一行通过CodeParser解析过的代码）、executeCompletely（executeLine的补充方法，如果调用executeLine返回的是ExecuteIncomplete，这时会将新的Code和之前返回ExecuteIncomplete的代码同时传递给engine执行）

Engine非必须实现的接口或bean如下:
- engineHooks: Array[EngineHook]，是一个spring bean。EngineHook是engine创建的前置和后置hook，目前系统已提供了2个hook：CodeGeneratorEngineHook用于加载UDF和函数，ReleaseEngineHook用于释放空闲的engine，如果不指定，系统默认会提供engineHooks=Array(ReleaseEngineHook)
- CodeParser。用于解析代码，以便一行一行执行。如果不指定，系统默认提供一个直接返回所有代码的CodeParser。
- EngineParser，用于将一个RequestTask转换成可提交给Scheduler的Job，如果没有指定，系统会默认提供一个将RequestTask转换成CommonEngineJob的EngineParser。
