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
COPY lib/linkis-spring-cloud-services/linkis-mg-gateway/ /opt/linkis/linkis-mg-gateway/lib/
COPY sbin/k8s/linkis-mg-gateway.sh /opt/linkis/linkis-mg-gateway/bin/startup.sh
RUN chmod +x /opt/linkis/linkis-mg-gateway/bin/startup.sh

ENTRYPOINT ["linkis-mg-gateway/bin/startup.sh"]
