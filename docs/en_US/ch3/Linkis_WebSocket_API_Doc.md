## Linkis WebSocket API Doc


#### 1.Summary
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Linkis provides an adaption method by WebSocket, for the convenience of the frontend of functional applications.

The data development IDE tool [Scriptis](https://github.com/WeBankFinTech/Scriptis) combined both ways to adapt with Linkis. It communicates with Linkis by websocket in normal circumstances, and failover to HTTP protocol in case the Websocket connection was down.

##### 2.1 API Specs
Linkis defined its own specs for front-backend adaption.<br>

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
			"scriptPath": "/home/Linkis/Linkis.sql"
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
