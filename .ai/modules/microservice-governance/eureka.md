# Eureka Service

The Eureka service provides service registration and discovery capabilities for the Linkis microservices architecture.

## Overview

This service implements the Eureka server for service discovery, managing the registration, discovery, and health checking of all microservice instances in the Linkis system.

## Key Components

### Core Classes
- `SpringCloudEurekaApplication` - Main application class
- Eureka server configuration
- Health check endpoints
- Service registry management

### Features
- Service instance registration
- Service discovery for clients
- Health status monitoring
- REST API for service management

## API Interfaces

### Service Registration
```
POST /eureka/apps/{appName}
```

Request Body:
```xml
<instance>
  <hostName>service-host</hostName>
  <app>APP-NAME</app>
  <ipAddr>127.0.0.1</ipAddr>
  <port>8080</port>
  <status>UP</status>
</instance>
```

### Service Discovery
```
GET /eureka/apps/{appName}
```

Response:
```xml
<application>
  <name>APP-NAME</name>
  <instance>
    <hostName>service-host</hostName>
    <app>APP-NAME</app>
    <ipAddr>127.0.0.1</ipAddr>
    <port>8080</port>
    <status>UP</status>
  </instance>
</application>
```

### Health Check
```
GET /eureka/apps/{appName}/{instanceId}
```

Response:
```xml
<instance>
  <hostName>service-host</hostName>
  <app>APP-NAME</app>
  <ipAddr>127.0.0.1</ipAddr>
  <port>8080</port>
  <status>UP</status>
  <lastUpdatedTimestamp>1234567890</lastUpdatedTimestamp>
</instance>
```

## Database Table Structures

The Eureka service typically doesn't directly manage database tables, as it stores service registry information in memory. However, it may interact with the following tables for persistent storage:

### Service Registry Table
```sql
CREATE TABLE linkis_service_registry (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  service_name VARCHAR(128) NOT NULL,
  instance_id VARCHAR(128) NOT NULL UNIQUE,
  instance_address VARCHAR(128),
  instance_port INT,
  status VARCHAR(50) DEFAULT 'UP',
  metadata JSON,
  register_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  last_heartbeat DATETIME,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

## RPC Methods

The Eureka service provides RPC methods for service management:

### Service Management RPCs

#### registerService
Registers a service instance:
```java
void registerService(ServiceRegistrationRequest request)
```

#### unregisterService
Unregisters a service instance:
```java
void unregisterService(String serviceId, String instanceId)
```

#### getServiceInstances
Retrieves instances of a service:
```java
List<ServiceInstance> getServiceInstances(String serviceName)
```

#### heartbeat
Sends a heartbeat for a service instance:
```java
void heartbeat(String serviceId, String instanceId)
```

## Dependencies

- Spring Cloud Netflix Eureka Server
- Spring Boot
- Netflix Eureka Core