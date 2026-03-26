# DataSource Service

The DataSource Service provides data source management capabilities for the Linkis system.

## Overview

This service manages data source connections, metadata, and provides unified access to various data sources.

## Key Components

### Core Classes
- `LinkisDataSourceApplication` - Main application class
- Data source management
- Metadata querying
- Connection testing

### Features
- Data source registration and management
- Metadata querying
- Connection testing and validation
- Data source versioning
- Access control

## API Interfaces

### Data Source Type APIs

#### Get All Data Source Types
```
GET /api/rest_j/v1/data-source-manager/type/all
```

Response:
```json
{
  "method": "/api/rest_j/v1/data-source-manager/type/all",
  "status": 0,
  "message": "success",
  "data": {
    "typeList": [
      {
        "id": 1,
        "name": "MySQL",
        "description": "MySQL Database",
        "option": "MySQL",
        "classifier": "Database",
        "icon": "",
        "layers": 3
      }
    ]
  }
}
```

#### Get Key Definitions By Type
```
GET /api/rest_j/v1/data-source-manager/key-define/type/{typeId}
```

Parameters:
- `typeId`: Data source type ID (required)

Response:
```json
{
  "method": "/api/rest_j/v1/data-source-manager/key-define/type/{typeId}",
  "status": 0,
  "message": "success",
  "data": {
    "keyDefine": [
      {
        "id": 1,
        "dataSourceTypeId": 1,
        "key": "host",
        "name": "Host",
        "defaultValue": "",
        "valueType": "String",
        "scope": "ENV",
        "require": 1,
        "description": "Host IP",
        "descriptionEn": "Host IP",
        "valueRegex": "",
        "refId": null,
        "refValue": null,
        "dataSource": null
      }
    ]
  }
}
```

#### Get Key Definitions By Type Name
```
GET /api/rest_j/v1/data-source-manager/key-define/{typeName}
```

Parameters:
- `typeName`: Data source type name (required)

Response:
```json
{
  "method": "/api/rest_j/v1/data-source-manager/key-define/{typeName}",
  "status": 0,
  "message": "success",
  "data": {
    "keyDefine": [
      {
        "id": 1,
        "dataSourceTypeId": 1,
        "key": "host",
        "name": "Host",
        "defaultValue": "",
        "valueType": "String",
        "scope": "ENV",
        "require": 1,
        "description": "Host IP",
        "descriptionEn": "Host IP",
        "valueRegex": "",
        "refId": null,
        "refValue": null,
        "dataSource": null
      }
    ]
  }
}
```

### Data Source Management APIs

#### Insert Data Source Info (JSON)
```
POST /api/rest_j/v1/data-source-manager/info/json
```

Request Body:
```json
{
  "dataSourceName": "mysql-ds",
  "dataSourceDesc": "MySQL Data Source",
  "dataSourceTypeId": 1,
  "createSystem": "linkis",
  "labels": [
    {
      "labelKey": "env",
      "labelValue": "production"
    }
  ],
  "connectParams": {
    "host": "localhost",
    "port": "3306",
    "username": "user",
    "password": "password",
    "database": "test"
  }
}
```

Response:
```json
{
  "method": "/api/rest_j/v1/data-source-manager/info/json",
  "status": 0,
  "message": "success",
  "data": {
    "insertId": 12345
  }
}
```

#### Insert Data Source (JSON Create)
```
POST /api/rest_j/v1/data-source-manager/info/json/create
```

Request Body:
```json
{
  "createUser": "testuser",
  "dataSourceTypeName": "starrocks",
  "connectParams": {
    "host": "localhost",
    "port": "9030",
    "driverClassName": "com.mysql.jdbc.Driver",
    "username": "user",
    "password": "password"
  }
}
```

Response:
```json
{
  "method": "/api/rest_j/v1/data-source-manager/info/json/create",
  "status": 0,
  "message": "success",
  "data": {
    "datasource": {
      "id": 12345,
      "dataSourceName": "starrocks_testuser_20230101120000",
      "dataSourceDesc": null,
      "dataSourceTypeId": 1,
      "createIdentify": null,
      "createSystem": null,
      "parameter": "{\"host\":\"localhost\",\"port\":\"9030\",\"driverClassName\":\"com.mysql.jdbc.Driver\",\"username\":\"user\",\"password\":\"password\"}",
      "createTime": "2023-01-01 12:00:00",
      "modifyTime": "2023-01-01 12:00:00",
      "createUser": "testuser",
      "modifyUser": null,
      "labels": null,
      "versionId": null,
      "expire": false,
      "publishedVersionId": 1
    }
  }
}
```

#### Update Data Source Info (JSON)
```
PUT /api/rest_j/v1/data-source-manager/info/{dataSourceId}/json
```

Parameters:
- `dataSourceId`: Data source ID (required)

Request Body:
```json
{
  "dataSourceName": "mysql-ds",
  "dataSourceDesc": "Updated MySQL Data Source",
  "dataSourceTypeId": 1,
  "createSystem": "linkis",
  "createTime": "1650426189000",
  "createUser": "testuser",
  "labels": [
    {
      "labelKey": "env",
      "labelValue": "production"
    }
  ],
  "connectParams": {
    "host": "localhost",
    "port": "3306",
    "username": "user",
    "password": "newpassword",
    "database": "test"
  }
}
```

Response:
```json
{
  "method": "/api/rest_j/v1/data-source-manager/info/{dataSourceId}/json",
  "status": 0,
  "message": "success",
  "data": {
    "updateId": 12345
  }
}
```

#### Insert Data Source Parameter (JSON)
```
POST /api/rest_j/v1/data-source-manager/parameter/{dataSourceId}/json
```

Parameters:
- `dataSourceId`: Data source ID (required)

Request Body:
```json
{
  "connectParams": {
    "host": "localhost",
    "port": "3306",
    "username": "user",
    "password": "password",
    "database": "test"
  },
  "comment": "Initial version"
}
```

Response:
```json
{
  "method": "/api/rest_j/v1/data-source-manager/parameter/{dataSourceId}/json",
  "status": 0,
  "message": "success",
  "data": {
    "version": 1
  }
}
```

#### Get Data Source Info By ID
```
GET /api/rest_j/v1/data-source-manager/info/{dataSourceId}
```

Parameters:
- `dataSourceId`: Data source ID (required)

Response:
```json
{
  "method": "/api/rest_j/v1/data-source-manager/info/{dataSourceId}",
  "status": 0,
  "message": "success",
  "data": {
    "info": {
      "id": 12345,
      "dataSourceName": "mysql-ds",
      "dataSourceDesc": "MySQL Data Source",
      "dataSourceTypeId": 1,
      "createIdentify": null,
      "createSystem": "linkis",
      "parameter": "{\"host\":\"localhost\",\"port\":\"3306\",\"username\":\"user\",\"password\":\"password\",\"database\":\"test\"}",
      "createTime": "2023-01-01 12:00:00",
      "modifyTime": "2023-01-01 12:00:00",
      "createUser": "testuser",
      "modifyUser": "testuser",
      "labels": "[{\"labelKey\":\"env\",\"labelValue\":\"production\"}]",
      "versionId": 1,
      "expire": false,
      "publishedVersionId": 1
    }
  }
}
```

#### Get Data Source Info By Name
```
GET /api/rest_j/v1/data-source-manager/info/name/{dataSourceName}
```

Parameters:
- `dataSourceName`: Data source name (required)

Response:
```json
{
  "method": "/api/rest_j/v1/data-source-manager/info/name/{dataSourceName}",
  "status": 0,
  "message": "success",
  "data": {
    "info": {
      "id": 12345,
      "dataSourceName": "mysql-ds",
      "dataSourceDesc": "MySQL Data Source",
      "dataSourceTypeId": 1,
      "createIdentify": null,
      "createSystem": "linkis",
      "parameter": "{\"host\":\"localhost\",\"port\":\"3306\",\"username\":\"user\",\"password\":\"password\",\"database\":\"test\"}",
      "createTime": "2023-01-01 12:00:00",
      "modifyTime": "2023-01-01 12:00:00",
      "createUser": "testuser",
      "modifyUser": "testuser",
      "labels": "[{\"labelKey\":\"env\",\"labelValue\":\"production\"}]",
      "versionId": 1,
      "expire": false,
      "publishedVersionId": 1
    }
  }
}
```

#### Get Published Data Source Info By Name
```
GET /api/rest_j/v1/data-source-manager/publishedInfo/name/{dataSourceName}
```

Parameters:
- `dataSourceName`: Data source name (required)

Response:
```json
{
  "method": "/api/rest_j/v1/data-source-manager/publishedInfo/name/{dataSourceName}",
  "status": 0,
  "message": "success",
  "data": {
    "info": {
      "id": 12345,
      "dataSourceName": "mysql-ds",
      "dataSourceDesc": "MySQL Data Source",
      "dataSourceTypeId": 1,
      "createIdentify": null,
      "createSystem": "linkis",
      "parameter": "{\"host\":\"localhost\",\"port\":\"3306\",\"username\":\"user\",\"password\":\"password\",\"database\":\"test\"}",
      "createTime": "2023-01-01 12:00:00",
      "modifyTime": "2023-01-01 12:00:00",
      "createUser": "testuser",
      "modifyUser": "testuser",
      "labels": "[{\"labelKey\":\"env\",\"labelValue\":\"production\"}]",
      "versionId": 1,
      "expire": false,
      "publishedVersionId": 1
    }
  }
}
```

#### Get Published Data Source Info By Type Name, User, IP and Port
```
GET /api/rest_j/v1/data-source-manager/publishedInfo/{datasourceTypeName}/{datasourceUser}/{ip}/{port}
```

Parameters:
- `datasourceTypeName`: Data source type name (required)
- `datasourceUser`: Data source user (required)
- `ip`: IP address (required)
- `port`: Port (required)

Response:
```json
{
  "method": "/api/rest_j/v1/data-source-manager/publishedInfo/{datasourceTypeName}/{datasourceUser}/{ip}/{port}",
  "status": 0,
  "message": "success",
  "data": {
    "info": {
      "id": 12345,
      "dataSourceName": "mysql-ds",
      "dataSourceDesc": "MySQL Data Source",
      "dataSourceTypeId": 1,
      "createIdentify": null,
      "createSystem": "linkis",
      "parameter": "{\"host\":\"localhost\",\"port\":\"3306\",\"username\":\"user\",\"password\":\"password\",\"database\":\"test\"}",
      "createTime": "2023-01-01 12:00:00",
      "modifyTime": "2023-01-01 12:00:00",
      "createUser": "testuser",
      "modifyUser": "testuser",
      "labels": "[{\"labelKey\":\"env\",\"labelValue\":\"production\"}]",
      "versionId": 1,
      "expire": false,
      "publishedVersionId": 1
    }
  }
}
```

#### Get Data Source Info By ID and Version
```
GET /api/rest_j/v1/data-source-manager/info/{dataSourceId}/{version}
```

Parameters:
- `dataSourceId`: Data source ID (required)
- `version`: Version ID (required)

Response:
```json
{
  "method": "/api/rest_j/v1/data-source-manager/info/{dataSourceId}/{version}",
  "status": 0,
  "message": "success",
  "data": {
    "info": {
      "id": 12345,
      "dataSourceName": "mysql-ds",
      "dataSourceDesc": "MySQL Data Source",
      "dataSourceTypeId": 1,
      "createIdentify": null,
      "createSystem": "linkis",
      "parameter": "{\"host\":\"localhost\",\"port\":\"3306\",\"username\":\"user\",\"password\":\"password\",\"database\":\"test\"}",
      "createTime": "2023-01-01 12:00:00",
      "modifyTime": "2023-01-01 12:00:00",
      "createUser": "testuser",
      "modifyUser": "testuser",
      "labels": "[{\"labelKey\":\"env\",\"labelValue\":\"production\"}]",
      "versionId": 1,
      "expire": false,
      "publishedVersionId": 1
    }
  }
}
```

#### Get Version List
```
GET /api/rest_j/v1/data-source-manager/{dataSourceId}/versions
```

Parameters:
- `dataSourceId`: Data source ID (required)

Response:
```json
{
  "method": "/api/rest_j/v1/data-source-manager/{dataSourceId}/versions",
  "status": 0,
  "message": "success",
  "data": {
    "versions": [
      {
        "versionId": 1,
        "dataSourceId": 12345,
        "parameter": "{\"host\":\"localhost\",\"port\":\"3306\",\"username\":\"user\",\"password\":\"password\",\"database\":\"test\"}",
        "comment": "Initial version",
        "createTime": "2023-01-01 12:00:00",
        "createUser": "testuser"
      }
    ]
  }
}
```

#### Publish Data Source By ID
```
POST /api/rest_j/v1/data-source-manager/publish/{dataSourceId}/{versionId}
```

Parameters:
- `dataSourceId`: Data source ID (required)
- `versionId`: Version ID (required)

Response:
```json
{
  "method": "/api/rest_j/v1/data-source-manager/publish/{dataSourceId}/{versionId}",
  "status": 0,
  "message": "success"
}
```

#### Remove Data Source
```
DELETE /api/rest_j/v1/data-source-manager/info/delete/{dataSourceId}
```

Parameters:
- `dataSourceId`: Data source ID (required)

Response:
```json
{
  "method": "/api/rest_j/v1/data-source-manager/info/delete/{dataSourceId}",
  "status": 0,
  "message": "success",
  "data": {
    "removeId": 12345
  }
}
```

#### Expire Data Source
```
PUT /api/rest_j/v1/data-source-manager/info/{dataSourceId}/expire
```

Parameters:
- `dataSourceId`: Data source ID (required)

Response:
```json
{
  "method": "/api/rest_j/v1/data-source-manager/info/{dataSourceId}/expire",
  "status": 0,
  "message": "success",
  "data": {
    "expireId": 12345
  }
}
```

#### Get Connect Params By Data Source ID
```
GET /api/rest_j/v1/data-source-manager/{dataSourceId}/connect-params
```

Parameters:
- `dataSourceId`: Data source ID (required)

Response:
```json
{
  "method": "/api/rest_j/v1/data-source-manager/{dataSourceId}/connect-params",
  "status": 0,
  "message": "success",
  "data": {
    "connectParams": {
      "host": "localhost",
      "port": "3306",
      "username": "user",
      "password": "password",
      "database": "test"
    }
  }
}
```

#### Get Connect Params By Data Source Name
```
GET /api/rest_j/v1/data-source-manager/name/{dataSourceName}/connect-params
```

Parameters:
- `dataSourceName`: Data source name (required)

Response:
```json
{
  "method": "/api/rest_j/v1/data-source-manager/name/{dataSourceName}/connect-params",
  "status": 0,
  "message": "success",
  "data": {
    "connectParams": {
      "host": "localhost",
      "port": "3306",
      "username": "user",
      "password": "password",
      "database": "test"
    }
  }
}
```

#### Connect Data Source
```
PUT /api/rest_j/v1/data-source-manager/{dataSourceId}/{version}/op/connect
```

Parameters:
- `dataSourceId`: Data source ID (required)
- `version`: Version ID (required)

Response:
```json
{
  "method": "/api/rest_j/v1/data-source-manager/{dataSourceId}/{version}/op/connect",
  "status": 0,
  "message": "success",
  "data": {
    "ok": true
  }
}
```

#### Query Data Source By IDs
```
GET /api/rest_j/v1/data-source-manager/info/ids
```

Parameters:
- `ids`: JSON array of data source IDs (required)

Response:
```json
{
  "method": "/api/rest_j/v1/data-source-manager/info/ids",
  "status": 0,
  "message": "success",
  "data": {
    "queryList": [
      {
        "id": 12345,
        "dataSourceName": "mysql-ds",
        "dataSourceDesc": "MySQL Data Source",
        "dataSourceTypeId": 1,
        "createIdentify": null,
        "createSystem": "linkis",
        "parameter": "{\"host\":\"localhost\",\"port\":\"3306\",\"username\":\"user\",\"password\":\"password\",\"database\":\"test\"}",
        "createTime": "2023-01-01 12:00:00",
        "modifyTime": "2023-01-01 12:00:00",
        "createUser": "testuser",
        "modifyUser": "testuser",
        "labels": "[{\"labelKey\":\"env\",\"labelValue\":\"production\"}]",
        "versionId": 1,
        "expire": false,
        "publishedVersionId": 1
      }
    ],
    "totalPage": 1
  }
}
```

#### Query Data Source
```
GET /api/rest_j/v1/data-source-manager/info
```

Parameters:
- `system`: Create system - optional
- `name`: Data source name - optional
- `typeId`: Data source type ID - optional
- `identifies`: Identifies - optional
- `currentPage`: Current page - optional, default 1
- `pageSize`: Page size - optional, default 10

Response:
```json
{
  "method": "/api/rest_j/v1/data-source-manager/info",
  "status": 0,
  "message": "success",
  "data": {
    "queryList": [
      {
        "id": 12345,
        "dataSourceName": "mysql-ds",
        "dataSourceDesc": "MySQL Data Source",
        "dataSourceTypeId": 1,
        "createIdentify": null,
        "createSystem": "linkis",
        "parameter": "{\"host\":\"localhost\",\"port\":\"3306\",\"username\":\"user\",\"password\":\"password\",\"database\":\"test\"}",
        "createTime": "2023-01-01 12:00:00",
        "modifyTime": "2023-01-01 12:00:00",
        "createUser": "testuser",
        "modifyUser": "testuser",
        "labels": "[{\"labelKey\":\"env\",\"labelValue\":\"production\"}]",
        "versionId": 1,
        "expire": false,
        "publishedVersionId": 1
      }
    ],
    "totalPage": 1
  }
}
```

### Data Source Environment APIs

#### Insert Data Source Environment (JSON)
```
POST /api/rest_j/v1/data-source-manager/env/json
```

Request Body:
```json
{
  "envName": "test-env",
  "envDesc": "Test Environment",
  "dataSourceTypeId": 1,
  "connectParams": {
    "host": "localhost",
    "port": "3306",
    "username": "user",
    "password": "password"
  }
}
```

Response:
```json
{
  "method": "/api/rest_j/v1/data-source-manager/env/json",
  "status": 0,
  "message": "success",
  "data": {
    "insertId": 12345
  }
}
```

#### Insert Data Source Environment Batch (JSON)
```
POST /api/rest_j/v1/data-source-manager/env/json/batch
```

Request Body:
```json
[
  {
    "envName": "test-env-1",
    "envDesc": "Test Environment 1",
    "dataSourceTypeId": 1,
    "connectParams": {
      "host": "localhost",
      "port": "3306",
      "username": "user",
      "password": "password"
    }
  },
  {
    "envName": "test-env-2",
    "envDesc": "Test Environment 2",
    "dataSourceTypeId": 1,
    "connectParams": {
      "host": "localhost",
      "port": "3307",
      "username": "user",
      "password": "password"
    }
  }
]
```

Parameters:
- `system`: System name (required)

Response:
```json
{
  "method": "/api/rest_j/v1/data-source-manager/env/json/batch",
  "status": 0,
  "message": "success",
  "data": {
    "envs": [
      {
        "id": 12345,
        "envName": "test-env-1",
        "envDesc": "Test Environment 1",
        "dataSourceTypeId": 1,
        "parameter": "{\"host\":\"localhost\",\"port\":\"3306\",\"username\":\"user\",\"password\":\"password\"}",
        "createTime": "2023-01-01 12:00:00",
        "createUser": "testuser",
        "modifyTime": "2023-01-01 12:00:00",
        "modifyUser": "testuser"
      },
      {
        "id": 12346,
        "envName": "test-env-2",
        "envDesc": "Test Environment 2",
        "dataSourceTypeId": 1,
        "parameter": "{\"host\":\"localhost\",\"port\":\"3307\",\"username\":\"user\",\"password\":\"password\"}",
        "createTime": "2023-01-01 12:00:00",
        "createUser": "testuser",
        "modifyTime": "2023-01-01 12:00:00",
        "modifyUser": "testuser"
      }
    ]
  }
}
```

#### Update Data Source Environment Batch (JSON)
```
PUT /api/rest_j/v1/data-source-manager/env/json/batch
```

Request Body:
```json
[
  {
    "id": 12345,
    "envName": "test-env-1",
    "envDesc": "Updated Test Environment 1",
    "dataSourceTypeId": 1,
    "connectParams": {
      "host": "localhost",
      "port": "3306",
      "username": "user",
      "password": "newpassword"
    }
  }
]
```

Parameters:
- `system`: System name (required)

Response:
```json
{
  "method": "/api/rest_j/v1/data-source-manager/env/json/batch",
  "status": 0,
  "message": "success",
  "data": {
    "envs": [
      {
        "id": 12345,
        "envName": "test-env-1",
        "envDesc": "Updated Test Environment 1",
        "dataSourceTypeId": 1,
        "parameter": "{\"host\":\"localhost\",\"port\":\"3306\",\"username\":\"user\",\"password\":\"newpassword\"}",
        "createTime": "2023-01-01 12:00:00",
        "createUser": "testuser",
        "modifyTime": "2023-01-01 12:00:00",
        "modifyUser": "testuser"
      }
    ]
  }
}
```

#### Get All Environment List By Data Source Type
```
GET /api/rest_j/v1/data-source-manager/env-list/all/type/{typeId}
```

Parameters:
- `typeId`: Data source type ID (required)

Response:
```json
{
  "method": "/api/rest_j/v1/data-source-manager/env-list/all/type/{typeId}",
  "status": 0,
  "message": "success",
  "data": {
    "envList": [
      {
        "id": 12345,
        "envName": "test-env",
        "envDesc": "Test Environment",
        "dataSourceTypeId": 1,
        "parameter": "{\"host\":\"localhost\",\"port\":\"3306\",\"username\":\"user\",\"password\":\"password\"}",
        "createTime": "2023-01-01 12:00:00",
        "createUser": "testuser",
        "modifyTime": "2023-01-01 12:00:00",
        "modifyUser": "testuser"
      }
    ]
  }
}
```

#### Get Environment Entity By ID
```
GET /api/rest_j/v1/data-source-manager/env/{envId}
```

Parameters:
- `envId`: Environment ID (required)

Response:
```json
{
  "method": "/api/rest_j/v1/data-source-manager/env/{envId}",
  "status": 0,
  "message": "success",
  "data": {
    "env": {
      "id": 12345,
      "envName": "test-env",
      "envDesc": "Test Environment",
      "dataSourceTypeId": 1,
      "parameter": "{\"host\":\"localhost\",\"port\":\"3306\",\"username\":\"user\",\"password\":\"password\"}",
      "createTime": "2023-01-01 12:00:00",
      "createUser": "testuser",
      "modifyTime": "2023-01-01 12:00:00",
      "modifyUser": "testuser"
    }
  }
}
```

#### Remove Environment Entity
```
DELETE /api/rest_j/v1/data-source-manager/env/{envId}
```

Parameters:
- `envId`: Environment ID (required)

Response:
```json
{
  "method": "/api/rest_j/v1/data-source-manager/env/{envId}",
  "status": 0,
  "message": "success",
  "data": {
    "removeId": 12345
  }
}
```

#### Update Data Source Environment (JSON)
```
PUT /api/rest_j/v1/data-source-manager/env/{envId}/json
```

Parameters:
- `envId`: Environment ID (required)

Request Body:
```json
{
  "envName": "test-env",
  "envDesc": "Updated Test Environment",
  "dataSourceTypeId": 1,
  "connectParams": {
    "host": "localhost",
    "port": "3306",
    "username": "user",
    "password": "newpassword"
  }
}
```

Response:
```json
{
  "method": "/api/rest_j/v1/data-source-manager/env/{envId}/json",
  "status": 0,
  "message": "success",
  "data": {
    "updateId": 12345
  }
}
```

#### Query Data Source Environment
```
GET /api/rest_j/v1/data-source-manager/env
```

Parameters:
- `name`: Environment name - optional
- `typeId`: Data source type ID - optional
- `currentPage`: Current page - optional, default 1
- `pageSize`: Page size - optional, default 10

Response:
```json
{
  "method": "/api/rest_j/v1/data-source-manager/env",
  "status": 0,
  "message": "success",
  "data": {
    "queryList": [
      {
        "id": 12345,
        "envName": "test-env",
        "envDesc": "Test Environment",
        "dataSourceTypeId": 1,
        "parameter": "{\"host\":\"localhost\",\"port\":\"3306\",\"username\":\"user\",\"password\":\"password\"}",
        "createTime": "2023-01-01 12:00:00",
        "createUser": "testuser",
        "modifyTime": "2023-01-01 12:00:00",
        "modifyUser": "testuser"
      }
    ]
  }
}
```

### Metadata Query APIs

#### Query Database Info
```
GET /api/rest_j/v1/datasource/dbs
```

Parameters:
- `permission`: Permission filter - optional

Response:
```json
{
  "method": "/api/rest_j/v1/datasource/dbs",
  "status": 0,
  "message": "success",
  "data": {
    "dbs": [
      {
        "name": "test_db",
        "permission": "READ"
      }
    ]
  }
}
```

#### Query Partition Exists
```
GET /api/rest_j/v1/datasource/partitionExists
```

Parameters:
- `database`: Database name (required)
- `table`: Table name (required)
- `partition`: Partition name (required)

Response:
```json
{
  "method": "/api/rest_j/v1/datasource/partitionExists",
  "status": 0,
  "message": "success",
  "data": {
    "partitionExists": true
  }
}
```

#### Query Databases With Tables
```
GET /api/rest_j/v1/datasource/all
```

Response:
```json
{
  "method": "/api/rest_j/v1/datasource/all",
  "status": 0,
  "message": "success",
  "data": {
    "dbs": [
      {
        "name": "test_db",
        "tables": [
          {
            "name": "test_table"
          }
        ]
      }
    ]
  }
}
```

#### Query Databases With Tables Order By Access Time
```
GET /api/rest_j/v1/datasource/getByAccessTime
```

Response:
```json
{
  "method": "/api/rest_j/v1/datasource/getByAccessTime",
  "status": 0,
  "message": "success",
  "data": {
    "dbs": [
      {
        "name": "test_db",
        "tables": [
          {
            "name": "test_table",
            "lastAccessTime": "2023-01-01 12:00:00"
          }
        ]
      }
    ]
  }
}
```

#### Query Tables
```
GET /api/rest_j/v1/datasource/tables
```

Parameters:
- `database`: Database name - optional

Response:
```json
{
  "method": "/api/rest_j/v1/datasource/tables",
  "status": 0,
  "message": "success",
  "data": {
    "tables": [
      {
        "name": "test_table"
      }
    ]
  }
}
```

#### Query Table Metadata
```
GET /api/rest_j/v1/datasource/columns
```

Parameters:
- `database`: Database name - optional
- `table`: Table name - optional

Response:
```json
{
  "method": "/api/rest_j/v1/datasource/columns",
  "status": 0,
  "message": "success",
  "data": {
    "columns": [
      {
        "name": "id",
        "type": "INT",
        "comment": "Primary key"
      },
      {
        "name": "name",
        "type": "VARCHAR",
        "comment": "Name field"
      }
    ]
  }
}
```

#### Get Table Size
```
GET /api/rest_j/v1/datasource/size
```

Parameters:
- `database`: Database name - optional
- `table`: Table name - optional
- `partition`: Partition name - optional

Response:
```json
{
  "method": "/api/rest_j/v1/datasource/size",
  "status": 0,
  "message": "success",
  "data": {
    "sizeInfo": {
      "size": "10MB",
      "fileCount": 5
    }
  }
}
```

#### Get Storage Info
```
GET /api/rest_j/v1/datasource/storage-info
```

Parameters:
- `database`: Database name (required)
- `table`: Table name (required)

Response:
```json
{
  "method": "/api/rest_j/v1/datasource/storage-info",
  "status": 0,
  "message": "success",
  "data": {
    "storageInfo": {
      "location": "/path/to/table",
      "format": "PARQUET",
      "compression": "SNAPPY"
    }
  }
}
```

#### Get Partitions
```
GET /api/rest_j/v1/datasource/partitions
```

Parameters:
- `database`: Database name - optional
- `table`: Table name - optional

Response:
```json
{
  "method": "/api/rest_j/v1/datasource/partitions",
  "status": 0,
  "message": "success",
  "data": {
    "partitionInfo": [
      {
        "name": "dt=20230101",
        "location": "/path/to/partition"
      }
    ]
  }
}
```

### Data Source Type Management APIs

#### List Data Source Types
```
GET /api/rest_j/v1/basedata-manager/datasource-type
```

Parameters:
- `searchName`: Search name - optional
- `currentPage`: Current page - optional, default 1
- `pageSize`: Page size - optional, default 10

Response:
```json
{
  "method": "",
  "status": 0,
  "message": "",
  "data": {
    "list": {
      "total": 1,
      "list": [
        {
          "id": 1,
          "name": "MySQL",
          "description": "MySQL Database",
          "option": "MySQL",
          "classifier": "Database",
          "icon": "",
          "layers": 3,
          "descriptionEn": "MySQL Database",
          "optionEn": "MySQL",
          "classifierEn": "Database"
        }
      ],
      "pageNum": 1,
      "pageSize": 10,
      "size": 1,
      "startRow": 1,
      "endRow": 1,
      "pages": 1,
      "prePage": 0,
      "nextPage": 0,
      "isFirstPage": true,
      "isLastPage": true,
      "hasPreviousPage": false,
      "hasNextPage": false,
      "navigatePages": 8,
      "navigatepageNums": [
        1
      ]
    }
  }
}
```

#### Get Data Source Type
```
GET /api/rest_j/v1/basedata-manager/datasource-type/{id}
```

Parameters:
- `id`: Data source type ID (required)

Response:
```json
{
  "method": "",
  "status": 0,
  "message": "",
  "data": {
    "item": {
      "id": 1,
      "name": "MySQL",
      "description": "MySQL Database",
      "option": "MySQL",
      "classifier": "Database",
      "icon": "",
      "layers": 3,
      "descriptionEn": "MySQL Database",
      "optionEn": "MySQL",
      "classifierEn": "Database"
    }
  }
}
```

#### Add Data Source Type
```
POST /api/rest_j/v1/basedata-manager/datasource-type
```

Request Body:
```json
{
  "name": "PostgreSQL",
  "description": "PostgreSQL Database",
  "option": "PostgreSQL",
  "classifier": "Database",
  "icon": "",
  "layers": 3,
  "descriptionEn": "PostgreSQL Database",
  "optionEn": "PostgreSQL",
  "classifierEn": "Database"
}
```

Response:
```json
{
  "method": "",
  "status": 0,
  "message": "",
  "data": {
    "result": true
  }
}
```

#### Remove Data Source Type
```
DELETE /api/rest_j/v1/basedata-manager/datasource-type/{id}
```

Parameters:
- `id`: Data source type ID (required)

Response:
```json
{
  "method": "",
  "status": 0,
  "message": "",
  "data": {
    "result": true
  }
}
```

#### Update Data Source Type
```
PUT /api/rest_j/v1/basedata-manager/datasource-type
```

Request Body:
```json
{
  "id": 1,
  "name": "MySQL",
  "description": "Updated MySQL Database",
  "option": "MySQL",
  "classifier": "Database",
  "icon": "",
  "layers": 3,
  "descriptionEn": "Updated MySQL Database",
  "optionEn": "MySQL",
  "classifierEn": "Database"
}
```

Response:
```json
{
  "method": "",
  "status": 0,
  "message": "",
  "data": {
    "result": true
  }
}
```

### Data Source Access Management APIs

#### List Data Source Accesses
```
GET /api/rest_j/v1/basedata-manager/datasource-access
```

Parameters:
- `searchName`: Search name - optional
- `currentPage`: Current page - optional, default 1
- `pageSize`: Page size - optional, default 10

Response:
```json
{
  "method": "",
  "status": 0,
  "message": "",
  "data": {
    "list": {
      "total": 1,
      "list": [
        {
          "id": 1,
          "tokenId": 1,
          "serviceId": 1,
          "accessTime": "2023-01-01 12:00:00"
        }
      ],
      "pageNum": 1,
      "pageSize": 10,
      "size": 1,
      "startRow": 1,
      "endRow": 1,
      "pages": 1,
      "prePage": 0,
      "nextPage": 0,
      "isFirstPage": true,
      "isLastPage": true,
      "hasPreviousPage": false,
      "hasNextPage": false,
      "navigatePages": 8,
      "navigatepageNums": [
        1
      ]
    }
  }
}
```

#### Get Data Source Access
```
GET /api/rest_j/v1/basedata-manager/datasource-access/{id}
```

Parameters:
- `id`: Data source access ID (required)

Response:
```json
{
  "method": "",
  "status": 0,
  "message": "",
  "data": {
    "item": {
      "id": 1,
      "tokenId": 1,
      "serviceId": 1,
      "accessTime": "2023-01-01 12:00:00"
    }
  }
}
```

#### Add Data Source Access
```
POST /api/rest_j/v1/basedata-manager/datasource-access
```

Request Body:
```json
{
  "tokenId": 1,
  "serviceId": 1
}
```

Response:
```json
{
  "method": "",
  "status": 0,
  "message": "",
  "data": {
    "result": true
  }
}
```

#### Remove Data Source Access
```
DELETE /api/rest_j/v1/basedata-manager/datasource-access/{id}
```

Parameters:
- `id`: Data source access ID (required)

Response:
```json
{
  "method": "",
  "status": 0,
  "message": "",
  "data": {
    "result": true
  }
}
```

#### Update Data Source Access
```
PUT /api/rest_j/v1/basedata-manager/datasource-access
```

Request Body:
```json
{
  "id": 1,
  "tokenId": 1,
  "serviceId": 2
}
```

Response:
```json
{
  "method": "",
  "status": 0,
  "message": "",
  "data": {
    "result": true
  }
}
```

### Data Source Type Key Management APIs

#### List Data Source Type Keys
```
GET /api/rest_j/v1/basedata-manager/datasource-type-key
```

Parameters:
- `searchName`: Search name - optional
- `dataSourceTypeId`: Data source type ID - optional
- `currentPage`: Current page - optional, default 1
- `pageSize`: Page size - optional, default 10

Response:
```json
{
  "method": "",
  "status": 0,
  "message": "",
  "data": {
    "list": {
      "total": 1,
      "list": [
        {
          "id": 1,
          "dataSourceTypeId": 1,
          "key": "host",
          "name": "Host",
          "nameEn": "Host",
          "defaultValue": "",
          "valueType": "String",
          "scope": "ENV",
          "require": 1,
          "description": "Host IP",
          "descriptionEn": "Host IP",
          "valueRegex": "",
          "refId": null,
          "refValue": null,
          "dataSource": null,
          "updateTime": "2023-01-01 12:00:00",
          "createTime": "2023-01-01 12:00:00"
        }
      ],
      "pageNum": 1,
      "pageSize": 10,
      "size": 1,
      "startRow": 1,
      "endRow": 1,
      "pages": 1,
      "prePage": 0,
      "nextPage": 0,
      "isFirstPage": true,
      "isLastPage": true,
      "hasPreviousPage": false,
      "hasNextPage": false,
      "navigatePages": 8,
      "navigatepageNums": [
        1
      ]
    }
  }
}
```

#### Get Data Source Type Key
```
GET /api/rest_j/v1/basedata-manager/datasource-type-key/{id}
```

Parameters:
- `id`: Data source type key ID (required)

Response:
```json
{
  "method": "",
  "status": 0,
  "message": "",
  "data": {
    "item": {
      "id": 1,
      "dataSourceTypeId": 1,
      "key": "host",
      "name": "Host",
      "nameEn": "Host",
      "defaultValue": "",
      "valueType": "String",
      "scope": "ENV",
      "require": 1,
      "description": "Host IP",
      "descriptionEn": "Host IP",
      "valueRegex": "",
      "refId": null,
      "refValue": null,
      "dataSource": null,
      "updateTime": "2023-01-01 12:00:00",
      "createTime": "2023-01-01 12:00:00"
    }
  }
}
```

#### Add Data Source Type Key
```
POST /api/rest_j/v1/basedata-manager/datasource-type-key
```

Request Body:
```json
{
  "dataSourceTypeId": 1,
  "key": "port",
  "name": "Port",
  "nameEn": "Port",
  "defaultValue": "3306",
  "valueType": "String",
  "scope": "ENV",
  "require": 1,
  "description": "Port number",
  "descriptionEn": "Port number",
  "valueRegex": ""
}
```

Response:
```json
{
  "method": "",
  "status": 0,
  "message": "",
  "data": {
    "result": true
  }
}
```

#### Remove Data Source Type Key
```
DELETE /api/rest_j/v1/basedata-manager/datasource-type-key/{id}
```

Parameters:
- `id`: Data source type key ID (required)

Response:
```json
{
  "method": "",
  "status": 0,
  "message": "",
  "data": {
    "result": true
  }
}
```

#### Update Data Source Type Key
```
PUT /api/rest_j/v1/basedata-manager/datasource-type-key
```

Request Body:
```json
{
  "id": 1,
  "dataSourceTypeId": 1,
  "key": "host",
  "name": "Host",
  "nameEn": "Host",
  "defaultValue": "",
  "valueType": "String",
  "scope": "ENV",
  "require": 1,
  "description": "Updated Host IP",
  "descriptionEn": "Updated Host IP",
  "valueRegex": ""
}
```

Response:
```json
{
  "method": "",
  "status": 0,
  "message": "",
  "data": {
    "result": true
  }
}
```

## Database Table Structures

The DataSource Service uses the following database tables from linkis_ddl.sql:

### Data Source Table
```sql
CREATE TABLE `linkis_ps_dm_datasource`
(
    `id`                   int(11)                       NOT NULL AUTO_INCREMENT,
    `datasource_name`      varchar(255) COLLATE utf8_bin NOT NULL,
    `datasource_desc`      varchar(255) COLLATE utf8_bin      DEFAULT NULL,
    `datasource_type_id`   int(11)                       NOT NULL,
    `create_identify`      varchar(255) COLLATE utf8_bin      DEFAULT NULL,
    `create_system`        varchar(255) COLLATE utf8_bin      DEFAULT NULL,
    `parameter`            varchar(2048) COLLATE utf8_bin NULL DEFAULT NULL,
    `create_time`          datetime                      NULL DEFAULT CURRENT_TIMESTAMP,
    `modify_time`          datetime                      NULL DEFAULT CURRENT_TIMESTAMP,
    `create_user`          varchar(255) COLLATE utf8_bin      DEFAULT NULL,
    `modify_user`          varchar(255) COLLATE utf8_bin      DEFAULT NULL,
    `labels`               varchar(255) COLLATE utf8_bin      DEFAULT NULL,
    `version_id`           int(11)                            DEFAULT NULL COMMENT 'current version id',
    `expire`               tinyint(1)                         DEFAULT 0,
    `published_version_id` int(11)                            DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uniq_datasource_name` (`datasource_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
```

### Data Source Environment Table
```sql
CREATE TABLE `linkis_ps_dm_datasource_env`
(
    `id`                 int(11)                       NOT NULL AUTO_INCREMENT,
    `env_name`           varchar(32) COLLATE utf8_bin  NOT NULL,
    `env_desc`           varchar(255) COLLATE utf8_bin          DEFAULT NULL,
    `datasource_type_id` int(11)                       NOT NULL,
    `parameter`          varchar(2048) COLLATE utf8_bin          DEFAULT NULL,
    `create_time`        datetime                      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_user`        varchar(255) COLLATE utf8_bin NULL     DEFAULT NULL,
    `modify_time`        datetime                      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `modify_user`        varchar(255) COLLATE utf8_bin NULL     DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_env_name` (`env_name`),
    UNIQUE INDEX `uniq_name_dtid` (`env_name`, `datasource_type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
```

### Data Source Type Table
```sql
CREATE TABLE `linkis_ps_dm_datasource_type`
(
    `id`          int(11)                      NOT NULL AUTO_INCREMENT,
    `name`        varchar(32) COLLATE utf8_bin NOT NULL,
    `description` varchar(255) COLLATE utf8_bin DEFAULT NULL,
    `option`      varchar(32) COLLATE utf8_bin  DEFAULT NULL,
    `classifier`  varchar(32) COLLATE utf8_bin NOT NULL,
    `icon`        varchar(255) COLLATE utf8_bin DEFAULT NULL,
    `layers`      int(3)                       NOT NULL,
    `description_en` varchar(255) DEFAULT NULL COMMENT 'english description',
    `option_en` varchar(32) DEFAULT NULL COMMENT 'english option',
    `classifier_en` varchar(32) DEFAULT NULL COMMENT 'english classifier',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uniq_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
```

### Data Source Type Key Table
```sql
CREATE TABLE `linkis_ps_dm_datasource_type_key`
(
    `id`                  int(11)                       NOT NULL AUTO_INCREMENT,
    `data_source_type_id` int(11)                       NOT NULL,
    `key`                 varchar(32) COLLATE utf8_bin  NOT NULL,
    `name`                varchar(32) COLLATE utf8_bin  NOT NULL,
    `name_en`             varchar(32) COLLATE utf8_bin  NULL     DEFAULT NULL,
    `default_value`       varchar(50) COLLATE utf8_bin  NULL     DEFAULT NULL,
    `value_type`          varchar(50) COLLATE utf8_bin  NOT NULL,
    `scope`               varchar(50) COLLATE utf8_bin  NULL     DEFAULT NULL,
    `require`             tinyint(1)                    NULL     DEFAULT 0,
    `description`         varchar(200) COLLATE utf8_bin NULL     DEFAULT NULL,
    `description_en`      varchar(200) COLLATE utf8_bin NULL     DEFAULT NULL,
    `value_regex`         varchar(200) COLLATE utf8_bin NULL     DEFAULT NULL,
    `ref_id`              bigint(20)                    NULL     DEFAULT NULL,
    `ref_value`           varchar(50) COLLATE utf8_bin  NULL     DEFAULT NULL,
    `data_source`         varchar(200) COLLATE utf8_bin NULL     DEFAULT NULL,
    `update_time`         datetime                      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_time`         datetime                      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_dstid_key` (`data_source_type_id`, `key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
```

### Data Source Version Table
```sql
CREATE TABLE `linkis_ps_dm_datasource_version`
(
    `version_id`    int(11)                        NOT NULL AUTO_INCREMENT,
    `datasource_id` int(11)                        NOT NULL,
    `parameter`     varchar(2048) COLLATE utf8_bin NULL DEFAULT NULL,
    `comment`       varchar(255) COLLATE utf8_bin  NULL DEFAULT NULL,
    `create_time`   datetime(0)                    NULL DEFAULT CURRENT_TIMESTAMP,
    `create_user`   varchar(255) COLLATE utf8_bin  NULL DEFAULT NULL,
    PRIMARY KEY `uniq_vid_did` (`version_id`, `datasource_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
```

### Data Source Access Table
```sql
CREATE TABLE `linkis_ps_dm_datasource_access`
(
    `id`          int(11) NOT NULL AUTO_INCREMENT,
    `token_id`    int(11) NOT NULL,
    `service_id`  int(11) NOT NULL,
    `access_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
```

## RPC Methods

The DataSource Service provides several RPC methods for data source management:

### Data Source RPCs

#### createDataSource
Creates a new data source:
```java
Long createDataSource(DataSourceCreationRequest request)
```

#### getDataSource
Retrieves a data source:
```java
DataSourceInfo getDataSource(Long dataSourceId)
```

#### updateDataSource
Updates a data source:
```java
void updateDataSource(DataSourceUpdateRequest request)
```

#### deleteDataSource
Deletes a data source:
```java
void deleteDataSource(Long dataSourceId)
```

#### listDataSources
Lists data sources with filtering:
```java
List<DataSourceInfo> listDataSources(DataSourceQueryRequest request)
```

### Metadata RPCs

#### getMetadata
Retrieves metadata for a data source:
```java
DataSourceMetadata getMetadata(DataSourceMetadataRequest request)
```

#### testConnection
Tests connection to a data source:
```java
ConnectionTestResult testConnection(Long dataSourceId)
```

#### getTableSchema
Retrieves table schema information:
```java
TableSchema getTableSchema(Long dataSourceId, String database, String table)
```

#### getDatabaseList
Retrieves list of databases:
```java
List<String> getDatabaseList(Long dataSourceId)
```

#### getTableList
Retrieves list of tables in a database:
```java
List<String> getTableList(Long dataSourceId, String database)
```

### Environment RPCs

#### createEnvironment
Creates a new environment:
```java
Long createEnvironment(EnvironmentCreationRequest request)
```

#### getEnvironment
Retrieves an environment:
```java
EnvironmentInfo getEnvironment(Long environmentId)
```

#### updateEnvironment
Updates an environment:
```java
void updateEnvironment(EnvironmentUpdateRequest request)
```

#### deleteEnvironment
Deletes an environment:
```java
void deleteEnvironment(Long environmentId)
```

### Access RPCs

#### grantAccess
Grants access to a data source:
```java
void grantAccess(DataSourceAccessRequest request)
```

#### revokeAccess
Revokes access from a data source:
```java
void revokeAccess(DataSourceAccessRequest request)
```

#### checkAccess
Checks if a user has access to a data source:
```java
boolean checkAccess(String user, Long dataSourceId)
```

## Dependencies

- linkis-datasource-manager
- linkis-metadata
- linkis-rpc
- linkis-protocol

## Interface Classes and MyBatis XML Files

### Interface Classes
- DataSourceCoreRestfulApi: `linkis-public-enhancements/linkis-datasource/linkis-datasource-manager/server/src/main/java/org/apache/linkis/datasourcemanager/core/restful/DataSourceCoreRestfulApi.java`
- DataSourceAdminRestfulApi: `linkis-public-enhancements/linkis-datasource/linkis-datasource-manager/server/src/main/java/org/apache/linkis/datasourcemanager/core/restful/DataSourceAdminRestfulApi.java`
- DataSourceRestfulApi: `linkis-public-enhancements/linkis-datasource/linkis-metadata/src/main/java/org/apache/linkis/metadata/restful/api/DataSourceRestfulApi.java`
- DatasourceTypeRestfulApi: `linkis-public-enhancements/linkis-pes-publicservice/src/main/java/org/apache/linkis/basedatamanager/server/restful/DatasourceTypeRestfulApi.java`
- DatasourceAccessRestfulApi: `linkis-public-enhancements/linkis-pes-publicservice/src/main/java/org/apache/linkis/basedatamanager/server/restful/DatasourceAccessRestfulApi.java`
- DatasourceTypeKeyRestfulApi: `linkis-public-enhancements/linkis-pes-publicservice/src/main/java/org/apache/linkis/basedatamanager/server/restful/DatasourceTypeKeyRestfulApi.java`

### MyBatis XML Files
- DataSouceMapper: `linkis-public-enhancements/linkis-datasource/linkis-datasource-manager/server/src/main/resources/mapper/mysql/DataSouceMapper.xml`
- DataSourceEnvMapper: `linkis-public-enhancements/linkis-datasource/linkis-datasource-manager/server/src/main/resources/mapper/mysql/DataSourceEnvMapper.xml`
- DataSourceParamKeyMapper: `linkis-public-enhancements/linkis-datasource/linkis-datasource-manager/server/src/main/resources/mapper/mysql/DataSourceParamKeyMapper.xml`
- DataSourceTypeMapper: `linkis-public-enhancements/linkis-datasource/linkis-datasource-manager/server/src/main/resources/mapper/mysql/DataSourceTypeMapper.xml`
- DataSourceVersionMapper: `linkis-public-enhancements/linkis-datasource/linkis-datasource-manager/server/src/main/resources/mapper/mysql/DataSourceVersionMapper.xml`
- DataSourceAccessMapper: `linkis-public-enhancements/linkis-pes-publicservice/src/main/resources/mapper/DataSourceAccessMapper.xml`
- DatasourceTypeMapper: `linkis-public-enhancements/linkis-pes-publicservice/src/main/resources/mapper/DatasourceTypeMapper.xml`
- DatasourceTypeKeyMapper: `linkis-public-enhancements/linkis-pes-publicservice/src/main/resources/mapper/DatasourceTypeKeyMapper.xml`