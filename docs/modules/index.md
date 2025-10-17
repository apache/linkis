# Apache Linkis Service Module Documentation

This documentation provides detailed information about each service module in Apache Linkis. Each module contains comprehensive documentation about its APIs, database schemas, and RPC methods.

## Service Module Categories

1. [Microservice Governance Services](./microservice-governance/README.md)
   - Infrastructure services for the microservices architecture
   
2. [Computation Governance Services](./computation-governance/README.md)
   - Core services for computation task lifecycle management
   
3. [Public Enhancement Services](./public-enhancements/README.md)
   - Shared services that provide common capabilities

## Documentation Standards

Each service module documentation follows these standards:

### API Interface Documentation
- Complete list of RESTful endpoints
- Request/response formats with examples
- Authentication requirements
- Error handling and status codes

### Database Schema Documentation
- Accurate table structures from linkis_ddl.sql
- Primary and foreign key relationships
- Indexing strategies
- Data types and constraints

### RPC Method Documentation
- Remote procedure calls provided by the service
- Method signatures and parameters
- Return types and error conditions
- Usage examples

## Contributing

This documentation is a living document that should be updated as the codebase evolves. When making changes to any service module, please ensure the corresponding documentation is updated to reflect those changes.