# Gateway Service

The Gateway service provides API gateway functionality for the Linkis system, routing requests to appropriate backend services and providing security, rate limiting, and other cross-cutting concerns.

## Overview

This service implements an API gateway that serves as the single entry point for all client requests to the Linkis system. It handles request routing, authentication, authorization, rate limiting, and other infrastructure concerns.

## Key Components

### Core Classes
- `LinkisGatewayApplication` - Main application class
- Route configuration management
- Request/response filtering
- Authentication handling
- Rate limiting implementation

### Features
- Request routing and load balancing
- Authentication and authorization
- Rate limiting and traffic control
- Request/response transformation
- SSL/TLS termination
- Logging and monitoring

## API Interfaces

### Route Management
```
GET /actuator/gateway/routes
```

Response:
```json
{
  "routes": [
    {
      "route_id": "linkis-entrance",
      "uri": "lb://linkis-entrance",
      "predicates": [
        "Path=/api/entrance/**"
      ],
      "filters": [
        "StripPrefix=1"
      ]
    }
  ]
}
```

### Health Check
```
GET /actuator/health
```

Response:
```json
{
  "status": "UP",
  "components": {
    "discoveryComposite": {
      "status": "UP"
    },
    "gateway": {
      "status": "UP"
    }
  }
}
```

### Gateway Metrics
```
GET /actuator/metrics
```

Response:
```json
{
  "names": [
    "gateway.requests",
    "jvm.memory.used",
    "http.server.requests"
  ]
}
```

### Authentication Token Management
```
GET /api/rest_j/v1/basedata-manager/gateway-auth-token
```

Response:
```json
{
  "method": "",
  "status": 0,
  "message": "",
  "data": {
    "list": {
      "total": 0,
      "list": [],
      "pageNum": 1,
      "pageSize": 10,
      "size": 0,
      "startRow": 0,
      "endRow": 0,
      "pages": 0,
      "prePage": 0,
      "nextPage": 0,
      "isFirstPage": true,
      "isLastPage": true,
      "hasPreviousPage": false,
      "hasNextPage": false,
      "navigatePages": 8,
      "navigatepageNums": []
    }
  }
}
```

### Add Authentication Token
```
POST /api/rest_j/v1/basedata-manager/gateway-auth-token
```

Request Body:
```json
{
  "tokenName": "test-token",
  "legalUsers": "*",
  "businessOwner": "BDP"
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

### Update Authentication Token
```
PUT /api/rest_j/v1/basedata-manager/gateway-auth-token
```

Request Body:
```json
{
  "id": 1,
  "tokenName": "test-token",
  "legalUsers": "user1,user2",
  "businessOwner": "BDP"
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

### Get Authentication Token
```
GET /api/rest_j/v1/basedata-manager/gateway-auth-token/{id}
```

Response:
```json
{
  "method": "",
  "status": 0,
  "message": "",
  "data": {
    "item": {
      "id": 1,
      "tokenName": "test-token",
      "legalUsers": "user1,user2",
      "businessOwner": "BDP",
      "createTime": "2023-01-01 12:00:00",
      "updateTime": "2023-01-01 12:00:00"
    }
  }
}
```

### Remove Authentication Token
```
DELETE /api/rest_j/v1/basedata-manager/gateway-auth-token/{id}
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

### Check Authentication Token
```
GET /api/rest_j/v1/basedata-manager/gateway-auth-token/checkToken
```

Parameters:
- `token`: Authentication token to check (required)
- `checkName`: User name to check (required)

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

### Decrypt Authentication Token
```
GET /api/rest_j/v1/basedata-manager/gateway-auth-token/decrypt-token
```

Parameters:
- `token`: Authentication token to decrypt (required)

Response:
```json
{
  "method": "",
  "status": 0,
  "message": "",
  "data": {
    "result": "decrypted-token"
  }
}
```

## Database Table Structures

The Gateway service manages the following database tables:

### Gateway Route Table
```sql
CREATE TABLE linkis_gateway_route (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  route_id VARCHAR(128) NOT NULL UNIQUE,
  route_order INT DEFAULT 0,
  uri VARCHAR(255) NOT NULL,
  predicates JSON,
  filters JSON,
  metadata JSON,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### Gateway Filter Table
```sql
CREATE TABLE linkis_gateway_filter (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  route_id VARCHAR(128) NOT NULL,
  filter_name VARCHAR(128) NOT NULL,
  filter_order INT DEFAULT 0,
  args JSON,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (route_id) REFERENCES linkis_gateway_route(route_id) ON DELETE CASCADE
);
```

### Authentication Configuration Table
```sql
CREATE TABLE linkis_gateway_auth (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  path_pattern VARCHAR(255) NOT NULL,
  auth_required BOOLEAN DEFAULT TRUE,
  allowed_roles JSON,
  rate_limit INT,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### Gateway Access Log Table
```sql
CREATE TABLE linkis_gateway_access_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  client_ip VARCHAR(50),
  request_method VARCHAR(10),
  request_uri VARCHAR(500),
  request_params TEXT,
  user_token VARCHAR(255),
  service_id VARCHAR(128),
  response_status INT,
  response_time BIGINT,
  access_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

### Gateway Auth Token Table
```sql
CREATE TABLE `linkis_gateway_auth_token` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `token_name` varchar(255) COLLATE utf8_bin NOT NULL,
  `legal_users` varchar(255) COLLATE utf8_bin NOT NULL,
  `create_by` varchar(255) COLLATE utf8_bin NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `elapse_day` bigint(20) DEFAULT '-1',
  `update_by` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `business_owner` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `token_alias` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `token_sign` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_token_name` (`token_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
```

## RPC Methods

The Gateway service provides RPC methods for gateway management:

### Route Management RPCs

#### addRoute
Adds a new route configuration:
```java
void addRoute(GatewayRoute route)
```

#### removeRoute
Removes a route configuration:
```java
void removeRoute(String routeId)
```

#### updateRoute
Updates a route configuration:
```java
void updateRoute(GatewayRoute route)
```

#### getRoutes
Retrieves all route configurations:
```java
List<GatewayRoute> getRoutes()
```

### Authentication RPCs

#### configureAuthentication
Configures authentication for a path:
```java
void configureAuthentication(AuthenticationConfig config)
```

#### validateToken
Validates an authentication token:
```java
TokenValidationResult validateToken(String token)
```

#### getUserPermissions
Retrieves user permissions:
```java
UserPermissions getUserPermissions(String user)
```

### Rate Limiting RPCs

#### applyRateLimit
Applies rate limiting to a route:
```java
void applyRateLimit(RateLimitConfig config)
```

#### getRateLimitStatus
Retrieves current rate limit status:
```java
RateLimitStatus getRateLimitStatus(String clientId)
```

## Dependencies

- Spring Cloud Gateway
- Spring Boot
- Spring Security
- Spring Cloud LoadBalancer
- linkis-common
- linkis-httpclient
- Various Spring Cloud components

## Interface Classes and MyBatis XML Files

### Interface Classes
- GatewayAuthTokenRestfulApi: `linkis-public-enhancements/linkis-pes-publicservice/src/main/java/org/apache/linkis/basedatamanager/server/restful/GatewayAuthTokenRestfulApi.java`

### MyBatis XML Files
- GatewayRouteMapper: `linkis-public-enhancements/linkis-pes-publicservice/src/main/resources/mapper/GatewayRouteMapper.xml`
- GatewayFilterMapper: `linkis-public-enhancements/linkis-pes-publicservice/src/main/resources/mapper/GatewayFilterMapper.xml`
- GatewayAuthMapper: `linkis-public-enhancements/linkis-pes-publicservice/src/main/resources/mapper/GatewayAuthMapper.xml`
- GatewayAccessLogMapper: `linkis-public-enhancements/linkis-pes-publicservice/src/main/resources/mapper/GatewayAccessLogMapper.xml`
- GatewayAuthTokenMapper: `linkis-public-enhancements/linkis-pes-publicservice/src/main/resources/mapper/GatewayAuthTokenMapper.xml`