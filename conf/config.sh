###
SSH_PORT=22

### deploy user
deployUser=hadoop


### The install home path of Linkis
LINKIS_INSTALL_HOME=/appcom/Install/Linkis        #Must provided



### Specifies the user workspace, which is used to store the user's script files and log files.
### Generally local directory
WORKSPACE_USER_ROOT_PATH=file:///tmp/linkis/ ##file:// required
### User's root hdfs path
HDFS_USER_ROOT_PATH=hdfs:///tmp/linkis ##hdfs:// required

### Path to store job ResultSet：file or hdfs path
RESULT_SET_ROOT_PATH=hdfs:///tmp/linkis

### Provide the DB information of Hive metadata database.
#HIVE_META_URL=
#HIVE_META_USER=
#HIVE_META_PASSWORD=


################### The install Configuration of all Micro-Services #####################
#
#    NOTICE:
#       1. If you just wanna try, the following micro-service configuration can be set without any settings.
#            These services will be installed by default on this machine.
#       2. In order to get the most complete enterprise-level features, we strongly recommend that you install
#            Linkis in a distributed manner and set the following microservice parameters
#

###  EUREKA install information。
###  You can access it in your browser at the address below：http://${EUREKA_INSTALL_IP}:${EUREKA_PORT}
EUREKA_INSTALL_IP=127.0.0.1         # Microservices Service Registration Discovery Center
EUREKA_PORT=20303

###  Gateway install information
GATEWAY_INSTALL_IP=127.0.0.1
GATEWAY_PORT=9001

###  publicservice
PUBLICSERVICE_INSTALL_IP=127.0.0.1
PUBLICSERVICE_PORT=9002


### Hive Metadata Query service, provide the metadata information of Hive databases.
METADATA_INSTALL_IP=127.0.0.1
METADATA_PORT=9008


### ResourceManager
RESOURCEMANAGER_INSTALL_IP=127.0.0.1
RESOURCEMANAGER_PORT=9003


### Spark
### This service is used to provide spark capability.
SPARK_INSTALL_IP=127.0.0.1
SPARK_EM_PORT=10001
SPARK_ENTRANCE_PORT=10002


### Hive
### This service is used to provide hive capability.
HIVE_INSTALL_IP=127.0.0.1
HIVE_EM_PORT=11001
HIVE_ENTRANCE_PORT=11002


### PYTHON
### This service is used to provide python capability.
PYTHON_INSTALL_IP=127.0.0.1
PYTHON_EM_PORT=12001
PYTHON_ENTRANCE_PORT=12002


### JDBC
### This service is used to provide jdbc capability.
JDBC_INSTALL_IP=127.0.0.1
JDBC_ENTRANCE_PORT=9888


MLSQL_INSTALL_IP=127.0.0.1
MLSQL_ENTRANCE_PORT=9889


### BML
### This service is used to provide BML capability.
BML_INSTALL_IP=127.0.0.1
BML_PORT=9999

########################################################################################

## LDAP is for enterprise authorization, if you just want to have a try, ignore it.
#LDAP_URL=ldap://localhost:1389/
#LDAP_BASEDN=dc=webank,dc=com


LINKIS_VERSION=0.9.1
