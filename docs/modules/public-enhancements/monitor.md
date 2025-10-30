# Monitor Service

Monitor service is responsible for monitoring the health status of various components in the Linkis system, including resource monitoring, node heartbeat monitoring, etc.

## Table of Contents
- [API Interfaces](#api-interfaces)
- [Database Tables](#database-tables)
- [RPC Methods](#rpc-methods)
- [Interface Classes and MyBatis XML Files](#interface-classes-and-mybatis-xml-files)

## API Interfaces

### Get Application List
**POST /linkisManager/rm/applicationlist**

Get the list of applications for a specific user.

Request Parameters:
```json
{
  "userCreator": "string",
  "engineType": "string"
}
```

Response:
```json
{
  "method": "/api/linkisManager/rm/applicationlist",
  "status": 0,
  "message": "OK",
  "data": {
    "applications": []
  }
}
```

### Reset User Resource
**DELETE /linkisManager/rm/resetResource**

Reset user resource, admin only.

Request Parameters:
- resourceId (optional): Integer

Response:
```json
{
  "method": "/api/linkisManager/rm/resetResource",
  "status": 0,
  "message": "success",
  "data": {}
}
```

### List All Engine Types
**GET /linkisManager/rm/engineType**

Get all supported engine types.

Response:
```json
{
  "method": "/api/linkisManager/rm/engineType",
  "status": 0,
  "message": "OK",
  "data": {
    "engineType": ["string"]
  }
}
```

### Get All User Resources
**GET /linkisManager/rm/allUserResource**

Get all user resources, admin only.

Request Parameters:
- username (optional): String
- creator (optional): String
- engineType (optional): String
- page (optional): Integer
- size (optional): Integer

Response:
```json
{
  "method": "/api/linkisManager/rm/allUserResource",
  "status": 0,
  "message": "OK",
  "data": {
    "resources": [],
    "total": 0
  }
}
```

### Get User Resource by Label
**GET /linkisManager/rm/get-user-resource**

Get user resource by label.

Request Parameters:
- username: String
- creator: String
- engineType: String

Response:
```json
{
  "method": "/api/linkisManager/rm/get-user-resource",
  "status": 0,
  "message": "OK",
  "data": {
    "resources": []
  }
}
```

### Get User Resources
**POST /linkisManager/rm/userresources**

Get user resources.

Request Parameters:
```json
{}
```

Response:
```json
{
  "method": "/api/linkisManager/rm/userresources",
  "status": 0,
  "message": "OK",
  "data": {
    "userResources": []
  }
}
```

### Get Engines
**POST /linkisManager/rm/engines**

Get engines for a user.

Request Parameters:
```json
{}
```

Response:
```json
{
  "method": "/api/linkisManager/rm/engines",
  "status": 0,
  "message": "OK",
  "data": {
    "engines": []
  }
}
```

### Get Queue Resource
**POST /linkisManager/rm/queueresources**

Get queue resource information.

Request Parameters:
```json
{
  "queuename": "string",
  "clustername": "string",
  "clustertype": "string",
  "crossCluster": "boolean"
}
```

Response:
```json
{
  "method": "/api/linkisManager/rm/queueresources",
  "status": 0,
  "message": "OK",
  "data": {
    "queueInfo": {},
    "userResources": []
  }
}
```

### Get Queues
**POST /linkisManager/rm/queues**

Get queue information.

Request Parameters:
```json
{}
```

Response:
```json
{
  "method": "/api/linkisManager/rm/queues",
  "status": 0,
  "message": "OK",
  "data": {
    "queues": []
  }
}
```

## Database Tables

### linkis_cg_rm_external_resource_provider
```sql
CREATE TABLE `linkis_cg_rm_external_resource_provider` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `resource_type` varchar(32) NOT NULL,
  `name` varchar(32) NOT NULL,
  `labels` varchar(32) DEFAULT NULL,
  `config` text NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
```

### linkis_cg_rm_resource_action_record
```sql
CREATE TABLE linkis_cg_rm_resource_action_record (
  `id` INT(20) NOT NULL AUTO_INCREMENT,
  `label_value` VARCHAR(100) NOT NULL,
  `ticket_id` VARCHAR(100) NOT NULL,
  `request_times` INT(8),
  `request_resource_all` VARCHAR(100),
  `used_times` INT(8),
  `used_resource_all` VARCHAR(100),
  `release_times` INT(8),
  `release_resource_all` VARCHAR(100),
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `label_value_ticket_id` (`label_value`, `ticket_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
```

## RPC Methods

Monitor service does not expose specific RPC methods directly. It primarily works through the RESTful APIs listed above.

## Interface Classes and MyBatis XML Files

### Interface Classes
- RMMonitorRest.scala: `e:\workspace\WeDataSphere\linkis\linkis-computation-governance\linkis-manager\linkis-application-manager\src\main\scala\org\apache\linkis\manager\rm\restful\RMMonitorRest.scala`

### MyBatis XML Files
The Monitor service uses the following persistence layer interfaces which may have corresponding MyBatis XML files:
- LabelManagerPersistence: `e:\workspace\WeDataSphere\linkis\linkis-computation-governance\linkis-manager\linkis-application-manager\src\main\scala\org\apache\linkis\manager\persistence\LabelManagerPersistence.scala`
- ResourceManagerPersistence: `e:\workspace\WeDataSphere\linkis\linkis-computation-governance\linkis-manager\linkis-application-manager\src\main\scala\org\apache\linkis\manager\persistence\ResourceManagerPersistence.scala`
- NodeManagerPersistence: `e:\workspace\WeDataSphere\linkis\linkis-computation-governance\linkis-manager\linkis-application-manager\src\main\scala\org\apache\linkis\manager\persistence\NodeManagerPersistence.scala`
- NodeMetricManagerPersistence: `e:\workspace\WeDataSphere\linkis\linkis-computation-governance\linkis-manager\linkis-application-manager\src\main\scala\org\apache\linkis\manager\persistence\NodeMetricManagerPersistence.scala`