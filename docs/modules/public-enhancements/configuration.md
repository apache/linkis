# Configuration Service

The Configuration Service provides centralized configuration management for the Linkis system.

## Overview

This service manages configuration properties for all Linkis components, providing a unified interface for configuration retrieval, updates, and validation.

## Key Components

### Core Classes
- `LinkisConfigurationApp` - Main application class
- Configuration management
- Configuration validation
- Template management

### Features
- Global configuration management
- User-specific configuration
- Configuration validation
- Template-based configuration
- Engine-type specific configuration

## API Interfaces

### Configuration Retrieval APIs

#### Get Full Configuration Trees
```
GET /api/rest_j/v1/configuration/getFullTreesByAppName
```

Parameters:
- `engineType`: Engine type (e.g., spark, hive) - optional
- `version`: Engine version - optional
- `creator`: Creator application - optional

Response:
```json
{
  "method": "/api/rest_j/v1/configuration/getFullTreesByAppName",
  "status": 0,
  "message": "success",
  "data": {
    "fullTree": [
      {
        "name": "JVM Configuration",
        "description": "JVM configuration for engine",
        "settings": [
          {
            "key": "wds.linkis.engineconn.java.driver.memory",
            "value": "2g",
            "defaultValue": "1g",
            "description": "Memory size of driver JVM process",
            "validateType": "Regex",
            "validateRange": "^[0-9]+(\\.?[0-9]*)([gGmMkK])?$",
            "level": 1,
            "hidden": false,
            "advanced": false
          }
        ]
      }
    ]
  }
}
```

#### Get Configuration Category
```
GET /api/rest_j/v1/configuration/getCategory
```

Response:
```json
{
  "method": "/api/rest_j/v1/configuration/getCategory",
  "status": 0,
  "message": "success",
  "data": {
    "Category": [
      {
        "categoryId": 1,
        "categoryName": "Engine Resource",
        "description": "Engine resource configuration"
      }
    ]
  }
}
```

#### Get Configuration Item List
```
GET /api/rest_j/v1/configuration/getItemList
```

Parameters:
- `engineType`: Engine type (e.g., spark, hive)

Response:
```json
{
  "method": "/api/rest_j/v1/configuration/getItemList",
  "status": 0,
  "message": "success",
  "data": {
    "itemList": [
      {
        "key": "spark.executor.instances",
        "name": "Executor Instances",
        "description": "Number of executor instances",
        "engineType": "spark",
        "validateType": "NumInterval",
        "validateRange": "[1,20]",
        "boundaryType": 3,
        "defaultValue": "1",
        "require": 0
      }
    ]
  }
}
```

#### List All Engine Types
```
GET /api/rest_j/v1/configuration/engineType
```

Response:
```json
{
  "method": "/api/rest_j/v1/configuration/engineType",
  "status": 0,
  "message": "success",
  "data": {
    "engineType": ["spark", "hive", "python"]
  }
}
```

#### Get Key Value
```
GET /api/rest_j/v1/configuration/keyvalue
```

Parameters:
- `engineType`: Engine type - default "*"
- `version`: Engine version - default "*"
- `creator`: Creator application - default "*"
- `configKey`: Configuration key (required)

Response:
```json
{
  "method": "/api/rest_j/v1/configuration/keyvalue",
  "status": 0,
  "message": "success",
  "data": {
    "configValues": [
      {
        "id": 1,
        "configKeyId": 1,
        "configValue": "2g",
        "configLabelId": 1
      }
    ]
  }
}
```

#### Get Base Key Value
```
GET /api/rest_j/v1/configuration/baseKeyValue
```

Parameters:
- `engineType`: Engine type - optional
- `key`: Configuration key - optional
- `pageNow`: Page number - default 1
- `pageSize`: Page size - default 20

Response:
```json
{
  "method": "/api/rest_j/v1/configuration/baseKeyValue",
  "status": 0,
  "message": "success",
  "data": {
    "configKeyList": [
      {
        "id": 1,
        "key": "spark.executor.instances",
        "description": "Number of executor instances",
        "name": "Executor Instances",
        "defaultValue": "1",
        "validateType": "NumInterval",
        "validateRange": "[1,20]",
        "engineConnType": "spark",
        "isHidden": 0,
        "isAdvanced": 0,
        "level": 1,
        "treeName": "Spark Configuration",
        "boundaryType": 3,
        "enDescription": "Number of executor instances",
        "enName": "Executor Instances",
        "enTreeName": "Spark Configuration",
        "templateRequired": 0
      }
    ],
    "totalPage": 10
  }
}
```

#### Get User Key Value
```
GET /api/rest_j/v1/configuration/userKeyValue
```

Parameters:
- `engineType`: Engine type - optional
- `key`: Configuration key - optional
- `creator`: Creator application - optional
- `user`: Username - optional
- `pageNow`: Page number - default 1
- `pageSize`: Page size - default 20

Response:
```json
{
  "method": "/api/rest_j/v1/configuration/userKeyValue",
  "status": 0,
  "message": "success",
  "data": {
    "configValueList": [
      {
        "id": 1,
        "configKeyId": 1,
        "configValue": "2",
        "configLabelId": 1,
        "updateTime": "2023-01-01 12:00:00",
        "createTime": "2023-01-01 12:00:00",
        "engineType": "spark",
        "key": "spark.executor.instances",
        "creator": "IDE",
        "user": "testuser"
      }
    ],
    "totalPage": 1
  }
}
```

### Configuration Management APIs

#### Create First Category
```
POST /api/rest_j/v1/configuration/createFirstCategory
```

Request Body:
```json
{
  "categoryName": "Engine Resource",
  "description": "Engine resource configuration"
}
```

Response:
```json
{
  "method": "/api/rest_j/v1/configuration/createFirstCategory",
  "status": 0,
  "message": "success"
}
```

#### Delete Category
```
POST /api/rest_j/v1/configuration/deleteCategory
```

Request Body:
```json
{
  "categoryId": 1
}
```

Response:
```json
{
  "method": "/api/rest_j/v1/configuration/deleteCategory",
  "status": 0,
  "message": "success"
}
```

#### Create Second Category
```
POST /api/rest_j/v1/configuration/createSecondCategory
```

Request Body:
```json
{
  "categoryId": 1,
  "engineType": "spark",
  "version": "2.4.3",
  "description": "Spark 2.4.3 configuration"
}
```

Response:
```json
{
  "method": "/api/rest_j/v1/configuration/createSecondCategory",
  "status": 0,
  "message": "success"
}
```

#### Update Category Info
```
POST /api/rest_j/v1/configuration/updateCategoryInfo
```

Request Body:
```json
{
  "categoryId": 1,
  "description": "Updated description"
}
```

Response:
```json
{
  "method": "/api/rest_j/v1/configuration/updateCategoryInfo",
  "status": 0,
  "message": "success"
}
```

#### Save Full Tree
```
POST /api/rest_j/v1/configuration/saveFullTree
```

Request Body:
```json
{
  "creator": "IDE",
  "engineType": "spark-2.4.3",
  "fullTree": [
    {
      "name": "JVM Configuration",
      "description": "JVM configuration for engine",
      "settings": [
        {
          "key": "wds.linkis.engineconn.java.driver.memory",
          "value": "2g",
          "defaultValue": "1g",
          "description": "Memory size of driver JVM process",
          "validateType": "Regex",
          "validateRange": "^[0-9]+(\\.?[0-9]*)([gGmMkK])?$",
          "level": 1,
          "hidden": false,
          "advanced": false
        }
      ]
    }
  ]
}
```

Response:
```json
{
  "method": "/api/rest_j/v1/configuration/saveFullTree",
  "status": 0,
  "message": "success"
}
```

#### Save Key Value
```
POST /api/rest_j/v1/configuration/keyvalue
```

Request Body:
```json
{
  "engineType": "spark",
  "version": "2.4.3",
  "creator": "IDE",
  "configKey": "spark.executor.instances",
  "configValue": "2",
  "user": "testuser"
}
```

Response:
```json
{
  "method": "/api/rest_j/v1/configuration/keyvalue",
  "status": 0,
  "message": "success",
  "data": {
    "configValue": {
      "id": 1,
      "configKeyId": 1,
      "configValue": "2",
      "configLabelId": 1
    }
  }
}
```

#### Delete Key Value
```
DELETE /api/rest_j/v1/configuration/keyvalue
```

Request Body:
```json
{
  "engineType": "spark",
  "version": "2.4.3",
  "creator": "IDE",
  "configKey": "spark.executor.instances"
}
```

Response:
```json
{
  "method": "/api/rest_j/v1/configuration/keyvalue",
  "status": 0,
  "message": "success",
  "data": {
    "configValues": [
      {
        "id": 1,
        "configKeyId": 1,
        "configValue": "2",
        "configLabelId": 1
      }
    ]
  }
}
```

#### Save Base Key Value
```
POST /api/rest_j/v1/configuration/baseKeyValue
```

Request Body:
```json
{
  "key": "spark.executor.instances",
  "name": "Executor Instances",
  "description": "Number of executor instances",
  "defaultValue": "1",
  "validateType": "NumInterval",
  "validateRange": "[1,20]",
  "boundaryType": 3,
  "treeName": "Spark Configuration",
  "engineType": "spark",
  "enDescription": "Number of executor instances",
  "enName": "Executor Instances",
  "enTreeName": "Spark Configuration",
  "templateRequired": 0
}
```

Response:
```json
{
  "method": "/api/rest_j/v1/configuration/baseKeyValue",
  "status": 0,
  "message": "success",
  "data": {
    "configKey": {
      "id": 1,
      "key": "spark.executor.instances",
      "description": "Number of executor instances",
      "name": "Executor Instances",
      "defaultValue": "1",
      "validateType": "NumInterval",
      "validateRange": "[1,20]",
      "engineConnType": "spark",
      "isHidden": 0,
      "isAdvanced": 0,
      "level": 1,
      "treeName": "Spark Configuration",
      "boundaryType": 3,
      "enDescription": "Number of executor instances",
      "enName": "Executor Instances",
      "enTreeName": "Spark Configuration",
      "templateRequired": 0
    }
  }
}
```

#### Delete Base Key Value
```
DELETE /api/rest_j/v1/configuration/baseKeyValue
```

Parameters:
- `id`: Configuration key ID (required)

Response:
```json
{
  "method": "/api/rest_j/v1/configuration/baseKeyValue",
  "status": 0,
  "message": "success"
}
```

### Template Management APIs

#### Update Key Mapping
```
POST /api/rest_j/v1/configuration/template/updateKeyMapping
```

Request Body:
```json
{
  "templateUid": "template-uuid",
  "templateName": "Spark Template",
  "engineType": "spark",
  "operator": "admin",
  "isFullMode": true,
  "itemList": [
    {
      "keyId": 1,
      "maxValue": "10",
      "minValue": "1"
    }
  ]
}
```

Response:
```json
{
  "method": "/api/rest_j/v1/configuration/template/updateKeyMapping",
  "status": 0,
  "message": "success"
}
```

#### Query Key Info List
```
POST /api/rest_j/v1/configuration/template/queryKeyInfoList
```

Request Body:
```json
{
  "templateUidList": ["template-uuid-1", "template-uuid-2"]
}
```

Response:
```json
{
  "method": "/api/rest_j/v1/configuration/template/queryKeyInfoList",
  "status": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "templateName": "Spark Template",
        "engineType": "spark",
        "configKey": "spark.executor.instances",
        "maxValue": "10",
        "minValue": "1"
      }
    ]
  }
}
```

#### Apply Configuration Template
```
POST /api/rest_j/v1/configuration/template/apply
```

Request Body:
```json
{
  "templateUid": "template-uuid",
  "application": "IDE",
  "engineType": "spark",
  "engineVersion": "2.4.3",
  "operator": "admin",
  "userList": ["user1", "user2"]
}
```

Response:
```json
{
  "method": "/api/rest_j/v1/configuration/template/apply",
  "status": 0,
  "message": "success",
  "data": {
    "success": true,
    "failedUsers": []
  }
}
```

#### Encrypt Datasource Password
```
GET /api/rest_j/v1/configuration/template/encrypt
```

Parameters:
- `isEncrypt`: Encrypt flag - optional

Response:
```json
{
  "method": "/api/rest_j/v1/configuration/template/encrypt",
  "status": 0,
  "message": "success"
}
```

### Tenant Configuration APIs

#### Create Tenant
```
POST /api/rest_j/v1/configuration/tenant-mapping/create-tenant
```

Request Body:
```json
{
  "user": "testuser",
  "creator": "IDE",
  "tenantValue": "tenant1",
  "desc": "Test tenant",
  "bussinessUser": "admin"
}
```

Response:
```json
{
  "method": "/api/rest_j/v1/configuration/tenant-mapping/create-tenant",
  "status": 0,
  "message": "success"
}
```

#### Update Tenant
```
POST /api/rest_j/v1/configuration/tenant-mapping/update-tenant
```

Request Body:
```json
{
  "id": 1,
  "user": "testuser",
  "creator": "IDE",
  "tenantValue": "tenant1-updated",
  "desc": "Updated tenant",
  "bussinessUser": "admin"
}
```

Response:
```json
{
  "method": "/api/rest_j/v1/configuration/tenant-mapping/update-tenant",
  "status": 0,
  "message": "success"
}
```

#### Delete Tenant
```
GET /api/rest_j/v1/configuration/tenant-mapping/delete-tenant
```

Parameters:
- `id`: Tenant ID (required)

Response:
```json
{
  "method": "/api/rest_j/v1/configuration/tenant-mapping/delete-tenant",
  "status": 0,
  "message": "success"
}
```

#### Query Tenant List
```
GET /api/rest_j/v1/configuration/tenant-mapping/query-tenant-list
```

Parameters:
- `user`: Username - optional
- `creator`: Creator application - optional
- `tenantValue`: Tenant value - optional
- `pageNow`: Page number - default 1
- `pageSize`: Page size - default 20

Response:
```json
{
  "method": "/api/rest_j/v1/configuration/tenant-mapping/query-tenant-list",
  "status": 0,
  "message": "success",
  "data": {
    "tenantList": [
      {
        "id": 1,
        "user": "testuser",
        "creator": "IDE",
        "tenantValue": "tenant1",
        "desc": "Test tenant",
        "bussinessUser": "admin",
        "createTime": "2023-01-01 12:00:00",
        "updateTime": "2023-01-01 12:00:00"
      }
    ],
    "totalPage": 1
  }
}
```

#### Check User Creator
```
GET /api/rest_j/v1/configuration/tenant-mapping/check-user-creator
```

Parameters:
- `user`: Username (required)
- `creator`: Creator application (required)

Response:
```json
{
  "method": "/api/rest_j/v1/configuration/tenant-mapping/check-user-creator",
  "status": 0,
  "message": "success",
  "data": {
    "exist": true
  }
}
```

#### Save Department Tenant
```
POST /api/rest_j/v1/configuration/tenant-mapping/save-department-tenant
```

Request Body:
```json
{
  "creator": "IDE",
  "department": "Engineering",
  "departmentId": "dept1",
  "tenantValue": "tenant1",
  "createBy": "admin"
}
```

Response:
```json
{
  "method": "/api/rest_j/v1/configuration/tenant-mapping/save-department-tenant",
  "status": 0,
  "message": "success"
}
```

#### Query Department Tenant
```
GET /api/rest_j/v1/configuration/tenant-mapping/query-department-tenant
```

Parameters:
- `departmentId`: Department ID - optional
- `department`: Department name - optional
- `creator`: Creator application - optional
- `tenantValue`: Tenant value - optional
- `pageNow`: Page number - default 1
- `pageSize`: Page size - default 20

Response:
```json
{
  "method": "/api/rest_j/v1/configuration/tenant-mapping/query-department-tenant",
  "status": 0,
  "message": "success",
  "data": {
    "tenantList": [
      {
        "id": 1,
        "creator": "IDE",
        "department": "Engineering",
        "departmentId": "dept1",
        "tenantValue": "tenant1",
        "createBy": "admin",
        "isValid": "Y",
        "createTime": "2023-01-01 12:00:00",
        "updateTime": "2023-01-01 12:00:00"
      }
    ],
    "totalPage": 1
  }
}
```

#### Delete Department Tenant
```
GET /api/rest_j/v1/configuration/tenant-mapping/delete-department-tenant
```

Parameters:
- `id`: Department tenant ID (required)

Response:
```json
{
  "method": "/api/rest_j/v1/configuration/tenant-mapping/delete-department-tenant",
  "status": 0,
  "message": "success"
}
```

#### Query Department List
```
GET /api/rest_j/v1/configuration/tenant-mapping/query-department
```

Response:
```json
{
  "method": "/api/rest_j/v1/configuration/tenant-mapping/query-department",
  "status": 0,
  "message": "success",
  "data": {
    "departmentList": ["Engineering", "Marketing", "Sales"]
  }
}
```

#### Query User Department
```
GET /api/rest_j/v1/configuration/tenant-mapping/query-user-department
```

Parameters:
- `username`: Username (required)

Response:
```json
{
  "method": "/api/rest_j/v1/configuration/tenant-mapping/query-user-department",
  "status": 0,
  "message": "success",
  "data": {
    "department": "Engineering"
  }
}
```

### User IP Configuration APIs

#### Create User IP
```
POST /api/rest_j/v1/configuration/user-ip-mapping/create-user-ip
```

Request Body:
```json
{
  "user": "testuser",
  "creator": "IDE",
  "ipList": "192.168.1.1,192.168.1.2",
  "desc": "Allowed IPs for testuser",
  "bussinessUser": "admin"
}
```

Response:
```json
{
  "method": "/api/rest_j/v1/configuration/user-ip-mapping/create-user-ip",
  "status": 0,
  "message": "success"
}
```

#### Update User IP
```
POST /api/rest_j/v1/configuration/user-ip-mapping/update-user-ip
```

Request Body:
```json
{
  "id": 1,
  "user": "testuser",
  "creator": "IDE",
  "ipList": "192.168.1.1,192.168.1.2,192.168.1.3",
  "desc": "Updated allowed IPs for testuser",
  "bussinessUser": "admin"
}
```

Response:
```json
{
  "method": "/api/rest_j/v1/configuration/user-ip-mapping/update-user-ip",
  "status": 0,
  "message": "success"
}
```

#### Delete User IP
```
GET /api/rest_j/v1/configuration/user-ip-mapping/delete-user-ip
```

Parameters:
- `id`: User IP ID (required)

Response:
```json
{
  "method": "/api/rest_j/v1/configuration/user-ip-mapping/delete-user-ip",
  "status": 0,
  "message": "success"
}
```

#### Query User IP List
```
GET /api/rest_j/v1/configuration/user-ip-mapping/query-user-ip-list
```

Parameters:
- `user`: Username - optional
- `creator`: Creator application - optional
- `pageNow`: Page number (required)
- `pageSize`: Page size (required)

Response:
```json
{
  "method": "/api/rest_j/v1/configuration/user-ip-mapping/query-user-ip-list",
  "status": 0,
  "message": "success",
  "data": {
    "userIpList": [
      {
        "id": 1,
        "user": "testuser",
        "creator": "IDE",
        "ipList": "192.168.1.1,192.168.1.2",
        "desc": "Allowed IPs for testuser",
        "bussinessUser": "admin",
        "createTime": "2023-01-01 12:00:00",
        "updateTime": "2023-01-01 12:00:00"
      }
    ],
    "totalPage": 1
  }
}
```

#### Check User Creator
```
GET /api/rest_j/v1/configuration/user-ip-mapping/check-user-creator
```

Parameters:
- `user`: Username (required)
- `creator`: Creator application (required)

Response:
```json
{
  "method": "/api/rest_j/v1/configuration/user-ip-mapping/check-user-creator",
  "status": 0,
  "message": "success",
  "data": {
    "exist": true
  }
}
```

## Database Table Structures

The Configuration Service uses the following database tables from linkis_ddl.sql:

### Configuration Config Key Table
```sql
CREATE TABLE `linkis_ps_configuration_config_key`(
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `key` varchar(50) DEFAULT NULL COMMENT 'Set key, e.g. spark.executor.instances',
  `description` varchar(200) DEFAULT NULL,
  `name` varchar(50) DEFAULT NULL,
  `default_value` varchar(200) DEFAULT NULL COMMENT 'Adopted when user does not set key',
  `validate_type` varchar(50) DEFAULT NULL COMMENT 'Validate type, one of the following: None, NumInterval, FloatInterval, Include, Regex, OPF, Custom Rules',
  `validate_range` varchar(150) DEFAULT NULL COMMENT 'Validate range',
  `engine_conn_type` varchar(50) DEFAULT '' COMMENT 'engine type,such as spark,hive etc',
  `is_hidden` tinyint(1) DEFAULT NULL COMMENT 'Whether it is hidden from user. If set to 1(true), then user cannot modify, however, it could still be used in back-end',
  `is_advanced` tinyint(1) DEFAULT NULL COMMENT 'Whether it is an advanced parameter. If set to 1(true), parameters would be displayed only when user choose to do so',
  `level` tinyint(1) DEFAULT NULL COMMENT 'Basis for displaying sorting in the front-end. Higher the level is, higher the rank the parameter gets',
  `treeName` varchar(20) DEFAULT NULL COMMENT 'Reserved field, representing the subdirectory of engineType',
  `boundary_type` TINYINT(2) NULL DEFAULT '0' COMMENT '0  none/ 1 with mix /2 with max / 3 min and max both',
  `en_description` varchar(200) DEFAULT NULL COMMENT 'english description',
  `en_name` varchar(100) DEFAULT NULL COMMENT 'english name',
  `en_treeName` varchar(100) DEFAULT NULL COMMENT 'english treeName',
  `template_required` tinyint(1) DEFAULT 0 COMMENT 'template required 0 none / 1 must',
  UNIQUE INDEX `uniq_key_ectype` (`key`,`engine_conn_type`),
  PRIMARY KEY  (`id`)
);
```

### Configuration Key Engine Relation Table
```sql
CREATE TABLE `linkis_ps_configuration_key_engine_relation`(
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `config_key_id` bigint(20) NOT NULL COMMENT 'config key id',
  `engine_type_label_id` bigint(20) NOT NULL COMMENT 'engine label id',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uniq_kid_lid` (`config_key_id`, `engine_type_label_id`)
);
```

### Configuration Config Value Table
```sql
CREATE TABLE `linkis_ps_configuration_config_value`(
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `config_key_id` bigint(20),
  `config_value` varchar(500),
  `config_label_id`int(20),
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uniq_kid_lid` (`config_key_id`, `config_label_id`)
);
```

### Configuration Category Table
```sql
CREATE TABLE `linkis_ps_configuration_category` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `label_id` int(20) NOT NULL,
  `level` int(20) NOT NULL,
  `description` varchar(200),
  `tag` varchar(200),
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uniq_label_id` (`label_id`)
);
```

### Tenant Label Config Table
```sql
CREATE TABLE `linkis_cg_tenant_label_config` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `user` varchar(50) COLLATE utf8_bin NOT NULL,
  `creator` varchar(50) COLLATE utf8_bin NOT NULL,
  `tenant_value` varchar(128) COLLATE utf8_bin NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `desc` varchar(100) COLLATE utf8_bin NOT NULL,
  `bussiness_user` varchar(50) COLLATE utf8_bin NOT NULL,
  `is_valid` varchar(1) COLLATE utf8_bin NOT NULL DEFAULT 'Y' COMMENT 'is valid',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_user_creator` (`user`,`creator`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
```

### User IP Config Table
```sql
CREATE TABLE `linkis_cg_user_ip_config` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `user` varchar(50) COLLATE utf8_bin NOT NULL,
  `creator` varchar(50) COLLATE utf8_bin NOT NULL,
  `ip_list` text COLLATE utf8_bin NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `desc` varchar(100) COLLATE utf8_bin NOT NULL,
  `bussiness_user` varchar(50) COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_user_creator` (`user`,`creator`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
```

### Tenant Department Config Table
```sql
CREATE TABLE `linkis_cg_tenant_department_config` (
  `id` int(20) NOT NULL AUTO_INCREMENT  COMMENT 'ID',
  `creator` varchar(50) COLLATE utf8_bin NOT NULL  COMMENT '应用',
  `department` varchar(64) COLLATE utf8_bin NOT NULL  COMMENT '部门名称',
  `department_id` varchar(16) COLLATE utf8_bin NOT NULL COMMENT '部门ID',
  `tenant_value` varchar(128) COLLATE utf8_bin NOT NULL  COMMENT '部门租户标签',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '更新时间',
  `create_by` varchar(50) COLLATE utf8_bin NOT NULL  COMMENT '创建用户',
  `is_valid` varchar(1) COLLATE utf8_bin NOT NULL DEFAULT 'Y' COMMENT '是否有效',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_creator_department` (`creator`,`department`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
```

### Configuration Template Config Key Table
```sql
CREATE TABLE `linkis_ps_configuration_template_config_key` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`template_name` VARCHAR(200) NOT NULL COMMENT '配置模板名称 冗余存储',
	`template_uuid` VARCHAR(36) NOT NULL COMMENT 'uuid  第三方侧记录的模板id',
	`key_id` BIGINT(20) NOT NULL COMMENT 'id of linkis_ps_configuration_config_key',
	`config_value` VARCHAR(200) NULL DEFAULT NULL COMMENT '配置值',
	`max_value` VARCHAR(50) NULL DEFAULT NULL COMMENT '上限值',
	`min_value` VARCHAR(50) NULL DEFAULT NULL COMMENT '下限值（预留）',
	`validate_range` VARCHAR(50) NULL DEFAULT NULL COMMENT '校验正则(预留) ',
	`is_valid` VARCHAR(2)   DEFAULT 'Y' COMMENT '是否有效 预留 Y/N',
	`create_by` VARCHAR(50) NOT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
	`update_by` VARCHAR(50) NULL DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'update time',
	PRIMARY KEY (`id`),
	UNIQUE INDEX `uniq_tid_kid` (`template_uuid`, `key_id`),
	UNIQUE INDEX `uniq_tname_kid` (`template_uuid`, `key_id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
```

### Configuration Key Limit For User Table
```sql
CREATE TABLE `linkis_ps_configuration_key_limit_for_user` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`user_name` VARCHAR(50) NOT NULL COMMENT '用户名',
	`combined_label_value` VARCHAR(128) NOT NULL COMMENT '组合标签 combined_userCreator_engineType  如 hadoop-IDE,spark-2.4.3',
	`key_id` BIGINT(20) NOT NULL COMMENT 'id of linkis_ps_configuration_config_key',
    `config_value` VARCHAR(200) NULL DEFAULT NULL COMMENT '配置值',
    `max_value` VARCHAR(50) NULL DEFAULT NULL COMMENT '上限值',
    `min_value` VARCHAR(50) NULL DEFAULT NULL COMMENT '下限值（预留）',
	`latest_update_template_uuid` VARCHAR(36) NOT NULL COMMENT 'uuid  第三方侧记录的模板id',
	`is_valid` VARCHAR(2)  DEFAULT 'Y' COMMENT '是否有效 预留 Y/N',
	`create_by` VARCHAR(50) NOT NULL COMMENT '创建人',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
	`update_by` VARCHAR(50) NULL DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'update time',
	PRIMARY KEY (`id`),
	UNIQUE INDEX `uniq_com_label_kid` (`combined_label_value`, `key_id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
```

## RPC Methods

The Configuration Service provides several RPC methods for configuration management:

### Configuration RPCs

#### getGlobalConfiguration
Retrieves global configuration properties:
```java
Map<String, String> getGlobalConfiguration()
```

#### getUserConfiguration
Retrieves user-specific configuration:
```java
Map<String, String> getUserConfiguration(String user)
```

#### updateConfiguration
Updates configuration properties:
```java
void updateConfiguration(String user, Map<String, String> configurations)
```

#### getConfigurationTemplate
Retrieves configuration template for an engine:
```java
ConfigurationTemplate getConfigurationTemplate(String engineType)
```

#### validateConfiguration
Validates configuration properties:
```java
ConfigurationValidationResult validateConfiguration(String key, String value)
```

#### getEngineConfiguration
Retrieves engine-specific configuration:
```java
Map<String, String> getEngineConfiguration(String engineType, String version, String user)
```

#### updateEngineConfiguration
Updates engine-specific configuration:
```java
void updateEngineConfiguration(String engineType, String version, String user, Map<String, String> configurations)
```

#### listConfigurationKeys
Lists all configuration keys for an engine type:
```java
List<ConfigurationKey> listConfigurationKeys(String engineType)
```

### Category RPCs

#### getCategoryConfiguration
Retrieves configuration for a category:
```java
Map<String, String> getCategoryConfiguration(String category)
```

#### updateCategoryConfiguration
Updates configuration for a category:
```java
void updateCategoryConfiguration(String category, Map<String, String> configurations)
```

## Dependencies

- linkis-mybatis
- linkis-rpc
- linkis-manager-common
- linkis-httpclient
- linkis-label-common

## Interface Classes and MyBatis XML Files

### Interface Classes
- ConfigurationRestfulApi: `linkis-public-enhancements/linkis-configuration/src/main/java/org/apache/linkis/configuration/restful/api/ConfigurationRestfulApi.java`
- TenantConfigrationRestfulApi: `linkis-public-enhancements/linkis-configuration/src/main/java/org/apache/linkis/configuration/restful/api/TenantConfigrationRestfulApi.java`
- UserIpConfigrationRestfulApi: `linkis-public-enhancements/linkis-configuration/src/main/java/org/apache/linkis/configuration/restful/api/UserIpConfigrationRestfulApi.java`
- TemplateRestfulApi: `linkis-public-enhancements/linkis-configuration/src/main/java/org/apache/linkis/configuration/restful/api/TemplateRestfulApi.java`
- AcrossClusterRuleRestfulApi: `linkis-public-enhancements/linkis-configuration/src/main/java/org/apache/linkis/configuration/restful/api/AcrossClusterRuleRestfulApi.java`
- ConfigurationTemplateRestfulApi: `linkis-public-enhancements/linkis-pes-publicservice/src/main/java/org/apache/linkis/basedatamanager/server/restful/ConfigurationTemplateRestfulApi.java`

### MyBatis XML Files
- ConfigMapper: `linkis-public-enhancements/linkis-configuration/src/main/resources/mapper/common/ConfigMapper.xml`
- LabelMapper: `linkis-public-enhancements/linkis-configuration/src/main/resources/mapper/common/LabelMapper.xml`
- UserTenantMapper: `linkis-public-enhancements/linkis-configuration/src/main/resources/mapper/common/UserTenantMapper.xml`
- DepartmentTenantMapper: `linkis-public-enhancements/linkis-configuration/src/main/resources/mapper/common/DepartmentTenantMapper.xml`
- UserIpMapper: `linkis-public-enhancements/linkis-configuration/src/main/resources/mapper/common/UserIpMapper.xml`
- DepartmentMapper: `linkis-public-enhancements/linkis-configuration/src/main/resources/mapper/common/DepartmentMapper.xml`
- AcrossClusterRuleMapper: `linkis-public-enhancements/linkis-configuration/src/main/resources/mapper/common/AcrossClusterRuleMapper.xml`
- TemplateConfigKeyMapper: `linkis-public-enhancements/linkis-configuration/src/main/resources/mapper/common/TemplateConfigKeyMapper.xml`
- ConfigKeyLimitForUserMapper: `linkis-public-enhancements/linkis-configuration/src/main/resources/mapper/common/ConfigKeyLimitForUserMapper.xml`