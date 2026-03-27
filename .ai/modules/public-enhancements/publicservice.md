# Public Service

The Public Service provides core public services for the Linkis system.

## Overview

This service provides common public services including file system operations, variable management, and other shared functionalities.

## Key Components

### Core Classes
- `LinkisPublicServiceApp` - Main application class
- File system operations
- Variable management
- Shared service utilities

### Features
- File system operations (upload, download, list)
- Variable management
- Shared service utilities
- Common REST APIs

## API Interfaces

### File System Operations
```
POST /api/rest_j/v1/filesystem/upload
```

Request:
```
multipart/form-data with file content
```

Response:
```json
{
  "method": "/api/rest_j/v1/filesystem/upload",
  "status": 0,
  "message": "success",
  "data": {
    "path": "/path/to/uploaded/file"
  }
}
```

### Variable Management
```
POST /api/rest_j/v1/variable/add
```

Request Body:
```json
{
  "key": "variableKey",
  "value": "variableValue",
  "user": "testuser"
}
```

Response:
```json
{
  "method": "/api/rest_j/v1/variable/add",
  "status": 0,
  "message": "success",
  "data": {}
}
```

## Database Table Structures

The Public Service manages the following database tables:

### File System Metadata Table
```sql
CREATE TABLE linkis_filesystem_meta (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_name VARCHAR(32) NOT NULL,
  path VARCHAR(500) NOT NULL,
  file_type VARCHAR(50),
  file_size BIGINT,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_path (user_name, path)
);
```

### Variable Table
```sql
CREATE TABLE linkis_variable (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_name VARCHAR(32) NOT NULL,
  key_name VARCHAR(128) NOT NULL,
  value TEXT,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_key (user_name, key_name)
);
```

## RPC Methods

The Public Service provides several RPC methods for common operations:

### File System RPCs

#### uploadFile
Uploads a file:
```java
String uploadFile(FileUploadRequest request)
```

#### downloadFile
Downloads a file:
```java
FileContent downloadFile(String path, String user)
```

#### listFiles
Lists files in a directory:
```java
List<FileInfo> listFiles(String path, String user)
```

### Variable RPCs

#### setVariable
Sets a variable:
```java
void setVariable(String key, String value, String user)
```

#### getVariable
Retrieves a variable:
```java
String getVariable(String key, String user)
```

#### deleteVariable
Deletes a variable:
```java
void deleteVariable(String key, String user)
```

## Dependencies

- linkis-filesystem
- linkis-variable
- linkis-rpc
- linkis-protocol