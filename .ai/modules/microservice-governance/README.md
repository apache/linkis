# Microservice Governance Services

The microservice governance services provide the infrastructure foundation for the Linkis microservices architecture.

## Service Modules

- [Eureka Service](./eureka.md) - Service registry and discovery center
- [Gateway Service](./gateway.md) - API gateway for request routing and security

## Overview

These services form the infrastructure layer of Linkis, providing essential capabilities for service discovery, API routing, and inter-service communication.

## Common Features

### Service Discovery
- Service registration and deregistration
- Health checking of services
- Service instance management
- Load balancing support

### API Gateway
- Request routing and filtering
- Authentication and authorization
- Rate limiting and traffic control
- Request/response transformation

### Inter-Service Communication
- RESTful service communication
- Load balancing between services
- Circuit breaker pattern implementation
- Service monitoring and metrics

## API Interface Summary

### Eureka Service APIs
- Service registration: `POST /eureka/apps/{appName}`
- Service discovery: `GET /eureka/apps/{appName}`
- Health check: `GET /eureka/apps/{appName}/{instanceId}`

### Gateway Service APIs
- Route management: `GET /actuator/gateway/routes`
- Health check: `GET /actuator/health`
- Gateway metrics: `GET /actuator/metrics`

## Database Schema Summary

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

## RPC Methods Summary

### Eureka Service RPCs
- `registerService(ServiceRegistrationRequest request)`
- `unregisterService(String serviceId, String instanceId)`
- `getServiceInstances(String serviceName)`
- `heartbeat(String serviceId, String instanceId)`

### Gateway Service RPCs
- `addRoute(GatewayRoute route)`
- `removeRoute(String routeId)`
- `updateRoute(GatewayRoute route)`
- `getRoutes()`
- `configureAuthentication(AuthenticationConfig config)`
- `validateToken(String token)`
- `getUserPermissions(String user)`
- `applyRateLimit(RateLimitConfig config)`
- `getRateLimitStatus(String clientId)`