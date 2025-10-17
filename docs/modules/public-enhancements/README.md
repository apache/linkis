# Public Enhancement Services

The public enhancement services provide shared capabilities used across the Linkis platform.

## Service Modules

- [Public Service](./publicservice.md) - Core public services
- [Configuration Service](./configuration.md) - Configuration management
- [BML Service](./bml.md) - Big Data Material Library
- [DataSource Service](./datasource.md) - Data source management
- [Context Service](./context.md) - Context and variable sharing
- [Monitor Service](./monitor.md) - System monitoring

## Overview

These services provide common capabilities that are used across the Linkis platform, including file management, configuration management, data source management, context sharing, and system monitoring.

## Common Features

### Resource Management
- Binary and material management
- User-defined function management
- Shared resource tracking

### Configuration Management
- Centralized configuration service
- Runtime configuration management
- Configuration versioning

### Context Management
- Cross-application context sharing
- Variable and parameter management
- Unified context service

### Data Source Management
- Data source registration and management
- Metadata querying
- Connection testing and validation

### System Monitoring
- Performance metrics collection
- System health monitoring
- Alerting and notifications

## API Interface Summary

### Public Service APIs
- File system operations
- Variable management
- Error code querying

### Configuration Service APIs
- Configuration retrieval: `GET /api/rest_j/v1/configuration`
- Configuration update: `POST /api/rest_j/v1/configuration/update`
- Template management: `GET /api/rest_j/v1/configuration/template`

### BML Service APIs
- File upload: `POST /api/rest_j/v1/bml/upload`
- File download: `GET /api/rest_j/v1/bml/download`
- File version list: `GET /api/rest_j/v1/bml/versions`

### DataSource Service APIs
- Data source CRUD: `POST/GET/PUT/DELETE /api/rest_j/v1/datasource`
- Metadata query: `GET /api/rest_j/v1/datasource/metadata`
- Connection test: `POST /api/rest_j/v1/datasource/connect`

### Context Service APIs
- Context creation: `POST /api/rest_j/v1/context`
- Variable management: `POST /api/rest_j/v1/context/variable`
- Context sharing: `POST /api/rest_j/v1/context/share`

### Monitor Service APIs
- Metrics collection: `GET /api/rest_j/v1/monitor/metrics`
- Health check: `GET /api/rest_j/v1/monitor/health`
- Alert management: `POST /api/rest_j/v1/monitor/alert`

## Database Schema Summary

### BML Resources Table
```sql
CREATE TABLE if not exists `linkis_ps_bml_resources` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `resource_id` varchar(50) NOT NULL COMMENT 'resource uuid',
  `is_private` TINYINT(1) DEFAULT 0 COMMENT 'Whether the resource is private, 0 means private, 1 means public',
  `resource_header` TINYINT(1) DEFAULT 0 COMMENT 'Classification, 0 means unclassified, 1 means classified',
  `downloaded_file_name` varchar(200) DEFAULT NULL COMMENT 'File name when downloading',
  `sys` varchar(100) NOT NULL COMMENT 'Owning system',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
  `owner` varchar(200) NOT NULL COMMENT 'Resource owner',
  `is_expire` TINYINT(1) DEFAULT 0 COMMENT 'Whether expired, 0 means not expired, 1 means expired',
  `expire_type` varchar(50) DEFAULT null COMMENT 'Expiration type, date refers to the expiration on the specified date, TIME refers to the time',
  `expire_time` varchar(50) DEFAULT null COMMENT 'Expiration time, one day by default',
  `max_version` int(20) DEFAULT 10 COMMENT 'The default is 10, which means to keep the latest 10 versions',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Updated time',
  `updator` varchar(50) DEFAULT NULL COMMENT 'updator',
  `enable_flag` tinyint(1) NOT NULL DEFAULT '1' COMMENT 'Status, 1: normal, 0: frozen',
  unique key `uniq_rid_eflag`(`resource_id`, `enable_flag`),
  PRIMARY KEY (`id`)
);
```

### BML Resources Version Table
```sql
CREATE TABLE if not exists `linkis_ps_bml_resources_version` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `resource_id` varchar(50) NOT NULL COMMENT 'Resource uuid',
  `file_md5` varchar(32) NOT NULL COMMENT 'Md5 summary of the file',
  `version` varchar(20) NOT NULL COMMENT 'Resource version (v plus five digits)',
  `size` int(10) NOT NULL COMMENT 'File size',
  `start_byte` BIGINT(20) UNSIGNED NOT NULL DEFAULT 0,
  `end_byte` BIGINT(20) UNSIGNED NOT NULL DEFAULT 0,
  `resource` varchar(2000) NOT NULL COMMENT 'Resource content (file information including path and file name)',
  `description` varchar(2000) DEFAULT NULL COMMENT 'description',
  `start_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Started time',
  `end_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Stoped time',
  `client_ip` varchar(200) NOT NULL COMMENT 'Client ip',
  `updator` varchar(50) DEFAULT NULL COMMENT 'updator',
  `enable_flag` tinyint(1) NOT NULL DEFAULT '1' COMMENT 'Status, 1: normal, 0: frozen',
  unique key `uniq_rid_version`(`resource_id`, `version`),
  PRIMARY KEY (`id`)
);
```

### Configuration Key Table
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
```

### Data Source Table
```sql
CREATE TABLE `linkis_ps_datasource_table` (
  `id` bigint(255) NOT NULL AUTO_INCREMENT,
  `database` varchar(64) COLLATE utf8_bin NOT NULL,
  `name` varchar(64) COLLATE utf8_bin NOT NULL,
  `alias` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `creator` varchar(16) COLLATE utf8_bin NOT NULL,
  `comment` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `create_time` datetime NOT NULL,
  `product_name` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `project_name` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `usage` varchar(128) COLLATE utf8_bin DEFAULT NULL,
  `lifecycle` int(4) NOT NULL,
  `use_way` int(4) NOT NULL,
  `is_import` tinyint(1) NOT NULL,
  `model_level` int(4) NOT NULL,
  `is_external_use` tinyint(1) NOT NULL,
  `is_partition_table` tinyint(1) NOT NULL,
  `is_available` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_db_name` (`database`,`name`)
);
```

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

## RPC Methods Summary

### Public Service RPCs
- `getErrorCode(String errorCode)`
- `getVariable(String variableName)`
- `setVariable(String variableName, String value)`

### Configuration Service RPCs
- `getConfiguration(String user, String creator, String engineType)`
- `updateConfiguration(String user, ConfigurationUpdateRequest request)`
- `getTemplateConfiguration(String engineType)`

### BML Service RPCs
- `uploadResource(ResourceUploadRequest request)`
- `downloadResource(String resourceId, String version)`
- `deleteResource(String resourceId)`
- `getResourceInfo(String resourceId)`

### DataSource Service RPCs
- `createDataSource(DataSourceCreateRequest request)`
- `updateDataSource(DataSourceUpdateRequest request)`
- `deleteDataSource(Long dataSourceId)`
- `queryDataSource(DataSourceQueryRequest request)`

### Context Service RPCs
- `createContext(ContextCreateRequest request)`
- `getContextValue(String contextId, String key)`
- `setContextValue(String contextId, String key, String value)`
- `removeContext(String contextId)`

### Monitor Service RPCs
- `collectMetrics(MetricsCollectionRequest request)`
- `getHealthStatus()`
- `sendAlert(AlertRequest request)`

## Dependencies

- linkis-commons - Shared utilities
- linkis-protocol - Communication protocols
- linkis-rpc - Remote procedure calls
- Various database drivers
- Spring Cloud ecosystem