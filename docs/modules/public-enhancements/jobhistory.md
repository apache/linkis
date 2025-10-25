# JobHistory Service

The JobHistory service manages job execution history and provides querying capabilities for completed tasks in the Linkis system.

## Overview

This service tracks and stores information about job executions, including task status, execution results, logs, and performance metrics. It provides APIs for querying job history, statistics, and diagnostics.

## Key Components

### Core Classes
- `LinkisJobHistoryApplication` - Main application class
- Job history persistence and querying
- Task statistics and metrics collection
- Job diagnosis and failure analysis

### Features
- Job execution history tracking
- Task result and log storage
- Performance statistics and metrics
- Job failure diagnosis
- Historical data querying and filtering

## API Interfaces

### Query Task by ID
```
GET /api/rest_j/v1/jobhistory/{id}/get
```

Parameters:
- `id` (required): Task ID
- `brief` (optional): If true, only returns brief info

Response:
```json
{
  "method": "/api/jobhistory/{id}/get",
  "status": 0,
  "message": "success",
  "data": {
    "task": {
      "jobId": 12345,
      "status": "Succeed",
      "submitUser": "testuser",
      "executeUser": "testuser",
      "instance": "bdp110:9100",
      "engineType": "spark",
      "executionCode": "SELECT * FROM table",
      "progress": "1.0",
      "logPath": "/path/to/log",
      "errorCode": 0,
      "errorDesc": "",
      "createdTime": "2023-07-27T10:00:00.000+00:00",
      "updatedTime": "2023-07-27T10:05:00.000+00:00",
      "engineStartTime": "2023-07-27T10:01:00.000+00:00",
      "runType": "sql",
      "params": {
        "configuration": {
          "runtime": {
            "spark.executor.instances": "2"
          }
        }
      }
    }
  }
}
```

### List Tasks
```
GET /api/rest_j/v1/jobhistory/list
```

Parameters:
- `startDate` (optional): Start date timestamp
- `endDate` (optional): End date timestamp
- `status` (optional): Task status filter
- `pageNow` (optional): Page number (default: 1)
- `pageSize` (optional): Page size (default: 20)
- `taskID` (optional): Specific task ID
- `executeApplicationName` (optional): Application name filter
- `creator` (optional): Creator filter
- `proxyUser` (optional): Proxy user filter
- `isAdminView` (optional): Admin view flag
- `isDeptView` (optional): Department view flag
- `instance` (optional): Instance filter
- `engineInstance` (optional): Engine instance filter
- `runType` (optional): Run type filter

Response:
```json
{
  "method": "/api/jobhistory/list",
  "status": 0,
  "message": "success",
  "data": {
    "tasks": [
      {
        "jobId": 12345,
        "status": "Succeed",
        "submitUser": "testuser",
        "executeUser": "testuser",
        "instance": "bdp110:9100",
        "engineType": "spark",
        "executionCode": "SELECT * FROM table",
        "progress": "1.0",
        "logPath": "/path/to/log",
        "errorCode": 0,
        "errorDesc": "",
        "createdTime": "2023-07-27T10:00:00.000+00:00",
        "updatedTime": "2023-07-27T10:05:00.000+00:00",
        "engineStartTime": "2023-07-27T10:01:00.000+00:00",
        "runType": "sql"
      }
    ],
    "totalPage": 100
  }
}
```

### List Undone Tasks
```
GET /api/rest_j/v1/jobhistory/listundonetasks
```

Parameters:
- `startDate` (optional): Start date timestamp
- `endDate` (optional): End date timestamp
- `status` (optional): Task status filter (default: "Running,Inited,Scheduled")
- `pageNow` (optional): Page number (default: 1)
- `pageSize` (optional): Page size (default: 20)
- `startTaskID` (optional): Start task ID
- `engineType` (optional): Engine type filter
- `creator` (optional): Creator filter

Response:
```json
{
  "method": "/api/jobhistory/listundonetasks",
  "status": 0,
  "message": "success",
  "data": {
    "tasks": [
      {
        "jobId": 12345,
        "status": "Running",
        "submitUser": "testuser",
        "executeUser": "testuser",
        "instance": "bdp110:9100",
        "engineType": "spark",
        "executionCode": "SELECT * FROM table",
        "progress": "0.5",
        "logPath": "/path/to/log",
        "errorCode": 0,
        "errorDesc": "",
        "createdTime": "2023-07-27T10:00:00.000+00:00",
        "updatedTime": "2023-07-27T10:05:00.000+00:00",
        "engineStartTime": "2023-07-27T10:01:00.000+00:00",
        "runType": "sql"
      }
    ],
    "totalPage": 10
  }
}
```

### List Undone Task Count
```
GET /api/rest_j/v1/jobhistory/listundone
```

Parameters:
- `startDate` (optional): Start date timestamp
- `endDate` (optional): End date timestamp
- `pageNow` (optional): Page number (default: 1)
- `pageSize` (optional): Page size (default: 20)
- `startTaskID` (optional): Start task ID
- `engineType` (optional): Engine type filter
- `creator` (optional): Creator filter

Response:
```json
{
  "method": "/api/jobhistory/listundone",
  "status": 0,
  "message": "success",
  "data": {
    "totalPage": 10
  }
}
```

### List Tasks by Task IDs
```
GET /api/rest_j/v1/jobhistory/list-taskids
```

Parameters:
- `taskID` (required): Comma-separated list of task IDs (max 30)

Response:
```json
{
  "method": "/api/jobhistory/list-taskids",
  "status": 0,
  "message": "success",
  "data": {
    "jobHistoryList": [
      {
        "jobId": 12345,
        "status": "Succeed",
        "submitUser": "testuser",
        "executeUser": "testuser",
        "instance": "bdp110:9100",
        "engineType": "spark",
        "executionCode": "SELECT * FROM table",
        "progress": "1.0",
        "logPath": "/path/to/log",
        "errorCode": 0,
        "errorDesc": "",
        "createdTime": "2023-07-27T10:00:00.000+00:00",
        "updatedTime": "2023-07-27T10:05:00.000+00:00",
        "engineStartTime": "2023-07-27T10:01:00.000+00:00",
        "runType": "sql"
      }
    ]
  }
}
```

### Get Job Extra Info
```
GET /api/rest_j/v1/jobhistory/job-extra-info
```

Parameters:
- `jobId` (required): Job ID

Response:
```json
{
  "method": "/api/jobhistory/job-extra-info",
  "status": 0,
  "message": "success",
  "data": {
    "metricsMap": {
      "executionCode": "SELECT * FROM table",
      "runtime": "300s",
      // Additional metrics data
    }
  }
}
```

### Download Job List
```
GET /api/rest_j/v1/jobhistory/download-job-list
```

Parameters:
- `startDate` (optional): Start date timestamp
- `endDate` (optional): End date timestamp
- `status` (optional): Task status filter
- `pageNow` (optional): Page number (default: 1)
- `pageSize` (optional): Page size (default: 20)
- `taskID` (optional): Specific task ID
- `executeApplicationName` (optional): Application name filter
- `creator` (optional): Creator filter
- `proxyUser` (optional): Proxy user filter
- `isAdminView` (optional): Admin view flag
- `isDeptView` (optional): Department view flag
- `instance` (optional): Instance filter
- `engineInstance` (optional): Engine instance filter

Response:
```
Excel file download
```

### List Duration Top Tasks
```
GET /api/rest_j/v1/jobhistory/listDurationTop
```

Parameters:
- `startDate` (optional): Start date timestamp
- `endDate` (optional): End date timestamp
- `executeApplicationName` (optional): Application name filter
- `creator` (optional): Creator filter
- `proxyUser` (optional): Proxy user filter
- `pageNow` (optional): Page number (default: 1)
- `pageSize` (optional): Page size (default: 20)

Response:
```json
{
  "method": "/api/jobhistory/listDurationTop",
  "status": 0,
  "message": "success",
  "data": {
    "tasks": [
      {
        "jobId": 12345,
        "status": "Succeed",
        "submitUser": "testuser",
        "executeUser": "testuser",
        "instance": "bdp110:9100",
        "engineType": "spark",
        "executionCode": "SELECT * FROM table",
        "progress": "1.0",
        "logPath": "/path/to/log",
        "errorCode": 0,
        "errorDesc": "",
        "createdTime": "2023-07-27T10:00:00.000+00:00",
        "updatedTime": "2023-07-27T10:05:00.000+00:00",
        "engineStartTime": "2023-07-27T10:01:00.000+00:00",
        "runType": "sql"
      }
    ]
  }
}
```

### Query Failed Task Diagnosis
```
GET /api/rest_j/v1/jobhistory/diagnosis-query
```

Parameters:
- `taskID` (required): Task ID

Response:
```json
{
  "method": "/api/jobhistory/diagnosis-query",
  "status": 0,
  "message": "success",
  "data": {
    "diagnosisMsg": "Diagnosis message content"
  }
}
```

### Get Governance Station Admin Info
```
GET /api/rest_j/v1/jobhistory/governanceStationAdmin
```

Response:
```json
{
  "method": "/api/jobhistory/governanceStationAdmin",
  "status": 0,
  "message": "success",
  "data": {
    "admin": true,
    "historyAdmin": true,
    "deptAdmin": false,
    "canResultSet": true,
    "errorMsgTip": "Error message tip"
  }
}
```

### Task Count Statistics
```
GET /api/rest_j/v1/jobhistory/jobstatistics/taskCount
```

Parameters:
- `startDate` (optional): Start date timestamp
- `endDate` (optional): End date timestamp
- `executeApplicationName` (optional): Application name filter
- `creator` (optional): Creator filter
- `proxyUser` (optional): Proxy user filter

Response:
```json
{
  "method": "/api/jobhistory/jobstatistics/taskCount",
  "status": 0,
  "message": "success",
  "data": {
    "sumCount": 100,
    "succeedCount": 80,
    "failedCount": 15,
    "cancelledCount": 5
  }
}
```

### Engine Count Statistics
```
GET /api/rest_j/v1/jobhistory/jobstatistics/engineCount
```

Parameters:
- `startDate` (optional): Start date timestamp
- `endDate` (optional): End date timestamp
- `executeApplicationName` (optional): Application name filter
- `creator` (optional): Creator filter
- `proxyUser` (optional): Proxy user filter

Response:
```json
{
  "method": "/api/jobhistory/jobstatistics/engineCount",
  "status": 0,
  "message": "success",
  "data": {
    "countEngine": 50,
    "countEngineSucceed": 40,
    "countEngineFailed": 8,
    "countEngineShutting": 2
  }
}
```

### Add Observe Info
```
POST /api/rest_j/v1/jobhistory/setting/addObserveInfo
```

Request Body:
```json
{
  "taskId": 12345,
  "receiver": "user@example.com",
  "extra": {
    "title": "Job Alert",
    "detail": "Job execution details"
  },
  "monitorLevel": "HIGH",
  "subSystemId": "subsystem1"
}
```

Response:
```json
{
  "method": "/api/jobhistory/setting/addObserveInfo",
  "status": 0,
  "message": "success"
}
```

### Delete Observe Info
```
GET /api/rest_j/v1/jobhistory/setting/deleteObserveInfo
```

Parameters:
- `taskId` (required): Task ID

Response:
```json
{
  "method": "/api/jobhistory/setting/deleteObserveInfo",
  "status": 0,
  "message": "success"
}
```

## Database Table Structures

The JobHistory service uses the following database tables for job execution history management:

### Job History Group History Table
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

The JobHistory service provides several RPC methods for job history management:

### Job History Query RPCs

#### getJobHistoryById
Retrieves job history by ID:
```java
JobHistory getJobHistoryById(Long jobId)
```

#### searchJobHistory
Searches job history with filters:
```java
List<JobHistory> searchJobHistory(JobHistorySearchRequest request)
```

#### updateJobHistory
Updates job history information:
```java
void updateJobHistory(JobHistory jobHistory)
```

#### deleteJobHistory
Deletes job history:
```java
void deleteJobHistory(Long jobId)
```

### Job Statistics RPCs

#### taskExecutionStatistics
Retrieves task execution statistics:
```java
JobStatistics taskExecutionStatistics(StatisticsRequest request)
```

#### engineExecutionStatistics
Retrieves engine execution statistics:
```java
JobStatistics engineExecutionStatistics(StatisticsRequest request)
```

### Job Diagnosis RPCs

#### diagnoseJob
Performs job diagnosis:
```java
JobDiagnosis diagnoseJob(Long jobId)
```

#### getDiagnosisInfo
Retrieves diagnosis information:
```java
JobDiagnosis getDiagnosisInfo(Long jobId)
```

## Dependencies

- linkis-jobhistory-server
- linkis-rpc
- linkis-protocol
- linkis-commons
- Database drivers (MySQL, etc.)