# Context Service

The Context Service provides context and variable sharing capabilities for the Linkis system.

## Overview

This service manages context information and variable sharing across different engines and applications in the Linkis system.

## Key Components

### Core Classes
- `LinkisCSApplication` - Main application class
- Context management
- Variable sharing
- Context persistence

### Features
- Cross-engine context sharing
- Variable management
- Context persistence
- Context versioning

## API Interfaces

### Context ID APIs

#### Create Context ID
```
POST /api/rest_j/v1/contextservice/createContextID
```

Request Body:
```json
{
  "contextId": "context-12345",
  "user": "testuser",
  "application": "IDE",
  "source": "test-source",
  "expireType": "NORMAL",
  "expireTime": "2023-12-31 23:59:59"
}
```

Response:
```json
{
  "method": "/api/rest_j/v1/contextservice/createContextID",
  "status": 0,
  "message": "success",
  "data": {
    "contextId": "context-12345"
  }
}
```

#### Get Context ID
```
GET /api/rest_j/v1/contextservice/getContextID
```

Parameters:
- `contextId`: Context ID (required)

Response:
```json
{
  "method": "/api/rest_j/v1/contextservice/getContextID",
  "status": 0,
  "message": "success",
  "data": {
    "contextId": {
      "contextId": "context-12345",
      "user": "testuser",
      "application": "IDE",
      "source": "test-source",
      "expireType": "NORMAL",
      "expireTime": "2023-12-31 23:59:59",
      "instance": "instance-1",
      "backupInstance": "backup-instance-1",
      "updateTime": "2023-01-01 12:00:00",
      "createTime": "2023-01-01 12:00:00",
      "accessTime": "2023-01-01 12:00:00"
    }
  }
}
```

#### Update Context ID
```
POST /api/rest_j/v1/contextservice/updateContextID
```

Request Body:
```json
{
  "contextId": "context-12345",
  "user": "testuser",
  "application": "IDE",
  "source": "updated-source",
  "expireType": "NORMAL",
  "expireTime": "2023-12-31 23:59:59",
  "instance": "instance-1",
  "backupInstance": "backup-instance-1"
}
```

Response:
```json
{
  "method": "/api/rest_j/v1/contextservice/updateContextID",
  "status": 0,
  "message": "success"
}
```

#### Reset Context ID
```
POST /api/rest_j/v1/contextservice/resetContextID
```

Request Body:
```json
{
  "contextId": "context-12345"
}
```

Response:
```json
{
  "method": "/api/rest_j/v1/contextservice/resetContextID",
  "status": 0,
  "message": "success"
}
```

#### Remove Context ID
```
POST /api/rest_j/v1/contextservice/removeContextID
```

Request Body:
```json
{
  "contextId": "context-12345"
}
```

Response:
```json
{
  "method": "/api/rest_j/v1/contextservice/removeContextID",
  "status": 0,
  "message": "success"
}
```

#### Search Context ID By Time
```
GET /api/rest_j/v1/contextservice/searchContextIDByTime
```

Parameters:
- `createTimeStart`: Create time start - optional
- `createTimeEnd`: Create time end - optional
- `updateTimeStart`: Update time start - optional
- `updateTimeEnd`: Update time end - optional
- `accessTimeStart`: Access time start - optional
- `accessTimeEnd`: Access time end - optional
- `pageNow`: Page number - optional, default 1
- `pageSize`: Page size - optional, default 100

Response:
```json
{
  "method": "/api/rest_j/v1/contextservice/searchContextIDByTime",
  "status": 0,
  "message": "success",
  "data": {
    "contextIds": [
      {
        "contextId": "context-12345",
        "user": "testuser",
        "application": "IDE",
        "source": "test-source",
        "expireType": "NORMAL",
        "expireTime": "2023-12-31 23:59:59",
        "instance": "instance-1",
        "backupInstance": "backup-instance-1",
        "updateTime": "2023-01-01 12:00:00",
        "createTime": "2023-01-01 12:00:00",
        "accessTime": "2023-01-01 12:00:00"
      }
    ]
  }
}
```

### Context Value APIs

#### Get Context Value
```
POST /api/rest_j/v1/contextservice/getContextValue
```

Request Body:
```json
{
  "contextID": {
    "contextId": "context-12345"
  },
  "contextKey": {
    "key": "test-key"
  }
}
```

Response:
```json
{
  "method": "/api/rest_j/v1/contextservice/getContextValue",
  "status": 0,
  "message": "success",
  "data": {
    "contextValue": {
      "key": "test-key",
      "value": "test-value",
      "contextType": "ENV",
      "scope": "PRIVATE",
      "props": {
        "prop1": "value1"
      }
    }
  }
}
```

#### Search Context Value
```
POST /api/rest_j/v1/contextservice/searchContextValue
```

Request Body:
```json
{
  "contextID": {
    "contextId": "context-12345"
  },
  "condition": {
    "key": "test-key",
    "contextType": "ENV"
  }
}
```

Response:
```json
{
  "method": "/api/rest_j/v1/contextservice/searchContextValue",
  "status": 0,
  "message": "success",
  "data": {
    "contextKeyValue": [
      {
        "key": "test-key",
        "value": "test-value",
        "contextType": "ENV",
        "scope": "PRIVATE",
        "props": {
          "prop1": "value1"
        }
      }
    ]
  }
}
```

#### Set Value By Key
```
POST /api/rest_j/v1/contextservice/setValueByKey
```

Request Body:
```json
{
  "contextID": {
    "contextId": "context-12345"
  },
  "contextKey": {
    "key": "test-key"
  },
  "contextValue": {
    "value": "test-value",
    "contextType": "ENV",
    "scope": "PRIVATE",
    "props": {
      "prop1": "value1"
    }
  }
}
```

Response:
```json
{
  "method": "/api/rest_j/v1/contextservice/setValueByKey",
  "status": 0,
  "message": "success"
}
```

#### Set Value
```
POST /api/rest_j/v1/contextservice/setValue
```

Request Body:
```json
{
  "contextID": {
    "contextId": "context-12345"
  },
  "contextKeyValue": {
    "key": "test-key",
    "value": "test-value",
    "contextType": "ENV",
    "scope": "PRIVATE",
    "props": {
      "prop1": "value1"
    }
  }
}
```

Response:
```json
{
  "method": "/api/rest_j/v1/contextservice/setValue",
  "status": 0,
  "message": "success"
}
```

#### Reset Value
```
POST /api/rest_j/v1/contextservice/resetValue
```

Request Body:
```json
{
  "contextID": {
    "contextId": "context-12345"
  },
  "contextKey": {
    "key": "test-key"
  }
}
```

Response:
```json
{
  "method": "/api/rest_j/v1/contextservice/resetValue",
  "status": 0,
  "message": "success"
}
```

#### Remove Value
```
POST /api/rest_j/v1/contextservice/removeValue
```

Request Body:
```json
{
  "contextID": {
    "contextId": "context-12345"
  },
  "contextKey": {
    "key": "test-key"
  }
}
```

Response:
```json
{
  "method": "/api/rest_j/v1/contextservice/removeValue",
  "status": 0,
  "message": "success"
}
```

#### Remove All Value
```
POST /api/rest_j/v1/contextservice/removeAllValue
```

Request Body:
```json
{
  "contextID": {
    "contextId": "context-12345"
  }
}
```

Response:
```json
{
  "method": "/api/rest_j/v1/contextservice/removeAllValue",
  "status": 0,
  "message": "success"
}
```

#### Remove All Value By Key Prefix And Context Type
```
POST /api/rest_j/v1/contextservice/removeAllValueByKeyPrefixAndContextType
```

Request Body:
```json
{
  "contextID": {
    "contextId": "context-12345"
  },
  "contextKeyType": "ENV",
  "keyPrefix": "test"
}
```

Response:
```json
{
  "method": "/api/rest_j/v1/contextservice/removeAllValueByKeyPrefixAndContextType",
  "status": 0,
  "message": "success"
}
```

#### Remove All Value By Key And Context Type
```
POST /api/rest_j/v1/contextservice/removeAllValueByKeyAndContextType
```

Request Body:
```json
{
  "contextID": {
    "contextId": "context-12345"
  },
  "contextKeyType": "ENV",
  "contextKey": "test-key"
}
```

Response:
```json
{
  "method": "/api/rest_j/v1/contextservice/removeAllValueByKeyAndContextType",
  "status": 0,
  "message": "success"
}
```

#### Remove All Value By Key Prefix
```
POST /api/rest_j/v1/contextservice/removeAllValueByKeyPrefix
```

Request Body:
```json
{
  "contextID": {
    "contextId": "context-12345"
  },
  "keyPrefix": "test"
}
```

Response:
```json
{
  "method": "/api/rest_j/v1/contextservice/removeAllValueByKeyPrefix",
  "status": 0,
  "message": "success"
}
```

#### Clear All Context By ID
```
POST /api/rest_j/v1/contextservice/clearAllContextByID
```

Request Body:
```json
{
  "idList": ["context-12345", "context-67890"]
}
```

Response:
```json
{
  "method": "/api/rest_j/v1/contextservice/clearAllContextByID",
  "status": 0,
  "message": "success",
  "data": {
    "num": 2
  }
}
```

#### Clear All Context By Time
```
POST /api/rest_j/v1/contextservice/clearAllContextByTime
```

Request Body:
```json
{
  "createTimeStart": "2023-01-01 00:00:00",
  "createTimeEnd": "2023-12-31 23:59:59",
  "updateTimeStart": "2023-01-01 00:00:00",
  "updateTimeEnd": "2023-12-31 23:59:59"
}
```

Response:
```json
{
  "method": "/api/rest_j/v1/contextservice/clearAllContextByTime",
  "status": 0,
  "message": "success",
  "data": {
    "num": 5
  }
}
```

### Context History APIs

#### Create History
```
POST /api/rest_j/v1/contextservice/createHistory
```

Request Body:
```json
{
  "contextID": {
    "contextId": "context-12345"
  },
  "contextHistory": {
    "source": "test-source",
    "contextType": "ENV",
    "historyJson": "{\"key\":\"test-key\",\"value\":\"test-value\"}",
    "keyword": "test"
  }
}
```

Response:
```json
{
  "method": "/api/rest_j/v1/contextservice/createHistory",
  "status": 0,
  "message": "success"
}
```

#### Remove History
```
POST /api/rest_j/v1/contextservice/removeHistory
```

Request Body:
```json
{
  "contextID": {
    "contextId": "context-12345"
  },
  "contextHistory": {
    "source": "test-source"
  }
}
```

Response:
```json
{
  "method": "/api/rest_j/v1/contextservice/removeHistory",
  "status": 0,
  "message": "success"
}
```

#### Get Histories
```
POST /api/rest_j/v1/contextservice/getHistories
```

Request Body:
```json
{
  "contextID": {
    "contextId": "context-12345"
  }
}
```

Response:
```json
{
  "method": "/api/rest_j/v1/contextservice/getHistories",
  "status": 0,
  "message": "success",
  "data": {
    "contextHistory": [
      {
        "id": 1,
        "contextId": 12345,
        "source": "test-source",
        "contextType": "ENV",
        "historyJson": "{\"key\":\"test-key\",\"value\":\"test-value\"}",
        "keyword": "test",
        "updateTime": "2023-01-01 12:00:00",
        "createTime": "2023-01-01 12:00:00",
        "accessTime": "2023-01-01 12:00:00"
      }
    ]
  }
}
```

#### Get History
```
POST /api/rest_j/v1/contextservice/getHistory
```

Request Body:
```json
{
  "contextID": {
    "contextId": "context-12345"
  },
  "source": "test-source"
}
```

Response:
```json
{
  "method": "/api/rest_j/v1/contextservice/getHistory",
  "status": 0,
  "message": "success",
  "data": {
    "contextHistory": {
      "id": 1,
      "contextId": 12345,
      "source": "test-source",
      "contextType": "ENV",
      "historyJson": "{\"key\":\"test-key\",\"value\":\"test-value\"}",
      "keyword": "test",
      "updateTime": "2023-01-01 12:00:00",
      "createTime": "2023-01-01 12:00:00",
      "accessTime": "2023-01-01 12:00:00"
    }
  }
}
```

#### Search History
```
POST /api/rest_j/v1/contextservice/searchHistory
```

Request Body:
```json
{
  "contextID": {
    "contextId": "context-12345"
  },
  "keywords": ["test", "key"]
}
```

Response:
```json
{
  "method": "/api/rest_j/v1/contextservice/searchHistory",
  "status": 0,
  "message": "success",
  "data": {
    "contextHistory": [
      {
        "id": 1,
        "contextId": 12345,
        "source": "test-source",
        "contextType": "ENV",
        "historyJson": "{\"key\":\"test-key\",\"value\":\"test-value\"}",
        "keyword": "test",
        "updateTime": "2023-01-01 12:00:00",
        "createTime": "2023-01-01 12:00:00",
        "accessTime": "2023-01-01 12:00:00"
      }
    ]
  }
}
```

### Context Listener APIs

#### On Bind ID Listener
```
POST /api/rest_j/v1/contextservice/onBindIDListener
```

Request Body:
```json
{
  "contextID": {
    "contextId": "context-12345"
  },
  "source": "test-source"
}
```

Response:
```json
{
  "method": "/api/rest_j/v1/contextservice/onBindIDListener",
  "status": 0,
  "message": "success",
  "data": {
    "listener": null
  }
}
```

#### On Bind Key Listener
```
POST /api/rest_j/v1/contextservice/onBindKeyListener
```

Request Body:
```json
{
  "contextID": {
    "contextId": "context-12345"
  },
  "contextKey": {
    "key": "test-key"
  },
  "source": "test-source"
}
```

Response:
```json
{
  "method": "/api/rest_j/v1/contextservice/onBindKeyListener",
  "status": 0,
  "message": "success",
  "data": {
    "listener": null
  }
}
```

#### Heartbeat
```
POST /api/rest_j/v1/contextservice/heartbeat
```

Request Body:
```json
{
  "source": "test-source"
}
```

Response:
```json
{
  "method": "/api/rest_j/v1/contextservice/heartbeat",
  "status": 0,
  "message": "success",
  "data": {
    "ContextKeyValueBean": null
  }
}
```

## Database Table Structures

The Context Service manages the following database tables from linkis_ddl.sql:

### Context Map Table
```sql
CREATE TABLE `linkis_ps_cs_context_map` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `key` varchar(128) DEFAULT NULL,
  `context_scope` varchar(32) DEFAULT NULL,
  `context_type` varchar(32) DEFAULT NULL,
  `props` text,
  `value` mediumtext,
  `context_id` int(11) DEFAULT NULL,
  `keywords` varchar(255) DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'update unix timestamp',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `access_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'last access time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_key_cid_ctype` (`key`,`context_id`,`context_type`),
  KEY `idx_keywords` (`keywords`(191))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
```

### Context Map Listener Table
```sql
CREATE TABLE `linkis_ps_cs_context_map_listener` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `listener_source` varchar(255) DEFAULT NULL,
  `key_id` int(11) DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'update unix timestamp',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `access_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'last access time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
```

### Context History Table
```sql
CREATE TABLE `linkis_ps_cs_context_history` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `context_id` int(11) DEFAULT NULL,
  `source` text,
  `context_type` varchar(32) DEFAULT NULL,
  `history_json` text,
  `keyword` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'update unix timestamp',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `access_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'last access time',
  KEY `idx_keyword` (`keyword`(191))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
```

### Context ID Table
```sql
CREATE TABLE `linkis_ps_cs_context_id` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user` varchar(32) DEFAULT NULL,
  `application` varchar(32) DEFAULT NULL,
  `source` varchar(255) DEFAULT NULL,
  `expire_type` varchar(32) DEFAULT NULL,
  `expire_time` datetime DEFAULT NULL,
  `instance` varchar(128) DEFAULT NULL,
  `backup_instance` varchar(255) DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'update unix timestamp',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `access_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'last access time',
  PRIMARY KEY (`id`),
  KEY `idx_instance` (`instance`(128)),
  KEY `idx_backup_instance` (`backup_instance`(191)),
  KEY `idx_instance_bin` (`instance`(128),`backup_instance`(128))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
```

### Context Listener Table
```sql
CREATE TABLE `linkis_ps_cs_context_listener` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `listener_source` varchar(255) DEFAULT NULL,
  `context_id` int(11) DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'update unix timestamp',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `access_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'last access time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
```

## RPC Methods

The Context Service provides several RPC methods for context management:

### Context RPCs

#### createContext
Creates a new context:
```java
String createContext(ContextCreationRequest request)
```

#### getContext
Retrieves a context:
```java
ContextInfo getContext(String contextId)
```

#### deleteContext
Deletes a context:
```java
void deleteContext(String contextId)
```

### Variable RPCs

#### setVariable
Sets a variable in a context:
```java
void setVariable(String contextId, String key, Object value, String valueType)
```

#### getVariable
Retrieves a variable from a context:
```java
Object getVariable(String contextId, String key)
```

#### removeVariable
Removes a variable from a context:
```java
void removeVariable(String contextId, String key)
```

### History RPCs

#### getContextHistory
Retrieves context history:
```java
List<ContextHistory> getContextHistory(String contextId)
```

#### clearContextHistory
Clears context history:
```java
void clearContextHistory(String contextId)
```

## Interface Classes and MyBatis XML Files

### Interface Classes
- ContextRestfulApi: `linkis-public-enhancements/linkis-cs-server/src/main/java/org/apache/linkis/cs/server/restful/ContextRestfulApi.java`
- ContextIDRestfulApi: `linkis-public-enhancements/linkis-cs-server/src/main/java/org/apache/linkis/cs/server/restful/ContextIDRestfulApi.java`
- ContextHistoryRestfulApi: `linkis-public-enhancements/linkis-cs-server/src/main/java/org/apache/linkis/cs/server/restful/ContextHistoryRestfulApi.java`
- ContextListenerRestfulApi: `linkis-public-enhancements/linkis-cs-server/src/main/java/org/apache/linkis/cs/server/restful/ContextListenerRestfulApi.java`

### MyBatis XML Files
- ContextMapper: `linkis-public-enhancements/linkis-cs-server/src/main/resources/mapper/ContextMapper.xml`
- ContextIDMapper: `linkis-public-enhancements/linkis-cs-server/src/main/resources/mapper/ContextIDMapper.xml`
- ContextHistoryMapper: `linkis-public-enhancements/linkis-cs-server/src/main/resources/mapper/ContextHistoryMapper.xml`
- ContextListenerMapper: `linkis-public-enhancements/linkis-cs-server/src/main/resources/mapper/ContextListenerMapper.xml`

## Dependencies

- linkis-cs-server
- linkis-rpc
- linkis-protocol