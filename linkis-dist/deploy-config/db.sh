#!/usr/bin/env bash
#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
# http://www.apache.org/licenses/LICENSE-2.0
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

### Used to store user's custom variables, user's configuration, UDFs and functions, while providing the JobHistory service
MYSQL_HOST=
MYSQL_PORT=
MYSQL_DB=
MYSQL_USER=
MYSQL_PASSWORD=

PG_HOST=
PG_PORT=
PG_DB=
PG_SCHEMA=
PG_USER=
PG_PASSWORD=

### Provide the DB information of Hive metadata database.
### Attention! If there are special characters like "&", they need to be enclosed in quotation marks.
HIVE_META_URL=""
HIVE_META_USER=""
HIVE_META_PASSWORD=""

### define openLookeng  parameters for connection.
OLK_HOST=
OLK_PORT=
OLK_CATALOG=
OLK_SCHEMA=
OLK_USER=
OLK_PASSWORD=

### define Presto parameters for connection.
PRESTO_HOST=
PRESTO_PORT=
PRESTO_CATALOG=
PRESTO_SCHEMA=

### define impala parameters for connection.
IMPALA_HOST=
IMPALA_PORT=

### define trino parameters for connection.
TRINO_COORDINATOR_HOST=
TRINO_COORDINATOR_PORT=
TRINO_COORDINATOR_CATALOG=
TRINO_COORDINATOR_SCHEMA=

### define seatunnel parameters for connection.
SEATUNNEL_HOST=
SEATUNNEL_PORT=