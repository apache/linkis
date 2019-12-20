## Linkis User Manual

#### 1.Summary
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Designed by Webank independently, Linkis is an extensible framework and a sophisticated solution for big data task submission. Conveniently, it could be used directly together with Scriptest, which is another open-source project powered by Webank. And those frontend APIs are also available for users. A client implementation is also provided as an SDK to interact directly with background services. As an highly extensible framework, users can leverage the SDK to develop their own applications.

#### 2.Frontend Adaption
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Two protocols are supported for frontend APIs: HTTP and WebSocket. Compared with HTTP, Websocket is more friendly to servers and behaved more efficiently in message pushing. But WebSocket is unstable and users are easily to be disconnected by accident. So Scriptest combined both ways to adapt with Linkis. It communicates with Linkis by websocket in normal circumstances, and failover to HTTP protocol in case the Websocket connection was down.
##### 2.1 API Specs
Linkis has its own specs for front-backend adaption.<br>

**1).URL specs**
```
/api/rest_j/v1/{applicationName}/.+
/api/rest_s/v1/{applicationName}/.+
```

- rest_j means the API is conformed to Jersey standards
- rest_s means the API is conformed to springMVC Rest standards
- v1 is the version of services，**The version will be upgraded with Linkis releases**
- {applicationName} is the microservice name 

**2).Request specs**
```json
{
 	"method":"/api/rest_j/v1/entrance/execute",
 	"data":{},
	"websocketTag":"37fcbd8b762d465a0c870684a0261c6e"
}
```

**3).Response specs**
```json
{"method":"/api/rest_j/v1/entrance/execute","status":0, "message":Success！","data":{}}
```
- method：Return the Restful API URL requested, basically used by websocket protocol。
- status：Return the status info, in which -1 means login failed, 0 means succeeded, 1 means error, 2 mean validation failed, and 3 means no permission.
- data：Return detailed data.
- message：Return hint message of the request. If the status is not 0, this message returns error messages. At the same time 'data' may return the stack information in its 'stack' column. 


##### 2.2WebSocket API Description


**1).Establish connection**<br>
<br>
Used to establish a WebSocket connection with Linkis.
- API `/api/rest_j/entrance/connect`
- HTTP Method **GET**
- Status Code **101**<br>

**2).Request execution**<br>
<br>
Used to submit user jobs to Linkis for execution.
- API `/api/rest_j/entrance/execute`
- HTTP Method `POST`<br>
- Sample Json request body
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
			"scriptPath": "/home//linkis.sql"
		},
    "websocketTag":"37fcbd8b762d465a0c870684a0261c6e"
	}
}
```
- Descriptions for parameters of request body data<br>

|  Parameter Name | Parameter Definition |  Type | Comments   |
| ------------ | ------------ | ------------ | ------------ |
| executeApplicationName  | The Engine service expected by the user, such as Spark or hive|  String | Not null  |
| requestApplicationName  | The name of the system launching this request |  String | Nullable  |
| params  | User-defined parameters to run services |  Map | Required, but values are nullable  |
| executionCode  | The execution code submitted by the user  |  String |Not null  |
| runType  | Assuming that the user executes a spark job, he may choose python, R or SQL as runType|  String | Not null  |
| scriptPath  | The script path of the execution code  |  String | For Scriptest, it shouldn't be null with executionCode at the same time  |
                                        Table 1 Descriptions for the parameters


- Sample Json response body
```json
{
 "method": "/api/rest_j/v1/entrance/execute",
 "status": 0,
 "message": "Execution request succeeded",
 "data": {
   "execID": "030418IDEhivebdpdwc010004:10087IDE_johnnwang_21",
   "taskID": "123"  
 }
}
```
- execID is a unique ID of String type generated for each user task after submitted to Linkis. It is only used during the execution period, like PID. The format of execID is (length of requestApplicationName)(length of executeAppName)(length of Instance)${requestApplicationName}${executeApplicationName}${entranceInstance infomation ip+port}${requestApplicationName}_${umUser}_${index}
- taskID is a unique ID of Long type genenrated incrementally by the database for each task.

**3).The push mechanism for task status, logs and progress**<br>

After submission, the status, logs and progress information will be pushed by the server. They could be retrieved by websocket protocol.
The API is consistent with HTTP protocol as mentioned below. The only difference is that the schema of websocket is ws://, but http:// for HTTP protocol.

Sample response of WebSocket API
- Logs
```json
{
  "method": "/api/rest_j/v1/entrance/${execID}/log",
  "status": 0,
  "message": "Returned log information",
  "data": {
    "execID": "${execID}",
	"log": ["errorLog","warnLog","infoLog", "allLog"],
  "taskID":28594,
	"fromLine": 56
  },
  "websocketTag":"37fcbd8b762d465a0c870684a0261c6e"
}
```
- Status
```json
{
  "method": "/api/rest_j/v1/entrance/${execID}/status",
  "status": 0,
  "message": "Return status information",
  "data": {
    "execID": "${execID}",
    "taskID":28594,
	  "status": "Running",
  },
  "websocketTag":"37fcbd8b762d465a0c870684a0261c6e"
}
```
- Progress
```json
{
  "method": "/api/rest_j/v1/entrance/${execID}/log",
  "status": 0,
  "message": "Return progress information",
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



##### 2.3HTTP API description
For HTTP API, polling should be used to retrieve the status, logs and progress information after submission.


**1).Request execution**

- API `/api/rest_j/entrance/execute`
- HTTP Method `POST`<br>
- Sample JSON request body, its description is the same with Table 1
```json
{
		"params": {},
		"executeApplicationName":"spark",
		"executionCode":"show tables",
		"runType":"sql",
		"source":{
			"scriptPath": "/home/linkis/linkis.sql"
		}
}
```
- The response is consistent with websocket. Both execId and taskId will be obtained.

**2).Retrieve status**<br>
<br>
- API `/api/rest_j/entrance/${execID}/status`
- HTTP Method `GET`<br>
- Sample response body
```json
{
 "method": "/api/rest_j/v1/entrance/{execID}/status",
 "status": 0,
 "message": "Succeeded to retrieve status",
 "data": {
   "execID": "${execID}",
   "status": "Running"
 }
}
```

**3).Retrieve logs**<br>
<br>
- API `/api/rest_j/entrance/${execID}/log?fromLine=${fromLine}&size=${size}`
- HTTP Method `GET`
- Parameter fromLine specifies from which line to start. Parameter size specifies the number of lines should be retrieved for this request.
- Sample response body, the returned fromLine indicates the value of parameter fromLine for next request.
```json
{
  "method": "/api/rest_j/v1/entrance/${execID}/log",
  "status": 0,
  "message": "Return logs information",
  "data": {
    "execID": "${execID}",
	"log": ["errorLogs","warnLogs","infoLogs", "allLogs],
	"fromLine": 56
  }
}
```

**4).Retrieve progress**<br>
<br>
- API `/api/rest_j/entrance/${execID}/progress`
- HTTP Method `GET`<br>
- Sample response body
```json
{
  "method": "/api/rest_j/v1/entrance/{execID}/progress",
  "status": 0,
  "message": "Return progress information",
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
**5).kill task**<br>
<br>
- API `/api/rest_j/entrance/${execID}/kill`
- HTTP Method `POST`
- Sample response body
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



### 3.Client SDK Adaption
[Please see Linkis Quick Start](/docs/en_US/ch2/Linkis%20Quick%20Start.md)



#### 4. Multiple engine type support
Except using the engines developed by Linkis directly, backend developers can also develop their own applications based on their requirements. Divided into Entrance, EngineManager and Engine modules, one can easily split an application to adapt to Linkis. The purpose and architecture of these three modules please refer to Linkis Architect Design Docs.

#### Convention

Linkis uses Spring framework as the underlying technique. So instances of some classes can be injected using Spring annotations. Linkis provides some default common implementations. If customized classes are needed by users, they can be directly injected and replace the current implementations.

##### 4.1Entrance module adaption
**1)maven dependency**
```xml
<dependency>
  <groupId>com.webank.wedatasphere.linkis</groupId>
  <artifactId>linkis-ujes-entrance</artifactId>
  <version>0.9.2</version>
</dependency>
```
**2)Interfaces to be implemented**

There is no compulsory interface in Entrance. Below interfaces can be implemented on demand.
- EntranceParser. Used to parse request maps from frontend to a persistable Task. Class AbstractEntranceParser is already provided and only parseToTask method needs to be overridden. Linkis provides CommonEntranceParser as the default implementation.
- EngineRequester. Used to build a RequestEngine object, which can be used to request a new engine from the EngineManager.
- Scheduler. Used to schedule tasks. The default implementation provides parallel mode for multi-user situations and FIFO mode for single user pattern. It is not suggested to be customized without special purposes.

##### 4.2EngineManager module adaption
**1)maven dependency**
```xml
<dependency>
  <groupId>com.webank.wedatasphere.linkis</groupId>
  <artifactId>linkis-ujes-enginemanager</artifactId>
  <version>0.9.2</version>
</dependency>
```

**2)Interfaces to be implemented**

Below interfaces are required to be implemented in EngineManager:
- EngineCreator. Method createProcessEngineBuilder needs to be overridden in the existing AbstractEngineCreator to create an EngineBuilder.
Here ProcessEngineBuilder has already provided a class called JavaProcessEngineBuilder, which is an abstract class accomplishes configurations of classpath, JavaOpts, GC file path and log path, and opening DEBUG port in test mode. To implement JavaProcessEngineBuilder, only extra classpath and JavaOpts are needed to be specified.
- EngineResourceFactory. Method getRequestResource needs to be overridden in the existing AbstractEngineResourceFactory to declare user customized resource requirements.
- hooks. A Spring bean used to add pre and post hooks around the Engine startup procedure. Users need to specify an Array[EngineHook] for dependency injection.
- resources. A Spring bean used to register resources to RM. Users need to specify an instance of ModuleInfo for dependency injection.

##### 4.3Engine module adaption
**1)maven dependency**
```xml
<dependency>
  <groupId>com.webank.wedatasphere.linkis</groupId>
  <artifactId>linkis-ujes-engine</artifactId>
  <version>0.9.2</version>
</dependency>
```
**2)Interfaces to be implemented**

Below interfaces are required to be implemented in Engine：
- EngineExecutorFactory. Used to build an EngineExecutor from a Map by implementing method createExecutor.
- EngineExecutor. The actual executor to execute the code submitted from the entrance. Methods need to be implemented: getActualUsedResources(the resource an engine acually used), executeLine(execute a line of the code parsed by CodeParser), executeCompletely(the suplementary method for executeLine. If executeLine returns ExecuteIncomplete, new code will be submitted with the previous code together to the engine)

Below interfaces/beans are optional in Engine:
- engineHooks: Array[EngineHook], a Spring bean used to add pre and post hooks around the Engine startup procedure. Currently the system provides 2 hooks: CodeGeneratorEngineHook for UDF/Function loading and ReleaseEngineHook for releasing spare engines. The system registers engineHooks=Array(ReleaseEngineHook) only by default.
- CodeParser. Used to parse code into lines and submit one line only for each execution loop. The system registers a CodeParser returns all the code at once by default. 
- EngineParser. Used to convert a RequestTask to a Job that is acceptable by Scheduler. If not specified, the system registers an EngineParser that converts RequestTask to CommonEngineJob.