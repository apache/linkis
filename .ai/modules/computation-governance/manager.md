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
- `nodeHealthy` (optional): Filter by node health status (Healthy, UnHealthy, WARN, StockAvailable, StockUnavailable)
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

Error Cases:
- Only admin users can access this API
- If parameters are invalid, appropriate error messages are returned

Notes:
- Requires admin privileges
- Results can be filtered and sorted by various criteria
- Returns EMNodeVo objects with detailed information about each ECM

#### List All ECM Healthy Status
```
GET /api/rest_j/v1/linkisManager/listAllECMHealthyStatus
```

Parameters:
- `onlyEditable` (optional): If true, returns only editable statuses (Healthy, UnHealthy, WARN, StockAvailable, StockUnavailable)

Response:
```json
{
  "method": "/api/linkisManager/listAllECMHealthyStatus",
  "status": 0,
  "message": "success",
  "data": {
    "nodeHealthy": ["Healthy", "UnHealthy", "WARN", "StockAvailable", "StockUnavailable"]
  }
}
```

Notes:
- Returns all possible NodeHealthy enum values
- With onlyEditable=true, returns only the statuses that can be modified by users

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

Error Cases:
- Only admin users can modify ECM info
- If applicationName or instance is null, returns error
- If labels contain duplicates, returns error
- If label values are invalid, returns error

Notes:
- Requires admin privileges
- Can update both EM status and labels
- Supports UserModifiable labels with value validation

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

Error Cases:
- If user doesn't have permission to execute operation, returns error
- If ECM node doesn't exist, returns error
- If operation parameters are invalid, returns error

Notes:
- Supports various admin operations (configurable via AMConfiguration.ECM_ADMIN_OPERATIONS)
- For log operations, automatically fills in logDirSuffix if not provided
- Validates user permissions for admin operations

#### Execute ECM Operation By EC
```
POST /api/rest_j/v1/linkisManager/executeECMOperationByEC
```

Request Body:
```json
{
  "applicationName": "linkis-cg-engineconn",
  "instance": "bdp110:12295",
  "parameters": {
    "operation": "stopEngine"
  }
}
```

Response:
```json
{
  "method": "/api/linkisManager/executeECMOperationByEC",
  "status": 0,
  "message": "success",
  "data": {
    "result": "Operation executed successfully",
    "errorMsg": "",
    "isError": false
  }
}
```

Error Cases:
- If user doesn't have permission to execute operation, returns error
- If engine node doesn't exist, returns error
- If operation parameters are invalid, returns error

Notes:
- User must be owner of the engine or admin
- Delegates to executeECMOperation after validating permissions

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

Error Cases:
- If user doesn't have permission to access logs, returns error
- If log type is invalid, returns error
- If engine instance doesn't exist, returns error

Notes:
- Supported log types: stdout, stderr, gc, udfLog, yarnApp
- Automatically fills in logDirSuffix if not provided
- Validates user permissions (must be owner or admin)

#### Task Prediction
```
GET /api/rest_j/v1/linkisManager/task-prediction
```

Parameters:
- `username` (optional): User name (defaults to current user)
- `engineType` (required): Engine type (spark/hive/etc.)
- `creator` (required): Creator application
- `clustername` (optional): Cluster name
- `queueName` (optional): Queue name
- `tenant` (optional): Tenant

Response:
```json
{
  "method": "/api/linkisManager/task-prediction",
  "status": 0,
  "message": "success",
  "data": {
    "tenant": "tenant",
    "userResource": {...},
    "ecmResource": {...},
    "yarnResource": {...},
    "checkResult": true
  }
}
```

Error Cases:
- If engineType or creator is null, returns error
- If resource check fails, returns error

Notes:
- Checks if user can create an engine for specified parameters
- Returns detailed resource information for user, ECM, and YARN

#### Reset Resource
```
GET /api/rest_j/v1/linkisManager/reset-resource
```

Parameters:
- `serviceInstance` (optional): Service instance to reset
- `username` (optional): User name to reset

Response:
```json
{
  "method": "/api/linkisManager/reset-resource",
  "status": 0,
  "message": "success"
}
```

Error Cases:
- Only admin users can reset resources

Notes:
- Requires admin privileges
- Resets resource allocations for specified instance or user

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

Error Cases:
- If timeout is invalid, uses default timeout
- If engine creation fails, returns error with retry information

Notes:
- First attempts to reuse existing engines
- If no suitable engine found, creates a new one
- Supports async engine creation with timeout handling

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

Error Cases:
- If timeout is invalid, uses default timeout
- If engine creation fails, returns error with retry information

Notes:
- Always creates a new engine (doesn't attempt reuse)
- Supports timeout configuration
- Returns EngineNode information with service instance and ticket ID

#### Get Engine Connection
```
POST /api/rest_j/v1/linkisManager/getEngineConn
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
  "method": "/api/linkisManager/getEngineConn",
  "status": 0,
  "message": "success",
  "data": {
    "engine": {
      "serviceInstance": {
        "applicationName": "linkis-cg-engineconn",
        "instance": "bdp110:12295"
      },
      "nodeStatus": "Running",
      "ticketId": "ticket-12345"
    }
  }
}
```

Error Cases:
- If user doesn't have permission to access engine, returns error
- If engine instance doesn't exist, returns error

Notes:
- User must be owner of the engine or admin
- Can retrieve engine info by service instance or ticket ID
- Returns EC metrics if available

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

Error Cases:
- If user doesn't have permission to kill engine, returns error
- If engine instance doesn't exist, returns error

Notes:
- User must be owner of the engine or admin
- Sends EngineStopRequest to engine stop service
- Logs kill operation

#### Kill ECM Engines
```
POST /api/rest_j/v1/linkisManager/rm/killUnlockEngineByEM
```

Request Body:
```json
{
  "instance": "bdp110:9210"
}
```

Response:
```json
{
  "method": "/api/linkisManager/rm/killUnlockEngineByEM",
  "status": 0,
  "message": "Kill engineConn succeed.",
  "data": {
    "result": {...}
  }
}
```

Error Cases:
- Only admin users can kill engines by ECM
- If instance parameter is null, returns error

Notes:
- Requires admin privileges
- Kills all unlocked engines under specified ECM
- Returns result information

#### Kill Multiple Engines
```
POST /api/rest_j/v1/linkisManager/rm/enginekill
```

Request Body:
```json
[
  {
    "applicationName": "linkis-cg-engineconn",
    "engineInstance": "bdp110:12295"
  }
]
```

Response:
```json
{
  "method": "/api/linkisManager/rm/enginekill",
  "status": 0,
  "message": "Kill engineConn succeed."
}
```

Error Cases:
- If engine instances don't exist, logs error but continues

Notes:
- Kills multiple engines in a single request
- No permission check (uses internal sender)

#### Kill Multiple Engines Async
```
POST /api/rest_j/v1/linkisManager/rm/enginekillAsyn
```

Request Body:
```json
{
  "instances": ["bdp110:12295", "bdp110:12296"]
}
```

Response:
```json
{
  "method": "/api/linkisManager/rm/enginekillAsyn",
  "status": 0,
  "message": "Kill engineConn succeed."
}
```

Error Cases:
- If user is not admin and doesn't have valid token, returns error
- If instances parameter is null or empty, returns error
- If instances parameter parsing fails, returns error

Notes:
- Requires admin privileges or valid admin token
- Asynchronously stops engines with metrics update
- Supports batch killing of multiple engine instances

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

Notes:
- Returns engines owned by the current user
- Lists all engine nodes for the user

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

Error Cases:
- Only admin users can list ECM engines
- If parameters are invalid, returns error

Notes:
- Requires admin privileges
- Supports filtering by EM instance, node status, engine type, and owner
- Returns AMEngineNodeVo objects with detailed engine information

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

Error Cases:
- Only admin users can modify engine info
- If applicationName or instance is null, returns error
- If labels contain duplicates, returns error

Notes:
- Requires admin privileges
- Can update both engine labels and health status
- Health status updates only support Healthy and UnHealthy values

#### Batch Set Engine To UnHealthy
```
POST /api/rest_j/v1/linkisManager/batchSetEngineToUnHealthy
```

Request Body:
```json
{
  "instances": [
    {
      "applicationName": "linkis-cg-engineconn",
      "instance": "bdp110:12295"
    }
  ]
}
```

Response:
```json
{
  "method": "/api/linkisManager/batchSetEngineToUnHealthy",
  "status": 0,
  "message": "success to update engine information(批量更新引擎健康信息成功)"
}
```

Error Cases:
- Only admin users can set engine health status
- If instances parameter is null, returns error

Notes:
- Requires admin privileges
- Sets multiple engines to UnHealthy status
- Logs batch update operation

#### List All Node Healthy Status
```
GET /api/rest_j/v1/linkisManager/listAllNodeHealthyStatus
```

Parameters:
- `onlyEditable` (optional): If true, returns only editable statuses

Response:
```json
{
  "method": "/api/linkisManager/listAllNodeHealthyStatus",
  "status": 0,
  "message": "success",
  "data": {
    "nodeStatus": ["Starting", "Unlock", "Locked", "Idle", "Busy", "Running", "ShuttingDown", "Failed", "Success"]
  }
}
```

Notes:
- Returns all possible NodeStatus enum values
- With onlyEditable parameter, behavior is the same (returns all statuses)

#### Execute Engine Conn Operation
```
POST /api/rest_j/v1/linkisManager/executeEngineConnOperation
```

Request Body:
```json
{
  "applicationName": "linkis-cg-engineconn",
  "instance": "bdp110:12295",
  "parameters": {
    "operation": "someOperation"
  }
}
```

Response:
```json
{
  "method": "/api/linkisManager/executeEngineConnOperation",
  "status": 0,
  "message": "success",
  "data": {
    "result": "Operation result...",
    "errorMsg": "",
    "isError": false
  }
}
```

Error Cases:
- If user doesn't have permission to execute operation, returns error
- If engine instance doesn't exist, returns error
- If operation fails, returns error details

Notes:
- User must be owner of the engine or admin
- Executes arbitrary operations on engine nodes
- Returns operation result and error information

#### Kill Engines By Creator Or EngineType
```
POST /api/rest_j/v1/linkisManager/rm/killEngineByCreatorEngineType
```

Request Body:
```json
{
  "creator": "IDE",
  "engineType": "hive-2.3.3"
}
```

Response:
```json
{
  "method": "/api/linkisManager/rm/killEngineByCreatorEngineType",
  "status": 0,
  "message": "Kill engineConn succeed."
}
```

Error Cases:
- Only admin users can kill engines by creator or engine type
- If creator or engineType parameters are null, returns error

Notes:
- Requires admin privileges
- Kills all engines matching creator and engine type
- Supports cross-cluster killing with additional parameters

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

Error Cases:
- If ticket ID doesn't exist, returns error
- If user doesn't have permission to access resource info, returns error

Notes:
- User must be creator of the resource or admin
- Returns detailed EC resource information record
- Includes resource usage statistics

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

Error Cases:
- If ticket ID doesn't exist, returns error
- If user doesn't have permission to delete resource info, returns error

Notes:
- User must be creator of the resource or admin
- Deletes EC resource information record from database
- Returns deleted record information

#### Query EC Resource History List
```
GET /api/rest_j/v1/linkisManager/ecinfo/ecrHistoryList
```

Parameters:
- `instance` (optional): Filter by instance
- `creator` (optional): Filter by creator
- `startDate` (optional): Filter by start date
- `endDate` (optional): Filter by end date (defaults to current date)
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

Error Cases:
- If creator parameter is invalid, returns error
- If date parameters are invalid, uses defaults

Notes:
- Admin users can view all records, regular users only their own
- Supports date range filtering
- Supports pagination
- Converts resource strings to maps for easier consumption

#### Query EC List
```
POST /api/rest_j/v1/linkisManager/ecinfo/ecList
```

Request Body:
```json
{
  "creators": ["testuser"],
  "engineTypes": ["spark-2.4.3"],
  "statuss": ["Running"],
  "queueName": "default",
  "ecInstances": ["bdp110:12295"],
  "crossCluster": false
}
```

Response:
```json
{
  "method": "/api/linkisManager/ecinfo/ecList",
  "status": 0,
  "message": "success",
  "data": {
    "ecList": [
      {
        // EC information
      }
    ]
  }
}
```

Error Cases:
- If creator parameter is invalid, returns error
- If parameters parsing fails, returns error

Notes:
- Requires admin privileges or valid admin token
- Supports filtering by creators, engine types, statuses, queue name, and EC instances
- Supports cross-cluster filtering