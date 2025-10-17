# Entrance Service

The Entrance service serves as the entry point for computation task submissions in the Linkis system.

## Overview

This service is responsible for receiving user computation requests, parsing them, validating them, and coordinating their execution through the appropriate engine connections. It acts as the primary interface between users and the computation execution layer.

## Key Components

### Core Classes
- `LinkisEntranceApplication` - Main application class
- Task submission handling
- Task parsing and validation
- Task scheduling coordination
- Task execution monitoring
- Task result management

### Features
- Task submission and management
- Code parsing and validation
- Engine routing and allocation
- Result set management
- Log retrieval and management

## API Interfaces

### Task Execution
```
POST /api/entrance/execute
```

Request Body:
```json
{
  "executionContent": {
    "code": "SELECT * FROM table",
    "runType": "sql"
  },
  "params": {
    "variable": {},
    "configuration": {
      "runtime": {},
      "special": {}
    }
  },
  "source": {
    "scriptPath": "/path/to/script"
  },
  "labels": {
    "engineType": "spark-2.4.3",
    "userCreator": "user-IDE"
  }
}
```

Response:
```json
{
  "method": "/api/entrance/execute",
  "status": 0,
  "message": "success",
  "data": {
    "taskID": 12345,
    "execID": "exec-id-12345"
  }
}
```

### Task Submission
```
POST /api/entrance/submit
```

Request Body:
```json
{
  "executionContent": {
    "code": "SELECT * FROM table",
    "runType": "sql"
  },
  "params": {
    "variable": {},
    "configuration": {
      "runtime": {},
      "special": {}
    }
  },
  "source": {
    "scriptPath": "/path/to/script"
  },
  "labels": {
    "engineType": "spark-2.4.3",
    "userCreator": "user-IDE"
  }
}
```

Response:
```json
{
  "method": "/api/entrance/submit",
  "status": 0,
  "message": "success",
  "data": {
    "taskID": 12345,
    "execID": "exec-id-12345"
  }
}
```

### Task Status Query
```
GET /api/entrance/{id}/status
```

Parameters:
- `id`: The execution ID or task ID
- `taskID` (optional): The ID of the task to query

Response:
```json
{
  "method": "/api/entrance/{id}/status",
  "status": 0,
  "message": "success",
  "data": {
    "taskID": 12345,
    "status": "Running",
    "execID": "exec-id-12345"
  }
}
```

### Task Progress
```
GET /api/entrance/{id}/progress
```

Parameters:
- `id`: The execution ID

Response:
```json
{
  "method": "/api/entrance/{id}/progress",
  "status": 0,
  "message": "success",
  "data": {
    "taskID": 12345,
    "progress": "0.75",
    "execID": "exec-id-12345",
    "progressInfo": [
      {
        "id": "stage1",
        "succeedTasks": 5,
        "failedTasks": 0,
        "runningTasks": 2,
        "totalTasks": 10
      }
    ]
  }
}
```

### Task Progress with Resource Info
```
GET /api/entrance/{id}/progressWithResource
```

Parameters:
- `id`: The execution ID

Response:
```json
{
  "method": "/api/entrance/{id}/progressWithResource",
  "status": 0,
  "message": "success",
  "data": {
    "taskID": 12345,
    "progress": "0.75",
    "execID": "exec-id-12345",
    "progressInfo": [
      {
        "id": "stage1",
        "succeedTasks": 5,
        "failedTasks": 0,
        "runningTasks": 2,
        "totalTasks": 10
      }
    ],
    "jobYarnMetrics": {
      "jobYarnResource": [
        {
          "applicationId": "application_1234567890123_0001",
          "queueCores": 2,
          "queueMemory": 4096,
          "usedCores": 1,
          "usedMemory": 2048,
          "resourceType": "YARN"
        }
      ]
    }
  }
}
```

### Task Log Retrieval
```
GET /api/entrance/{id}/log
```

Parameters:
- `id`: The execution ID
- `fromLine` (optional): Starting line number (default: 0)
- `size` (optional): Number of lines to retrieve (default: 100)
- `distinctLevel` (optional): Whether to separate logs by level (default: true)

Response:
```json
{
  "method": "/api/entrance/{id}/log",
  "status": 0,
  "message": "success",
  "data": {
    "taskID": 12345,
    "log": ["log line 1", "log line 2", "log line 3"],
    "fromLine": 1,
    "execID": "exec-id-12345"
  }
}
```

### Task Cancellation
```
GET /api/entrance/{id}/kill
```

Parameters:
- `id`: The execution ID
- `taskID` (optional): The ID of the task to cancel

Response:
```json
{
  "method": "/api/entrance/{id}/kill",
  "status": 0,
  "message": "success",
  "data": {
    "taskID": 12345,
    "execID": "exec-id-12345"
  }
}
```

### Batch Task Cancellation
```
POST /api/entrance/{id}/killJobs
```

Request Body:
```json
{
  "idList": ["exec-id-1", "exec-id-2"],
  "taskIDList": [12345, 12346]
}
```

Parameters:
- `id`: The strong execution ID

Response:
```json
{
  "method": "/api/entrance/{id}/killJobs",
  "status": 0,
  "message": "success",
  "data": {
    "messages": [
      {
        "method": "/api/entrance/exec-id-1/kill",
        "status": 0,
        "message": "Successfully killed the job(成功kill了job)"
      },
      {
        "method": "/api/entrance/exec-id-2/kill",
        "status": 0,
        "message": "Successfully killed the job(成功kill了job)"
      }
    ]
  }
}
```

### Task Pause
```
GET /api/entrance/{id}/pause
```

Parameters:
- `id`: The execution ID

Response:
```json
{
  "method": "/api/entrance/{id}/pause",
  "status": 0,
  "message": "success to pause job (成功pause了job)",
  "data": {
    "execID": "exec-id-12345"
  }
}
```

## Database Table Structures

The Entrance service uses the following database tables from the job history system:

### Job History Group Table
```sql
CREATE TABLE `linkis_ps_job_history_group_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Primary Key, auto increment',
  `job_req_id` varchar(64) DEFAULT NULL COMMENT 'job execId',
  `submit_user` varchar(50) DEFAULT NULL COMMENT 'who submitted this Job',
  `execute_user` varchar(50) DEFAULT NULL COMMENT 'who actually executed this Job',
  `source` text DEFAULT NULL COMMENT 'job source',
  `labels` text DEFAULT NULL COMMENT 'job labels',
  `params` text DEFAULT NULL COMMENT 'job params',
  `progress` varchar(32) DEFAULT NULL COMMENT 'Job execution progress',
  `status` varchar(50) DEFAULT NULL COMMENT 'Script execution status, must be one of the following: Inited, WaitForRetry, Scheduled, Running, Succeed, Failed, Cancelled, Timeout',
  `log_path` varchar(200) DEFAULT NULL COMMENT 'File path of the job log',
  `error_code` int DEFAULT NULL COMMENT 'Error code. Generated when the execution of the script fails',
  `error_desc` varchar(1000) DEFAULT NULL COMMENT 'Execution description. Generated when the execution of script fails',
  `created_time` datetime(3) DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Creation time',
  `updated_time` datetime(3) DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Update time',
  `instances` varchar(250) DEFAULT NULL COMMENT 'Entrance instances',
  `metrics` text DEFAULT NULL COMMENT 'Job Metrics',
  `engine_type` varchar(32) DEFAULT NULL COMMENT 'Engine type',
  `execution_code` text DEFAULT NULL COMMENT 'Job origin code or code path',
  `result_location` varchar(500) DEFAULT NULL COMMENT 'File path of the resultsets',
  `observe_info` varchar(500) DEFAULT NULL COMMENT 'The notification information configuration of this job',
  PRIMARY KEY (`id`),
  KEY `idx_created_time` (`created_time`),
  KEY `idx_submit_user` (`submit_user`)
);
```

### Job History Detail Table
```sql
CREATE TABLE `linkis_ps_job_history_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Primary Key, auto increment',
  `job_history_id` bigint(20) NOT NULL COMMENT 'ID of JobHistory',
  `result_location` varchar(500) DEFAULT NULL COMMENT 'File path of the resultsets',
  `execution_content` text DEFAULT NULL COMMENT 'The script code or other execution content executed by this Job',
  `result_array_size` int(4) DEFAULT 0 COMMENT 'size of result array',
  `job_group_info` text DEFAULT NULL COMMENT 'Job group info/path',
  `created_time` datetime(3) DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Creation time',
  `updated_time` datetime(3) DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Update time',
  `status` varchar(32) DEFAULT NULL COMMENT 'status',
  `priority` int(4) DEFAULT 0 COMMENT 'order of subjob',
  PRIMARY KEY (`id`)
);
```

## RPC Methods

The Entrance service provides several RPC methods for inter-service communication:

### Task Management RPCs

#### submitTask
Submits a task for execution:
```java
JobRespProtocol submitTask(JobReqInsert request)
```

#### updateTask
Updates a task:
```java
JobRespProtocol updateTask(JobReqUpdate request)
```

#### batchUpdateTasks
Batch updates tasks:
```java
JobRespProtocol batchUpdateTasks(JobReqBatchUpdate request)
```

#### queryTask
Queries a task:
```java
JobRespProtocol queryTask(JobReqQuery request)
```

#### readAllTasks
Reads all tasks:
```java
JobRespProtocol readAllTasks(JobReqReadAll request)
```

#### getTaskStatus
Retrieves the status of a task:
```java
String getTaskStatus(String taskId)
```

#### cancelTask
Cancels a running task:
```java
void cancelTask(String taskId)
```

#### getTaskResult
Retrieves the result of a completed task:
```java
TaskResult getTaskResult(String taskId)
```

#### getTaskProgress
Retrieves the progress of a task:
```java
TaskProgress getTaskProgress(String taskId)
```

### Engine Management RPCs

#### requestEngine
Requests an engine for task execution:
```java
EngineConnection requestEngine(EngineRequest request)
```

#### releaseEngine
Releases an engine after task completion:
```java
void releaseEngine(String engineId)
```

#### getEngineStatus
Retrieves the status of an engine:
```java
EngineStatus getEngineStatus(String engineId)
```

### Log Management RPCs

#### getTaskLog
Retrieves logs for a specific task:
```java
TaskLog getTaskLog(String taskId, int fromLine, int pageSize)
```

#### appendTaskLog
Appends log entries for a task:
```java
void appendTaskLog(String taskId, List<String> logLines)
```

## Dependencies

- linkis-scheduler
- linkis-protocol
- linkis-rpc
- linkis-storage
- linkis-computation-governance-common
- linkis-computation-orchestrator
- linkis-pes-client
- linkis-io-file-client
- linkis-pes-rpc-client
- linkis-ps-common-lock

## Interface Classes and MyBatis XML Files

### Interface Classes
- EntranceRestfulApi: `linkis-computation-governance/linkis-entrance/src/main/java/org/apache/linkis/entrance/restful/EntranceRestfulApi.java`
- EntranceLabelRestfulApi: `linkis-computation-governance/linkis-entrance/src/main/java/org/apache/linkis/entrance/restful/EntranceLabelRestfulApi.java`
- EntranceMetricRestfulApi: `linkis-computation-governance/linkis-entrance/src/main/java/org/apache/linkis/entrance/restful/EntranceMetricRestfulApi.java`
- EntranceConsumerRestfulApi: `linkis-computation-governance/linkis-entrance/src/main/java/org/apache/linkis/entrance/restful/EntranceConsumerRestfulApi.java`

### MyBatis XML Files
The Entrance service uses the JobHistory service's persistence layer, which includes:
- JobHistoryMapper: `linkis-public-enhancements/linkis-jobhistory/src/main/resources/mapper/mysql/JobHistoryMapper.xml`
- JobDetailMapper: `linkis-public-enhancements/linkis-jobhistory/src/main/resources/mapper/common/JobDetailMapper.xml`
- JobStatisticsMapper: `linkis-public-enhancements/linkis-jobhistory/src/main/resources/mapper/common/JobStatisticsMapper.xml`
- JobDiagnosisMapper: `linkis-public-enhancements/linkis-jobhistory/src/main/resources/mapper/common/JobDiagnosisMapper.xml`
