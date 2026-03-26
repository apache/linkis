# JobHistory Service

The JobHistory service tracks and manages the execution history of tasks in the Linkis system.

## Overview

This service provides task execution history tracking, including task status, execution time, results, and error information.

## Key Components

### Core Classes
- `LinkisJobHistoryApp` - Main application class
- Task history storage and retrieval
- Task statistics and analytics
- Task search and filtering

### Features
- Task execution history tracking
- Task result storage and retrieval
- Task performance metrics
- Task search and filtering capabilities
- Task statistics and reporting

## API Interfaces

### Get Task By ID
```
GET /api/rest_j/v1/jobhistory/{id}/get
```

Parameters:
- `id`: Job ID (required)
- `brief`: Whether to return brief info only (optional)

Response:
```json
{
  "method": "/api/jobhistory/{id}/get",
  "status": 0,
  "message": "success",
  "data": {
    "task": {
      "jobId": "12345",
      "jobReqId": "job-12345",
      "submitUser": "testuser",
      "executeUser": "testuser",
      "status": "Succeed",
      "engineType": "spark",
      "createdTime": "2023-01-01 12:00:00",
      "updatedTime": "2023-01-01 12:05:00",
      "executionCode": "SELECT * FROM table",
      "resultLocation": "/path/to/result",
      "errorCode": null,
      "errorDesc": null,
      "progress": "1.0",
      "costTime": 300000
    }
  }
}
```

### List Job History
```
GET /api/rest_j/v1/jobhistory/list
```

Parameters:
- `startDate`: Start date for filtering (optional)
- `endDate`: End date for filtering (optional)
- `status`: Task status to filter by (optional)
- `pageNow`: Page number (optional, default: 1)
- `pageSize`: Page size (optional, default: 20)
- `taskID`: Task ID to filter by (optional)
- `executeApplicationName`: Application name to filter by (optional)
- `creator`: Creator to filter by (optional)
- `proxyUser`: Proxy user to filter by (optional)
- `isAdminView`: Whether to view as admin (optional)
- `isDeptView`: Whether to view as department admin (optional)
- `instance`: Instance to filter by (optional)
- `engineInstance`: Engine instance to filter by (optional)
- `runType`: Run type to filter by (optional)

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
        "jobReqId": "job-12345",
        "submitUser": "testuser",
        "executeUser": "testuser",
        "status": "Succeed",
        "engineType": "spark",
        "createdTime": "2023-01-01 12:00:00",
        "updatedTime": "2023-01-01 12:05:00",
        "executionCode": "SELECT * FROM table",
        "resultLocation": "/path/to/result",
        "errorCode": null,
        "errorDesc": null,
        "progress": "1.0",
        "costTime": 300000
      }
    ],
    "totalPage": 1
  }
}
```

### List Undone Tasks
```
GET /api/rest_j/v1/jobhistory/listundonetasks
```

Parameters:
- `startDate`: Start date for filtering (optional)
- `endDate`: End date for filtering (optional)
- `status`: Task status to filter by (optional, default: "Running,Inited,Scheduled")
- `pageNow`: Page number (optional, default: 1)
- `pageSize`: Page size (optional, default: 20)
- `startTaskID`: Start task ID (optional)
- `engineType`: Engine type to filter by (optional)
- `creator`: Creator to filter by (optional)

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
        "jobReqId": "job-12345",
        "submitUser": "testuser",
        "executeUser": "testuser",
        "status": "Running",
        "engineType": "spark",
        "createdTime": "2023-01-01 12:00:00",
        "updatedTime": "2023-01-01 12:05:00"
      }
    ],
    "totalPage": 1
  }
}
```

### List By Task IDs
```
GET /api/rest_j/v1/jobhistory/list-taskids
```

Parameters:
- `taskID`: Comma-separated list of task IDs (required)

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
        "jobReqId": "job-12345",
        "submitUser": "testuser",
        "executeUser": "testuser",
        "status": "Succeed",
        "engineType": "spark",
        "createdTime": "2023-01-01 12:00:00",
        "updatedTime": "2023-01-01 12:05:00"
      }
    ]
  }
}
```

### Job Extra Info
```
GET /api/rest_j/v1/jobhistory/job-extra-info
```

Parameters:
- `jobId`: Job ID (required)

Response:
```json
{
  "method": "/api/jobhistory/job-extra-info",
  "status": 0,
  "message": "success",
  "data": {
    "metricsMap": {
      "executionCode": "SELECT * FROM table",
      "runtime": "300000"
    }
  }
}
```

### List Duration Top
```
GET /api/rest_j/v1/jobhistory/listDurationTop
```

Parameters:
- `startDate`: Start date for filtering (optional)
- `endDate`: End date for filtering (optional)
- `executeApplicationName`: Application name to filter by (optional)
- `creator`: Creator to filter by (optional)
- `proxyUser`: Proxy user to filter by (optional)
- `pageNow`: Page number (optional, default: 1)
- `pageSize`: Page size (optional, default: 20)

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
        "jobReqId": "job-12345",
        "submitUser": "testuser",
        "executeUser": "testuser",
        "status": "Succeed",
        "engineType": "spark",
        "createdTime": "2023-01-01 12:00:00",
        "updatedTime": "2023-01-01 12:05:00",
        "costTime": 300000
      }
    ]
  }
}
```

### Task Count Statistics
```
GET /api/rest_j/v1/jobhistory/jobstatistics/taskCount
```

Parameters:
- `startDate`: Start date for filtering (optional)
- `endDate`: End date for filtering (optional)
- `executeApplicationName`: Application name to filter by (optional)
- `creator`: Creator to filter by (optional)
- `proxyUser`: Proxy user to filter by (optional)

Response:
```json
{
  "method": "/api/jobhistory/jobstatistics/taskCount",
  "status": 0,
  "message": "success",
  "data": {
    "sumCount": 100,
    "succeedCount": 95,
    "failedCount": 5,
    "cancelledCount": 0
  }
}
```

### Engine Count Statistics
```
GET /api/rest_j/v1/jobhistory/jobstatistics/engineCount
```

Parameters:
- `startDate`: Start date for filtering (optional)
- `endDate`: End date for filtering (optional)
- `executeApplicationName`: Application name to filter by (optional)
- `creator`: Creator to filter by (optional)
- `proxyUser`: Proxy user to filter by (optional)

Response:
```json
{
  "method": "/api/jobhistory/jobstatistics/engineCount",
  "status": 0,
  "message": "success",
  "data": {
    "countEngine": 100,
    "countEngineSucceed": 95,
    "countEngineFailed": 5,
    "countEngineShutting": 0
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
  "receiver": "testuser",
  "extra": {
    "title": "Task Alert",
    "detail": "Task execution alert"
  },
  "monitorLevel": "HIGH",
  "subSystemId": "1"
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
- `taskId`: Task ID (required)

Response:
```json
{
  "method": "/api/jobhistory/setting/deleteObserveInfo",
  "status": 0,
  "message": "success"
}
```

## Database Table Structures

The JobHistory service uses the following database tables from the linkis_ddl.sql file:

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

The JobHistory service provides several RPC methods for job history management:

### Job History RPCs

#### recordJob
Records a job execution:
```java
void recordJob(JobRecordRequest request)
```

#### updateJobStatus
Updates the status of a job:
```java
void updateJobStatus(String jobId, JobStatus status)
```

#### getJobHistory
Retrieves job history:
```java
JobHistory getJobHistory(String jobId)
```

#### searchJobs
Searches for jobs based on criteria:
```java
List<JobHistory> searchJobs(JobSearchCriteria criteria)
```

#### getJobDetails
Retrieves detailed job information:
```java
JobDetails getJobDetails(Long jobId)
```

#### deleteJobHistory
Deletes job history records:
```java
void deleteJobHistory(List<Long> jobIds)
```

### Statistics RPCs

#### recordStatistics
Records job statistics:
```java
void recordStatistics(JobStatistics statistics)
```

#### getStatistics
Retrieves job statistics:
```java
JobStatistics getStatistics(String jobId)
```

#### getStatisticsByUser
Retrieves job statistics for a user:
```java
List<JobStatistics> getStatisticsByUser(String username, Date startDate, Date endDate)
```

#### getStatisticsByEngine
Retrieves job statistics by engine type:
```java
List<JobStatistics> getStatisticsByEngine(String engineType, Date startDate, Date endDate)
```

## Dependencies

- linkis-mybatis
- linkis-rpc
- linkis-protocol
- linkis-common
- linkis-computation-governance-common

## Interface Classes and MyBatis XML Files

### Interface Classes
- QueryRestfulApi: `linkis-public-enhancements/linkis-jobhistory/src/main/java/org/apache/linkis/jobhistory/restful/api/QueryRestfulApi.java`
- StatisticsRestfulApi: `linkis-public-enhancements/linkis-jobhistory/src/main/java/org/apache/linkis/jobhistory/restful/api/StatisticsRestfulApi.java`
- JobhistorySettingApi: `linkis-public-enhancements/linkis-jobhistory/src/main/java/org/apache/linkis/jobhistory/restful/api/JobhistorySettingApi.java`

### MyBatis XML Files
- JobHistoryMapper: `linkis-public-enhancements/linkis-jobhistory/src/main/resources/mapper/mysql/JobHistoryMapper.xml`
- JobDetailMapper: `linkis-public-enhancements/linkis-jobhistory/src/main/resources/mapper/common/JobDetailMapper.xml`
- JobStatisticsMapper: `linkis-public-enhancements/linkis-jobhistory/src/main/resources/mapper/common/JobStatisticsMapper.xml`
- JobDiagnosisMapper: `linkis-public-enhancements/linkis-jobhistory/src/main/resources/mapper/common/JobDiagnosisMapper.xml`
- JobAiHistoryMapper: `linkis-public-enhancements/linkis-jobhistory/src/main/resources/mapper/common/JobAiHistoryMapper.xml`
