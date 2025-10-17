# Manager Service

The Manager service provides resource and application management capabilities for the Linkis system.

## Overview

This service manages the resources and applications in the Linkis system, including node management, resource allocation, label management, and engine lifecycle management.

## Key Components

### Core Classes
- `LinkisManagerApplication` - Main application class
- Node management
- Resource management
- Label management
- Engine lifecycle management

### Features
- Node registration and management
- Resource allocation and monitoring
- Label-based routing
- Engine instance management
- Load balancing

## API Interfaces

### ECM (EngineConnManager) Management

#### List All ECMs
```
GET /api/rest_j/v1/linkisManager/listAllEMs
```

Parameters:
- `instance` (optional): Filter by instance name
- `nodeHealthy` (optional): Filter by node health status
- `owner` (optional): Filter by owner
- `tenantLabel` (optional): Filter by tenant label

Response:
```json
{
  "method": "/api/linkisManager/listAllEMs",
  "status": 0,
  "message": "success",
  "data": {
    "EMs": [
      {
        "serviceInstance": {
          "applicationName": "linkis-cg-engineconnmanager",
          "instance": "bdp110:9102"
        },
        "labels": [
          {
            "labelKey": "engineType",
            "stringValue": "spark"
          }
        ],
        "nodeHealthy": "Healthy",
        "owner": "testuser"
      }
    ]
  }
}
```

#### Modify ECM Info
```
PUT /api/rest_j/v1/linkisManager/modifyEMInfo
```

Request Body:
```json
{
  "applicationName": "linkis-cg-engineconnmanager",
  "instance": "bdp110:9102",
  "emStatus": "Healthy",
  "labels": [
    {
      "labelKey": "engineType",
      "stringValue": "spark"
    }
  ]
}
```

Response:
```json
{
  "method": "/api/linkisManager/modifyEMInfo",
  "status": 0,
  "message": "success"
}
```

#### Execute ECM Operation
```
POST /api/rest_j/v1/linkisManager/executeECMOperation
```

Request Body:
```json
{
  "applicationName": "linkis-cg-engineconnmanager",
  "instance": "bdp110:9102",
  "parameters": {
    "operation": "stopEngine",
    "engineConnInstance": "bdp110:12295"
  }
}
```

Response:
```json
{
  "method": "/api/linkisManager/executeECMOperation",
  "status": 0,
  "message": "success",
  "data": {
    "result": "Operation executed successfully",
    "errorMsg": "",
    "isError": false
  }
}
```

#### Open Engine Log
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
  "message": "success",
  "data": {
    "result": "Log content...",
    "errorMsg": "",
    "isError": false
  }
}
```

### Engine Management

#### Ask Engine Connection
```
POST /api/rest_j/v1/linkisManager/askEngineConn
```

Request Body:
```json
{
  "labels": {
    "engineType": "spark-2.4.3",
    "userCreator": "testuser-IDE"
  },
  "timeOut": 30000,
  "user": "testuser"
}
```

Response:
```json
{
  "method": "/api/linkisManager/askEngineConn",
  "status": 0,
  "message": "create engineConn ended.",
  "data": {
    "engine": {
      "serviceInstance": {
        "applicationName": "linkis-cg-engineconn",
        "instance": "bdp110:12295"
      },
      "nodeStatus": "Starting",
      "ticketId": "ticket-12345",
      "ecmServiceInstance": {
        "applicationName": "linkis-cg-engineconnmanager",
        "instance": "bdp110:9102"
      }
    }
  }
}
```

#### Create Engine Connection
```
POST /api/rest_j/v1/linkisManager/createEngineConn
```

Request Body:
```json
{
  "labels": {
    "engineType": "spark-2.4.3",
    "userCreator": "testuser-IDE"
  },
  "timeout": 30000,
  "user": "testuser"
}
```

Response:
```json
{
  "method": "/api/linkisManager/createEngineConn",
  "status": 0,
  "message": "create engineConn succeed.",
  "data": {
    "engine": {
      "serviceInstance": {
        "applicationName": "linkis-cg-engineconn",
        "instance": "bdp110:12295"
      },
      "nodeStatus": "Starting",
      "ticketId": "ticket-12345"
    }
  }
}
```

#### Kill Engine Connection
```
POST /api/rest_j/v1/linkisManager/killEngineConn
```

Request Body:
```json
{
  "applicationName": "linkis-cg-engineconn",
  "instance": "bdp110:12295"
}
```

Response:
```json
{
  "method": "/api/linkisManager/killEngineConn",
  "status": 0,
  "message": "Kill engineConn succeed."
}
```

#### List User Engines
```
GET /api/rest_j/v1/linkisManager/listUserEngines
```

Response:
```json
{
  "method": "/api/linkisManager/listUserEngines",
  "status": 0,
  "message": "success",
  "data": {
    "engines": [
      {
        "serviceInstance": {
          "applicationName": "linkis-cg-engineconn",
          "instance": "bdp110:12295"
        },
        "nodeStatus": "Running",
        "owner": "testuser",
        "engineType": "spark"
      }
    ]
  }
}
```

#### List ECM Engines
```
POST /api/rest_j/v1/linkisManager/listEMEngines
```

Request Body:
```json
{
  "em": {
    "serviceInstance": {
      "applicationName": "linkis-cg-engineconnmanager",
      "instance": "bdp110:9102"
    }
  },
  "emInstance": "bdp110:9102"
}
```

Response:
```json
{
  "method": "/api/linkisManager/listEMEngines",
  "status": 0,
  "message": "success",
  "data": {
    "engines": [
      {
        "serviceInstance": {
          "applicationName": "linkis-cg-engineconn",
          "instance": "bdp110:12295"
        },
        "nodeStatus": "Running",
        "owner": "testuser",
        "engineType": "spark"
      }
    ]
  }
}
```

#### Modify Engine Info
```
PUT /api/rest_j/v1/linkisManager/modifyEngineInfo
```

Request Body:
```json
{
  "applicationName": "linkis-cg-engineconn",
  "instance": "bdp110:12295",
  "labels": [
    {
      "labelKey": "engineType",
      "stringValue": "spark"
    }
  ],
  "nodeHealthy": "Healthy"
}
```

Response:
```json
{
  "method": "/api/linkisManager/modifyEngineInfo",
  "status": 0,
  "message": "success to update engine information(更新引擎信息成功)"
}
```

### EC Resource Info Management

#### Get EC Resource Info
```
GET /api/rest_j/v1/linkisManager/ecinfo/get
```

Parameters:
- `ticketid`: Ticket ID

Response:
```json
{
  "method": "/api/linkisManager/ecinfo/get",
  "status": 0,
  "message": "success",
  "data": {
    "ecResourceInfoRecord": {
      "id": 12345,
      "labelValue": "spark-2.4.3",
      "createUser": "testuser",
      "serviceInstance": "bdp110:12295",
      "ticketId": "ticket-12345",
      "status": "Running",
      "usedResource": "{\"cpu\": 2, \"memory\": \"2G\"}",
      "releasedResource": "{\"cpu\": 0, \"memory\": \"0G\"}",
      "requestResource": "{\"cpu\": 2, \"memory\": \"2G\"}"
    }
  }
}
```

#### Delete EC Resource Info
```
DELETE /api/rest_j/v1/linkisManager/ecinfo/delete/{ticketid}
```

Response:
```json
{
  "method": "/api/linkisManager/ecinfo/delete/ticket-12345",
  "status": 0,
  "message": "success",
  "data": {
    "ecResourceInfoRecord": {
      "id": 12345,
      "labelValue": "spark-2.4.3",
      "createUser": "testuser",
      "serviceInstance": "bdp110:12295",
      "ticketId": "ticket-12345",
      "status": "Running"
    }
  }
}
```

#### Query EC Resource History List
```
GET /api/rest_j/v1/linkisManager/ecinfo/ecrHistoryList
```

Parameters:
- `instance` (optional): Filter by instance
- `creator` (optional): Filter by creator
- `startDate` (optional): Filter by start date
- `endDate` (optional): Filter by end date
- `engineType` (optional): Filter by engine type
- `status` (optional): Filter by status
- `pageNow` (optional): Page number (default: 1)
- `pageSize` (optional): Page size (default: 20)

Response:
```json
{
  "method": "/api/linkisManager/ecinfo/ecrHistoryList",
  "status": 0,
  "message": "success",
  "data": {
    "engineList": [
      {
        "id": 12345,
        "labelValue": "spark-2.4.3",
        "createUser": "testuser",
        "serviceInstance": "bdp110:12295",
        "ticketId": "ticket-12345",
        "status": "Running",
        "usedResource": {
          "cpu": 2,
          "memory": "2G"
        },
        "releasedResource": {
          "cpu": 0,
          "memory": "0G"
        },
        "requestResource": {
          "cpu": 2,
          "memory": "2G"
        }
      }
    ],
    "totalPage": 1
  }
}
```

## Database Table Structures

The Manager service uses the following database tables for resource and node management:

### Service Instance Table
```sql
CREATE TABLE `linkis_cg_manager_service_instance` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `instance` varchar(128) COLLATE utf8_bin DEFAULT NULL,
  `name` varchar(32) COLLATE utf8_bin DEFAULT NULL,
  `owner` varchar(32) COLLATE utf8_bin DEFAULT NULL,
  `mark` varchar(32) COLLATE utf8_bin DEFAULT NULL,
  `identifier` varchar(32) COLLATE utf8_bin DEFAULT NULL,
  `ticketId` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `mapping_host` varchar(128) COLLATE utf8_bin DEFAULT NULL,
  `mapping_ports` varchar(128) COLLATE utf8_bin DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updator` varchar(32) COLLATE utf8_bin DEFAULT NULL,
  `creator` varchar(32) COLLATE utf8_bin DEFAULT NULL,
  `params` text COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_instance` (`instance`)
);
```

### Manager Linkis Resources Table
```sql
CREATE TABLE `linkis_cg_manager_linkis_resources` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `max_resource` varchar(1020) COLLATE utf8_bin DEFAULT NULL,
  `min_resource` varchar(1020) COLLATE utf8_bin DEFAULT NULL,
  `used_resource` varchar(1020) COLLATE utf8_bin DEFAULT NULL,
  `left_resource` varchar(1020) COLLATE utf8_bin DEFAULT NULL,
  `expected_resource` varchar(1020) COLLATE utf8_bin DEFAULT NULL,
  `locked_resource` varchar(1020) COLLATE utf8_bin DEFAULT NULL,
  `resourceType` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `ticketId` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updator` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `creator` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
);
```

### Manager Label Table
```sql
CREATE TABLE `linkis_cg_manager_label` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `label_key` varchar(32) COLLATE utf8_bin NOT NULL,
  `label_value` varchar(255) COLLATE utf8_bin NOT NULL,
  `label_feature` varchar(16) COLLATE utf8_bin NOT NULL,
  `label_value_size` int(20) NOT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_lk_lv` (`label_key`,`label_value`)
);
```

### Manager Label Value Relation Table
```sql
CREATE TABLE `linkis_cg_manager_label_value_relation` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `label_value_key` varchar(255) COLLATE utf8_bin NOT NULL,
  `label_value_content` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `label_id` int(20) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_lvk_lid` (`label_value_key`,`label_id`),
  UNIQUE KEY `unlid_lvk_lvc` (`label_id`,`label_value_key`,`label_value_content`)
);
```

### Manager Label Resource Table
```sql
CREATE TABLE `linkis_cg_manager_label_resource` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `label_id` int(20) DEFAULT NULL,
  `resource_id` int(20) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_label_id` (`label_id`)
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

The Manager service provides several RPC methods for resource and application management:

### Node Management RPCs

#### registerNode
Registers a new node:
```java
void registerNode(NodeRegistrationRequest request)
```

#### unregisterNode
Unregisters a node:
```java
void unregisterNode(String nodeId)
```

#### getNodeInfo
Retrieves information about a node:
```java
NodeInfo getNodeInfo(String nodeId)
```

#### listNodes
Lists all registered nodes:
```java
List<NodeInfo> listNodes()
```

#### getNodeMetrics
Retrieves metrics for a node:
```java
NodeMetrics getNodeMetrics(String nodeId)
```

### Resource Management RPCs

#### allocateResource
Allocates resources to a request:
```java
ResourceAllocation allocateResource(ResourceRequest request)
```

#### releaseResource
Releases allocated resources:
```java
void releaseResource(String resourceId)
```

#### getResourceInfo
Retrieves resource information:
```java
ResourceInfo getResourceInfo(String nodeId)
```

#### updateResourceUsage
Updates resource usage information:
```java
void updateResourceUsage(String nodeId, ResourceUsage usage)
```

### Label Management RPCs

#### addLabel
Adds a new label:
```java
void addLabel(Label label)
```

#### removeLabel
Removes a label:
```java
void removeLabel(String labelId)
```

#### getNodeLabels
Retrieves labels for a node:
```java
List<Label> getNodeLabels(String nodeId)
```

#### updateNodeLabels
Updates labels for a node:
```java
void updateNodeLabels(String nodeId, List<Label> labels)
```

### Engine Management RPCs

#### requestEngine
Requests creation of a new engine:
```java
EngineConnection requestEngine(EngineCreateRequest request)
```

#### terminateEngine
Terminates an engine:
```java
void terminateEngine(String engineId)
```

#### getEngineInfo
Retrieves information about an engine:
```java
EngineInfo getEngineInfo(String engineId)
```

#### listEngines
Lists all engines:
```java
List<EngineInfo> listEngines()
```

## Dependencies

- linkis-manager-common
- linkis-manager-persistence
- linkis-label-common
- linkis-rpc
- linkis-protocol

## Interface Classes and MyBatis XML Files

### Interface Classes
- EMRestfulApi: `linkis-computation-governance/linkis-manager/linkis-application-manager/src/main/java/org/apache/linkis/manager/am/restful/EMRestfulApi.java`
- EngineRestfulApi: `linkis-computation-governance/linkis-manager/linkis-application-manager/src/main/java/org/apache/linkis/manager/am/restful/EngineRestfulApi.java`
- ECResourceInfoRestfulApi: `linkis-computation-governance/linkis-manager/linkis-application-manager/src/main/java/org/apache/linkis/manager/am/restful/ECResourceInfoRestfulApi.java`

### MyBatis XML Files
- LabelManagerMapper: `linkis-computation-governance/linkis-manager/linkis-manager-persistence/src/main/resources/mapper/common/LabelManagerMapper.xml`
- ResourceManagerMapper: `linkis-computation-governance/linkis-manager/linkis-manager-persistence/src/main/resources/mapper/common/ResourceManagerMapper.xml`
- NodeManagerMapper: `linkis-computation-governance/linkis-manager/linkis-manager-persistence/src/main/resources/mapper/common/NodeManagerMapper.xml`
- NodeMetricManagerMapper: `linkis-computation-governance/linkis-manager/linkis-manager-persistence/src/main/resources/mapper/common/NodeMetricManagerMapper.xml`
- LockManagerMapper: `linkis-computation-governance/linkis-manager/linkis-manager-persistence/src/main/resources/mapper/common/LockManagerMapper.xml`
- ECResourceRecordMapper: `linkis-computation-governance/linkis-manager/linkis-manager-persistence/src/main/resources/mapper/common/ECResourceRecordMapper.xml`
