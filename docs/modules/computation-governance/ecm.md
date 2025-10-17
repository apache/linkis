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

### MyBatis XML Files
The ECM service primarily uses the Manager service's persistence layer, which includes:
- LabelManagerMapper: `linkis-computation-governance/linkis-manager/linkis-manager-persistence/src/main/resources/mapper/common/LabelManagerMapper.xml`
- ResourceManagerMapper: `linkis-computation-governance/linkis-manager/linkis-manager-persistence/src/main/resources/mapper/common/ResourceManagerMapper.xml`
- NodeManagerMapper: `linkis-computation-governance/linkis-manager/linkis-manager-persistence/src/main/resources/mapper/common/NodeManagerMapper.xml`
- NodeMetricManagerMapper: `linkis-computation-governance/linkis-manager/linkis-manager-persistence/src/main/resources/mapper/common/NodeMetricManagerMapper.xml`
