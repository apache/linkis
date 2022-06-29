# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
# 
#   http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

FROM linkis-base:1.2.0

WORKDIR /opt/linkis

COPY lib/linkis-commons/public-module/ /opt/linkis/public-module/
COPY lib/linkis-public-enhancements/linkis-ps-metadatamanager/ /opt/linkis/linkis-ps-metadatamanager/lib/
RUN curl -L -o /opt/linkis/linkis-ps-metadatamanager/lib/mysql-connector-java-5.1.49.jar https://repo1.maven.org/maven2/mysql/mysql-connector-java/5.1.49/mysql-connector-java-5.1.49.jar
RUN mkdir /opt/linkis/linkis-ps-metadatamanager/bin/
COPY sbin/k8s/linkis-ps-metadatamanager.sh /opt/linkis/linkis-ps-metadatamanager/bin/startup.sh
RUN chmod +x /opt/linkis/linkis-ps-metadatamanager/bin/startup.sh

ENTRYPOINT ["linkis-ps-metadatamanager/bin/startup.sh"]
