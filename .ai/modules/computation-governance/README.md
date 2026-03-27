# Computation Governance Services

The computation governance services handle the core computation task lifecycle management in Linkis.

## Service Modules

- [Entrance Service](./entrance.md) - Task submission and entrance point
- [Manager Service](./manager.md) - Resource and application management
- [ECM Service](./ecm.md) - Engine Connection Manager
- [JobHistory Service](./jobhistory.md) - Task execution history tracking

## Overview

These services form the core of Linkis' computation governance capabilities, managing the complete lifecycle of computation tasks from submission to execution and monitoring.

## Common Features

### Task Lifecycle Management
- Task submission and validation
- Task scheduling and resource allocation
- Task execution monitoring
- Task result management
- Task error handling and recovery

### Engine Management
- Dynamic engine connection creation
- Engine lifecycle management
- Engine resource monitoring
- Engine scaling capabilities

### Resource Governance
- Multi-tenant resource isolation
- Load balancing across engines
- Resource usage tracking
- Quota management

## API Interface Summary

### Entrance Service APIs
- Task submission: `POST /api/entrance/submit`
- Task status query: `GET /api/entrance/{id}/status`
- Task progress: `GET /api/entrance/{id}/progress`
- Task log retrieval: `GET /api/entrance/{id}/log`
- Task cancellation: `GET /api/entrance/{id}/kill`

### Manager Service APIs
- Engine instance management
- Resource allocation and monitoring
- Node status querying
- Engine creation requests

### ECM Service APIs
- Engine connection management
- Engine lifecycle operations
- Resource reporting
- Engine metrics collection

### JobHistory Service APIs
- Job history querying
- Job detail retrieval
- Job statistics reporting

## Database Schema Summary

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

### Common Lock Table
```sql
CREATE TABLE `linkis_ps_common_lock` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `lock_object` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `locker` VARCHAR(255) CHARSET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT 'locker',
  `time_out` longtext COLLATE utf8_bin,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_lock_object` (`lock_object`)
);
```

## RPC Methods Summary

### Entrance Service RPCs
- `submitTask(TaskRequest request)`
- `getTaskStatus(String taskId)`
- `cancelTask(String taskId)`
- `getTaskResult(String taskId)`

### Manager Service RPCs
- `requestEngine(EngineRequest request)`
- `releaseEngine(String engineId)`
- `getEngineStatus(String engineId)`
- `getNodeMetrics(String nodeId)`

### ECM Service RPCs
- `createEngineConnection(EngineCreateRequest request)`
- `terminateEngineConnection(String engineId)`
- `reportEngineResourceUsage(String engineId, ResourceUsage usage)`
- `getEngineMetrics(String engineId)`

### JobHistory Service RPCs
- `saveJobHistory(JobHistory history)`
- `queryJobHistory(JobHistoryQuery query)`
- `getJobDetails(Long jobId)`
- `updateJobStatus(Long jobId, String status)`

## Dependencies

- linkis-commons - Shared utilities
- linkis-protocol - Communication protocols
- linkis-rpc - Remote procedure calls
- Various engine connection plugins
- Spring Cloud ecosystem