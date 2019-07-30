## Linkis HTTP接入文档


#### 1.概述
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Linkis提供了HTTP的接入方式，方便上层应用的前端，快速实现接入。


#### 2.前端接入
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;注意事项：Linkis项目前端接口提供了两种方式，HTTP和WebSocket。Websocket方式相比于HTTP方式具有对服务器友好，信息推送更加及时等优势，但是WebSocket在用户使用的时候可能出现断开连接的情况。

数据开发IDE工具[Scriptis](https://github.com/WeBankFinTech/Scriptis)在对接Linkis时，采用了WebSocket和HTTP结合的方式，正常情况下使用websocket与Linkis进行通信，出现WebSocket断开连接的时候，就会切换为HTTP的方式与后台进行交互。

##### 2.1 接口规范

Linkis在前后端进行交互的时候，自定义了一套自己的接口规范。

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


##### 2.2 WebSocket接口描述


**1).建立连接**

此接口是为了和Linkis建立一个WebSocket连接。

- `/api/rest_j/entrance/connect`
- 请求方式 **GET**
- 响应状态码 **101**<br>

**2).请求执行任务**


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

- 请求体data中的参数描述如下


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


**3).任务状态、日志、进度主动推送**


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