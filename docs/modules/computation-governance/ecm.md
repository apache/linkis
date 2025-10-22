# ECM Service

The ECM (Engine Connection Manager) service manages the lifecycle of engine connections in the Linkis system.

## Overview

This service is responsible for managing the lifecycle of engine connections, including creating, starting, stopping, and monitoring engine instances.

## Key Components

### Core Classes
- `LinkisECMApplication` - Main application class
- Engine connection lifecycle management
- Engine resource monitoring
- Engine health checking

### Features
- Engine connection creation and initialization
- Engine lifecycle management
- Resource allocation for engines
- Engine monitoring and health checking
- Engine termination and cleanup

## API Interfaces

### Download Engine Log
```
GET /api/rest_j/v1/engineconnManager/downloadEngineLog
```

Parameters:
- `emInstance`: ECM instance (required)
- `instance`: Engine instance (required)
- `logDirSuffix`: Log directory suffix (required)
- `logType`: Log type (required) - stdout, stderr, gc, or yarnApp

Response:
```
Binary file download (log file content)
```

Error Codes:
- 11110: Log directory {0} does not exists.(日志目录 {0} 不存在.)
- 911115: failed to downLoad(下载失败)
- 911116: Download file has exceeded 100MB(下载文件已超过100M)
- 911117: Parameter {0} cannot be empty (参数 {0} 不能为空)
- 911118: logType only supports stdout, stderr, gc, yarnApp(logType仅支持stdout,stderr,gc,yarnApp)
- 911119: You {0} have no permission to download Log in ECM {1}(用户 {0} 无权限下载  ECM {1} 日志)

Notes:
- Only supports GET method due to gateway forwarding rules
- File size limit is 100MB
- Supported log types: stdout, stderr, gc, yarnApp
- Requires user authentication and authorization checks
- Filename format in response: {instance}_{logType}.txt

### List All ECMs
```
GET /api/rest_j/v1/linkisManager/listAllEMs
```

Parameters:
- `instance`: ECM instance filter (optional)
- `nodeHealthy`: Node healthy status filter (optional)
- `owner`: Owner filter (optional)
- `tenantLabel`: Tenant label filter (optional)

Response:
```json
{
  "method": "/api/linkisManager/listAllEMs",
  "status": 0,
  "message": "OK",
  "data": {
    "EMs": [
      {
        "applicationName": "linkis-cg-engineconnmanager",
        "instance": "gz.bdz.bdplxxxxx.apache:9102",
        "nodeHealthy": "Healthy",
        "labels": [
          {
            "stringValue": "gz.bdz.bdplxxxxx.apache:9102",
            "labelKey": "emInstance"
          }
        ],
        "owner": "hadoop",
        "nodeStatus": "Healthy"
      }
    ]
  }
}
```

Error Codes:
- 210003: Only admin can modify ECMs(只有管理员才能修改ECM)

Notes:
- Requires admin privileges
- Returns list of all ECM instances with their status and labels

### List All ECM Healthy Status
```
GET /api/rest_j/v1/linkisManager/listAllECMHealthyStatus
```

Parameters:
- `onlyEditable`: Boolean flag to return only editable statuses (optional)

Response:
```json
{
  "method": "/api/linkisManager/listAllECMHealthyStatus",
  "status": 0,
  "message": "OK",
  "data": {
    "nodeHealthy": [
      "Healthy",
      "UnHealthy",
      "WARN",
      "StockAvailable",
      "StockUnavailable"
    ]
  }
}
```

Notes:
- Returns all possible ECM healthy status values
- When `onlyEditable` is true, returns only the statuses that can be modified

### Modify ECM Info
```
PUT /api/rest_j/v1/linkisManager/modifyEMInfo
```

Parameters:
- `applicationName`: Application name (optional)
- `emStatus`: ECM status (optional)
- `instance`: ECM instance (required)
- `labels`: Labels list (optional)
- `labelKey`: Label key (optional)
- `description`: Description (optional)
- `stringValue`: String value (optional)

Response:
```json
{
  "method": "/api/linkisManager/modifyEMInfo",
  "status": 0,
  "message": "success"
}
```

Error Codes:
- 210003: Failed to update label, include repeat labels(更新label失败，包含重复label)

Notes:
- Allows modification of ECM instance information
- Supports updating labels, status, and description

### Execute ECM Operation
```
POST /api/rest_j/v1/linkisManager/executeECMOperation
```

Request Body:
```json
{
  "serviceInstance": {
    "applicationName": "linkis-cg-engineconnmanager",
    "instance": "gz.bdz.bdplxxxxx.apache:9102"
  },
  "parameters": {
    // Operation specific parameters
  }
}
```

Response:
```json
{
  "method": "/api/linkisManager/executeECMOperation",
  "status": 0,
  "message": "OK",
  "data": {
    // Operation result data
  }
}
```

Error Codes:
- Various operation-specific error codes

Notes:
- Executes administrative operations on ECM instances
- Requires appropriate permissions
- Operation parameters vary based on the specific operation being performed

### Execute ECM Operation by Engine Connection
```
POST /api/rest_j/v1/linkisManager/executeECMOperationByEC
```

Request Body:
```json
{
  "serviceInstance": {
    "applicationName": "linkis-cg-engineconn",
    "instance": "gz.bdz.bdplxxxxx.apache:12295"
  },
  "parameters": {
    // Operation specific parameters
  }
}
```

Response:
```json
{
  "method": "/api/linkisManager/executeECMOperationByEC",
  "status": 0,
  "message": "OK",
  "data": {
    // Operation result data
  }
}
```

Error Codes:
- Permission-related errors when user doesn't own the engine connection

Notes:
- Executes ECM operations triggered by engine connections
- Validates that the user owns the engine connection or is an admin
- Operation parameters vary based on the specific operation being performed

### Reset Resource
```
GET /api/rest_j/v1/linkisManager/reset-resource
```

Parameters:
- `serviceInstance`: ECM service instance (optional)
- `username`: Username (optional)

Response:
```json
{
  "method": "/api/linkisManager/reset-resource",
  "status": 0,
  "message": "OK",
  "data": {}
}
```

Error Codes:
- Permission error when user is not admin

Notes:
- Resets resource allocation for ECM instances or users
- Requires admin privileges
- Can reset resources for a specific ECM instance or user

### Open Engine Log
```
POST /api/rest_j/v1/linkisManager/openEngineLog
```

Request Body:
```json
{
  "applicationName": "linkis-cg-engineconn",
  "emInstance": "bdp110:9100",
  "instance": "bdp110:21976",
  "parameters": {
    "logType": "stdout",
    "fromLine": "0",
    "pageSize": "1000"
  }
}
```

Response:
```json
{
  "method": "/api/linkisManager/openEngineLog",
  "status": 0,
  "message": "OK",
  "data": {
    // Log content or operation result
  }
}
```

Error Codes:
- Parameter validation errors
- Permission errors

Notes:
- Opens and retrieves engine log content
- Supports different log types (stdout, stderr, gc, udfLog, yarnApp)
- Requires appropriate permissions

### Task Prediction
```
GET /api/rest_j/v1/linkisManager/task-prediction
```

Parameters:
- `username`: Username (optional)
- `engineType`: Engine type (required)
- `creator`: Creator (required)
- `clustername`: Cluster name (optional)
- `queueName`: Queue name (optional)
- `tenant`: Tenant (optional)

Response:
```json
{
  "method": "/api/linkisManager/task-prediction",
  "status": 0,
  "message": "OK",
  "data": {
    "tenant": "tenant",
    "userResource": {},
    "ecmResource": {},
    "yarnResource": {},
    "checkResult": true
  }
}
```

Error Codes:
- Parameter validation errors

Notes:
- Predicts if a task can be executed based on available resources
- Requires engineType and creator parameters
- Returns resource availability information

### Get Engine Connection Info
```
GET /api/rest_j/v1/linkisManager/ecinfo/get
```

Parameters:
- `ticketid`: Ticket ID (required)

Response:
```json
{
  "method": "/api/linkisManager/ecinfo/get",
  "status": 0,
  "message": "OK",
  "data": {
    "ecResourceInfoRecord": {
      // Engine connection resource information
    }
  }
}
```

Error Codes:
- Ticket ID not found

Notes:
- Retrieves engine connection information by ticket ID
- Requires user to be owner or admin

### Delete Engine Connection Info
```
DELETE /api/rest_j/v1/linkisManager/ecinfo/delete/{ticketid}
```

Parameters:
- `ticketid`: Ticket ID (required)

Response:
```json
{
  "method": "/api/linkisManager/ecinfo/delete/{ticketid}",
  "status": 0,
  "message": "OK",
  "data": {
    "ecResourceInfoRecord": {
      // Deleted engine connection resource information
    }
  }
}
```

Error Codes:
- Ticket ID not found
- Permission errors

Notes:
- Deletes engine connection information by ticket ID
- Requires user to be owner or admin

### Query Engine Connection Resource History List
```
GET /api/rest_j/v1/linkisManager/ecinfo/ecrHistoryList
```

Parameters:
- `instance`: Instance (optional)
- `creator`: Creator (optional)
- `startDate`: Start date (optional)
- `endDate`: End date (optional)
- `engineType`: Engine type (optional)
- `status`: Status (optional)
- `pageNow`: Page number (optional, default: 1)
- `pageSize`: Page size (optional, default: 20)

Response:
```json
{
  "method": "/api/linkisManager/ecinfo/ecrHistoryList",
  "status": 0,
  "message": "OK",
  "data": {
    "engineList": [
      // Engine connection resource history records
    ],
    "totalPage": 100
  }
}
```

Error Codes:
- Parameter validation errors

Notes:
- Queries engine connection resource history
- Supports filtering by various parameters
- Returns paginated results

### Query Engine Connection List
```
POST /api/rest_j/v1/linkisManager/ecinfo/ecList
```

Request Body:
```json
{
  "creators": ["IDE"],
  "engineTypes": ["spark"],
  "statuss": ["Running"],
  "queueName": "default",
  "ecInstances": ["instance1", "instance2"],
  "crossCluster": false
}
```

Response:
```json
{
  "method": "/api/linkisManager/ecinfo/ecList",
  "status": 0,
  "message": "OK",
  "data": {
    "ecList": [
      // Engine connection records
    ]
  }
}
```

Error Codes:
- Parameter validation errors
- Permission errors

Notes:
- Queries engine connection list
- Requires admin privileges
- Supports filtering by various parameters

## Database Table Structures

The ECM service uses the following database tables for engine management:

### Engine Connection Plugin BML Resources Table
```sql
CREATE TABLE `linkis_cg_engine_conn_plugin_bml_resources` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `engine_conn_type` varchar(100) NOT NULL COMMENT 'Engine type',
  `version` varchar(100) COMMENT 'version',
  `file_name` varchar(255) COMMENT 'file name',
  `file_size` bigint(20)  DEFAULT 0 NOT NULL COMMENT 'file size',
  `last_modified` bigint(20)  COMMENT 'File update time',
  `bml_resource_id` varchar(100) NOT NULL COMMENT 'Owning system',
  `bml_resource_version` varchar(200) NOT NULL COMMENT 'Resource owner',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'created time',
  `last_update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'updated time',
  PRIMARY KEY (`id`)
);
```

### Manager Engine EM Table
```sql
CREATE TABLE `linkis_cg_manager_engine_em` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `engine_instance` varchar(128) COLLATE utf8_bin DEFAULT NULL,
  `em_instance` varchar(128) COLLATE utf8_bin DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
);
```

### EC Resource Info Record Table
```sql
CREATE TABLE `linkis_cg_ec_resource_info_record` (
    `id` INT(20) NOT NULL AUTO_INCREMENT,
    `label_value` VARCHAR(255) NOT NULL COMMENT 'ec labels stringValue',
    `create_user` VARCHAR(128) NOT NULL COMMENT 'ec create user',
    `service_instance` varchar(128) COLLATE utf8_bin DEFAULT NULL COMMENT 'ec instance info',
    `ecm_instance` varchar(128) COLLATE utf8_bin DEFAULT NULL COMMENT 'ecm instance info ',
    `ticket_id` VARCHAR(100) NOT NULL COMMENT 'ec ticket id',
    `status` varchar(50) DEFAULT NULL COMMENT 'EC status: Starting,Unlock,Locked,Idle,Busy,Running,ShuttingDown,Failed,Success',
    `log_dir_suffix` varchar(128) COLLATE utf8_bin DEFAULT NULL COMMENT 'log path',
    `request_times` INT(8) COMMENT 'resource request times',
    `request_resource` VARCHAR(1020) COMMENT 'request resource',
    `used_times` INT(8) COMMENT 'resource used times',
    `used_resource` VARCHAR(1020) COMMENT 'used resource',
    `metrics` TEXT DEFAULT NULL COMMENT 'ec metrics',
    `release_times` INT(8) COMMENT 'resource released times',
    `released_resource` VARCHAR(1020)  COMMENT 'released resource',
    `release_time` datetime DEFAULT NULL COMMENT 'released time',
    `used_time` datetime DEFAULT NULL COMMENT 'used time',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    PRIMARY KEY (`id`),
    KEY `idx_ticket_id` (`ticket_id`),
    UNIQUE KEY `uniq_tid_lv` (`ticket_id`,`label_value`),
    UNIQUE KEY `uniq_sinstance_status_cuser_ctime` (`service_instance`, `status`, `create_user`, `create_time`)
);
```

## RPC Methods

The ECM service provides several RPC methods for engine management:

### Engine Management RPCs

#### createEngineConnection
Creates a new engine connection:
```java
EngineConnection createEngineConnection(EngineCreateRequest request)
```

#### executeCode
Executes code on an engine:
```java
ExecutionResult executeCode(String engineId, String code, String runType)
```

#### getEngineStatus
Retrieves the status of an engine:
```java
EngineStatus getEngineStatus(String engineId)
```

#### terminateEngine
Terminates an engine connection:
```java
void terminateEngine(String engineId)
```

#### listEngines
Lists all engine connections:
```java
List<EngineInfo> listEngines()
```

#### getEngineMetrics
Retrieves metrics for an engine:
```java
EngineMetrics getEngineMetrics(String engineId)
```

### Resource Management RPCs

#### getResourceUsage
Retrieves resource usage for an engine:
```java
ResourceUsage getResourceUsage(String engineId)
```

#### updateResource
Updates resource allocation for an engine:
```java
void updateResource(String engineId, ResourceRequest request)
```

#### reportResourceUsage
Reports resource usage from an engine:
```java
void reportResourceUsage(String engineId, ResourceUsage usage)
```

### Engine Communication RPCs

#### sendEngineCommand
Sends a command to an engine:
```java
CommandResponse sendEngineCommand(String engineId, EngineCommand command)
```

#### getEngineLogs
Retrieves logs from an engine:
```java
EngineLogs getEngineLogs(String engineId, int fromLine, int lines)
```

## Dependencies

- linkis-engineconn-manager-core
- linkis-engineconn-plugin-core
- linkis-rpc
- linkis-protocol
- linkis-manager-common

## Interface Classes and MyBatis XML Files

### Interface Classes
- ECMRestfulApi: `linkis-computation-governance/linkis-engineconn-manager/linkis-engineconn-manager-server/src/main/java/org/apache/linkis/ecm/restful/ECMRestfulApi.java`
- EMRestfulApi: `linkis-computation-governance/linkis-manager/linkis-application-manager/src/main/java/org/apache/linkis/manager/am/restful/EMRestfulApi.java`
- ECResourceInfoRestfulApi: `linkis-computation-governance/linkis-manager/linkis-application-manager/src/main/java/org/apache/linkis/manager/am/restful/ECResourceInfoRestfulApi.java`

### MyBatis XML Files
The ECM service primarily uses the Manager service's persistence layer, which includes:
- LabelManagerMapper: `linkis-computation-governance/linkis-manager/linkis-manager-persistence/src/main/resources/mapper/common/LabelManagerMapper.xml`
- ResourceManagerMapper: `linkis-computation-governance/linkis-manager/linkis-manager-persistence/src/main/resources/mapper/common/ResourceManagerMapper.xml`
- NodeManagerMapper: `linkis-computation-governance/linkis-manager/linkis-manager-persistence/src/main/resources/mapper/common/NodeManagerMapper.xml`
- NodeMetricManagerMapper: `linkis-computation-governance/linkis-manager/linkis-manager-persistence/src/main/resources/mapper/common/NodeMetricManagerMapper.xml`