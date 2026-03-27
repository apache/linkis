# BML Service

The BML (Big Data Material Library) Service provides file and material management capabilities for the Linkis system.

## Overview

This service manages the storage, versioning, and sharing of files and materials used in big data processing tasks.

## Key Components

### Core Classes
- `LinkisBMLApplication` - Main application class
- File upload and download
- File version management
- File sharing and access control

### Features
- File upload and download
- File versioning
- File sharing
- Access control
- File metadata management

## API Interfaces

### File Upload
```
POST /api/rest_j/v1/bml/upload
```

Parameters:
- `system` (optional): System name
- `resourceHeader` (optional): Resource header
- `isExpire` (optional): Whether resource expires
- `expireType` (optional): Expiration type
- `expireTime` (optional): Expiration time
- `maxVersion` (optional): Maximum version count
- `file` (required): File to upload

Response:
```
{
  "method": "/api/bml/upload",
  "status": 0,
  "message": "The task of submitting and uploading resources was successful(提交上传资源任务成功)",
  "data": {
    "resourceId": "resource-12345",
    "version": "v000001",
    "taskId": 12345
  }
}
```

### File Download
```
GET /api/rest_j/v1/bml/download
```

Parameters:
- `resourceId`: Resource ID to download (required)
- `version`: Version to download (optional, defaults to latest)

Response:
```
Binary file content
```

### File Version List
```
GET /api/rest_j/v1/bml/getVersions
```

Parameters:
- `resourceId`: Resource ID to list versions for (required)
- `currentPage`: Current page number (optional)
- `pageSize`: Page size (optional)

Response:
```json
{
  "method": "/api/bml/getVersions",
  "status": 0,
  "message": "Version information obtained successfully (成功获取版本信息)",
  "data": {
    "ResourceVersions": {
      "resourceId": "resource-12345",
      "user": "testuser",
      "versions": [
        {
          "version": "v000001",
          "size": 1024,
          "createTime": "2023-01-01 12:00:00"
        }
      ]
    }
  }
}
```

### File Update
```
POST /api/rest_j/v1/bml/updateVersion
```

Parameters:
- `resourceId`: Resource ID to update (required)
- `file`: File to upload (required)

Response:
```json
{
  "method": "/api/bml/updateVersion",
  "status": 0,
  "message": "The update resource task was submitted successfully(提交更新资源任务成功)",
  "data": {
    "resourceId": "resource-12345",
    "version": "v000002",
    "taskId": 12346
  }
}
```

### File Delete
```
POST /api/rest_j/v1/bml/deleteResource
```

Request Body:
```json
{
  "resourceId": "resource-12345"
}
```

Response:
```json
{
  "method": "/api/bml/deleteResource",
  "status": 0,
  "message": "Resource deleted successfully(删除资源成功)"
}
```

### Batch File Delete
```
POST /api/rest_j/v1/bml/deleteResources
```

Request Body:
```json
{
  "resourceIds": ["resource-12345", "resource-12346"]
}
```

Response:
```json
{
  "method": "/api/bml/deleteResources",
  "status": 0,
  "message": "Batch deletion of resource was successful(批量删除资源成功)"
}
```

### File Information
```
GET /api/rest_j/v1/bml/getBasic
```

Parameters:
- `resourceId`: Resource ID to get information for (required)

Response:
```json
{
  "method": "/api/bml/getBasic",
  "status": 0,
  "message": "Acquisition of resource basic information successfully(获取资源基本信息成功)",
  "data": {
    "basic": {
      "resourceId": "resource-12345",
      "owner": "testuser",
      "createTime": "2023-01-01 12:00:00",
      "downloadedFileName": "test.csv",
      "expireTime": "Resource not expired(资源不过期)",
      "numberOfVerions": 10
    }
  }
}
```

### Version Delete
```
POST /api/rest_j/v1/bml/deleteVersion
```

Request Body:
```json
{
  "resourceId": "resource-12345",
  "version": "v000001"
}
```

Response:
```json
{
  "method": "/api/bml/deleteVersion",
  "status": 0,
  "message": "Deleted version successfully(删除版本成功)"
}
```

### Change Owner
```
POST /api/rest_j/v1/bml/changeOwner
```

Request Body:
```json
{
  "resourceId": "resource-12345",
  "oldOwner": "testuser",
  "newOwner": "newuser"
}
```

Response:
```json
{
  "method": "/api/bml/changeOwner",
  "status": 0,
  "message": "更新owner成功！"
}
```

### Copy Resource To Another User
```
POST /api/rest_j/v1/bml/copyResourceToAnotherUser
```

Request Body:
```json
{
  "resourceId": "resource-12345",
  "anotherUser": "newuser"
}
```

Response:
```json
{
  "method": "/api/bml/copyResourceToAnotherUser",
  "status": 0,
  "message": "success",
  "data": {
    "resourceId": "resource-67890"
  }
}
```

### Rollback Version
```
POST /api/rest_j/v1/bml/rollbackVersion
```

Request Body:
```json
{
  "resourceId": "resource-12345",
  "version": "v000001"
}
```

Response:
```json
{
  "method": "/api/bml/rollbackVersion",
  "status": 0,
  "message": "success",
  "data": {
    "resourceId": "resource-12345",
    "version": "v000001"
  }
}
```

### Create BML Project
```
POST /api/rest_j/v1/bml/createBmlProject
```

Request Body:
```json
{
  "projectName": "test-project",
  "editUsers": ["user1", "user2"],
  "accessUsers": ["user3", "user4"]
}
```

Response:
```json
{
  "method": "/api/bml/createBmlProject",
  "status": 0,
  "message": "success to create project(创建工程ok)"
}
```

### Upload Share Resource
```
POST /api/rest_j/v1/bml/uploadShareResource
```

Parameters:
- `system` (optional): System name
- `resourceHeader` (optional): Resource header
- `isExpire` (optional): Whether resource expires
- `expireType` (optional): Expiration type
- `expireTime` (optional): Expiration time
- `maxVersion` (optional): Maximum version count
- `projectName`: Project name (required)
- `file`: File to upload (required)

Response:
```json
{
  "method": "/api/bml/uploadShareResource",
  "status": 0,
  "message": "The task of submitting and uploading resources was successful(提交上传资源任务成功)",
  "data": {
    "resourceId": "resource-12345",
    "version": "v000001",
    "taskId": 12345
  }
}
```

### Update Share Resource
```
POST /api/rest_j/v1/bml/updateShareResource
```

Parameters:
- `resourceId`: Resource ID to update (required)
- `file`: File to upload (required)

Response:
```json
{
  "method": "/api/bml/updateShareResource",
  "status": 0,
  "message": "The update resource task was submitted successfully(提交更新资源任务成功)",
  "data": {
    "resourceId": "resource-12345",
    "version": "v000002",
    "taskId": 12346
  }
}
```

### Download Share Resource
```
GET /api/rest_j/v1/bml/downloadShareResource
```

Parameters:
- `resourceId`: Resource ID to download (required)
- `version`: Version to download (optional, defaults to latest)

Response:
```
Binary file content
```

### Update Project Users
```
POST /api/rest_j/v1/bml/updateProjectUsers
```

Request Body:
```json
{
  "projectName": "test-project",
  "editUsers": ["user1", "user2", "user5"],
  "accessUsers": ["user3", "user4", "user6"]
}
```

Response:
```json
{
  "method": "/api/bml/updateProjectUsers",
  "status": 0,
  "message": "Updated project related user success(更新工程的相关用户成功)"
}
```

## Database Table Structures

The BML Service uses the following database tables from linkis_ddl.sql:

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

### BML Resources Permission Table
```sql
CREATE TABLE if not exists `linkis_ps_bml_resources_permission` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `resource_id` varchar(50) NOT NULL COMMENT 'Resource uuid',
  `permission` varchar(10) NOT NULL COMMENT 'permission',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'created time',
  `system` varchar(50) default "dss" COMMENT 'creator',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'updated time',
  `updator` varchar(50) NOT NULL COMMENT 'updator',
  PRIMARY KEY (`id`)
);
```

### BML Resources Task Table
```sql
CREATE TABLE if not exists `linkis_ps_bml_resources_task` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `resource_id` varchar(50) DEFAULT NULL COMMENT 'resource uuid',
  `version` varchar(20) DEFAULT NULL COMMENT 'Resource version number of the current operation',
  `operation` varchar(20) NOT NULL COMMENT 'Operation type. upload = 0, update = 1',
  `state` varchar(20) NOT NULL DEFAULT 'Schduled' COMMENT 'Current status of the task:Schduled, Running, Succeed, Failed,Cancelled',
  `submit_user` varchar(20) NOT NULL DEFAULT '' COMMENT 'Job submission user name',
  `system` varchar(20) DEFAULT 'dss' COMMENT 'Subsystem name: wtss',
  `instance` varchar(128) NOT NULL COMMENT 'Material library example',
  `client_ip` varchar(50) DEFAULT NULL COMMENT 'Request IP',
  `extra_params` text COMMENT 'Additional key information. Such as the resource IDs and versions that are deleted in batches, and all versions under the resource are deleted',
  `err_msg` varchar(2000) DEFAULT NULL COMMENT 'Task failure information.e.getMessage',
  `start_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Starting time',
  `end_time` datetime DEFAULT NULL COMMENT 'End Time',
  `last_update_time` datetime NOT NULL COMMENT 'Last update time',
   unique key `uniq_rid_version` (resource_id, version),
  PRIMARY KEY (`id`)
);
```

### BML Project Table
```sql
create table if not exists linkis_ps_bml_project(
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `name` varchar(128) DEFAULT NULL,
  `system` varchar(64) not null default "dss",
  `source` varchar(1024) default null,
  `description` varchar(1024) default null,
  `creator` varchar(128) not null,
  `enabled` tinyint default 1,
  `create_time` datetime DEFAULT now(),
  unique key `uniq_name` (`name`),
PRIMARY KEY (`id`)
);
```

## RPC Methods

The BML Service provides several RPC methods for file management:

### Resource RPCs

#### uploadResource
Uploads a resource:
```java
ResourceUploadResult uploadResource(ResourceUploadRequest request)
```

#### downloadResource
Downloads a resource:
```java
ResourceContent downloadResource(String resourceId, String version)
```

#### deleteResource
Deletes a resource:
```java
void deleteResource(String resourceId)
```

#### getResourceInfo
Retrieves resource information:
```java
ResourceInfo getResourceInfo(String resourceId)
```

#### listResources
Lists resources for a user:
```java
List<ResourceInfo> listResources(String username)
```

### Version RPCs

#### listVersions
Lists versions of a resource:
```java
List<ResourceVersion> listVersions(String resourceId)
```

#### updateResource
Updates a resource with a new version:
```java
ResourceUpdateResult updateResource(ResourceUpdateRequest request)
```

#### deleteVersion
Deletes a specific version of a resource:
```java
void deleteVersion(String resourceId, String version)
```

#### getVersionInfo
Gets information about a specific version:
```java
ResourceVersion getVersionInfo(String resourceId, String version)
```

### Permission RPCs

#### grantPermission
Grants permission to a user:
```java
void grantPermission(String resourceId, String username, String permission)
```

#### checkPermission
Checks if a user has permission:
```java
boolean checkPermission(String resourceId, String username, String permission)
```

#### revokePermission
Revokes permission from a user:
```java
void revokePermission(String resourceId, String username)
```

#### listPermissions
Lists all permissions for a resource:
```java
List<ResourcePermission> listPermissions(String resourceId)
```

### Project RPCs

#### createProject
Creates a new project:
```java
Project createProject(ProjectCreateRequest request)
```

#### deleteProject
Deletes a project:
```java
void deleteProject(Long projectId)
```

#### addResourceToProject
Adds a resource to a project:
```java
void addResourceToProject(Long projectId, String resourceId)
```

#### removeResourceFromProject
Removes a resource from a project:
```java
void removeResourceFromProject(Long projectId, String resourceId)
```

## Dependencies

- linkis-bml-server
- linkis-mybatis
- linkis-rpc
- linkis-protocol

## Interface Classes and MyBatis XML Files

### Interface Classes
- BmlRestfulApi: `linkis-public-enhancements/linkis-bml-server/src/main/java/org/apache/linkis/bml/restful/BmlRestfulApi.java`
- BmlProjectRestful: `linkis-public-enhancements/linkis-bml-server/src/main/java/org/apache/linkis/bml/restful/BmlProjectRestful.java`
- BMLFsRestfulApi: `linkis-public-enhancements/linkis-pes-publicservice/src/main/java/org/apache/linkis/filesystem/restful/api/BMLFsRestfulApi.java`

### MyBatis XML Files
- ResourceMapper: `linkis-public-enhancements/linkis-bml-server/src/main/resources/mapper/common/ResourceMapper.xml`
- VersionMapper: `linkis-public-enhancements/linkis-bml-server/src/main/resources/mapper/common/VersionMapper.xml`
- TaskMapper: `linkis-public-enhancements/linkis-bml-server/src/main/resources/mapper/common/TaskMapper.xml`
- DownloadMapper: `linkis-public-enhancements/linkis-bml-server/src/main/resources/mapper/common/DownloadMapper.xml`
- BmlProjectMapper: `linkis-public-enhancements/linkis-bml-server/src/main/resources/mapper/common/BmlProjectMapper.xml`
