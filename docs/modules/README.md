# Apache Linkis Service Modules

## Overview

Apache Linkis is built using a microservices architecture and can be categorized into three service types:

1. **Microservice Governance Services**: Infrastructure services including service discovery, API gateway, and service communication
2. **Computation Governance Services**: Core services for computation task lifecycle management
3. **Public Enhancement Services**: Shared services that provide common capabilities across the platform

Each service type contains multiple actual deployable Java applications (sub-modules) that work together to provide the complete functionality.

## Service Structure

Based on the actual deployment structure, the services are organized as follows:

### Microservice Governance Services
These are the infrastructure services that provide the foundation for the microservices architecture:
- [Eureka Service](./microservice-governance/eureka.md) - Service registry and discovery
- [Gateway Service](./microservice-governance/gateway.md) - API gateway for request routing

### Computation Governance Services
These services handle the core computation task lifecycle:
- [Entrance Service](./computation-governance/entrance.md) - Task submission and entrance point
- [Manager Service](./computation-governance/manager.md) - Resource and application management
- [ECM Service](./computation-governance/ecm.md) - Engine Connection Manager
- [JobHistory Service](./computation-governance/jobhistory.md) - Task execution history tracking

### Public Enhancement Services
These services provide shared capabilities used across the platform:
- [Public Service](./public-enhancements/publicservice.md) - Core public services
- [Configuration Service](./public-enhancements/configuration.md) - Configuration management
- [BML Service](./public-enhancements/bml.md) - Big Data Material Library
- [DataSource Service](./public-enhancements/datasource.md) - Data source management
- [Context Service](./public-enhancements/context.md) - Context and variable sharing
- [Monitor Service](./microservice-governance/monitor.md) - System monitoring

## RPC Development Example

Linkis uses RPC (Remote Procedure Call) for inter-service communication. Here's a basic example of how to use RPC in Linkis:

```java
// Get sender for target service
Sender sender = Sender.getSender("linkis-publicservice");

// Create request protocol
InsLabelAttachRequest request = new InsLabelAttachRequest();
request.setServiceInstance(serviceInstance);
request.setLabels(labels);

// Send request via RPC
sender.ask(request);
```

## Documentation Structure

Each service module documentation includes:
- API interfaces
- Database table structures
- RPC methods provided to other services