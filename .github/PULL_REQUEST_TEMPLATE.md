### What is the purpose of the change
(For example: EngineConn-Core defines the the abstractions and interfaces of the EngineConn core functions.
The Engine Service in Linkis 0.x is refactored, EngineConn will handle the engine connection and session management.
Related issues: #590. )

### Brief change log
(for example:)
- Define the core abstraction and interfaces of the EngineConn Factory;
- Define the core abstraction and interfaces of Executor Manager.

### Verifying this change
(Please pick either of the following options)  
This change is a trivial rework / code cleanup without any test coverage.  
(or)  
This change is already covered by existing tests, such as (please describe tests).  
(or)  
This change added tests and can be verified as follows:  
(example:)  
- Added tests for submit and execute all kinds of jobs to go through and verify the lifecycles of different EngineConns.

### Does this pull request potentially affect one of the following parts:
- Dependencies (does it add or upgrade a dependency): (yes / no)
- Anything that affects deployment: (yes / no / don't know)
- The MGS(Microservice Governance Services), i.e., Spring Cloud Gateway, OpenFeign, Eureka.: (yes / no)

### Documentation
- Does this pull request introduce a new feature? (yes / no)
- If yes, how is the feature documented? (not applicable / docs / JavaDocs / not documented)