#
#  Copyright 2019 WeBank
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

FROM chongchuanbing/linkis:emr-base-spark2.4.4
MAINTAINER brian.rongzhang@gmail.com
RUN yum install -y unzip
WORKDIR /opt/ihome

COPY target/linkis-ujes-spark-enginemanager.zip /opt/ihome
RUN unzip linkis-ujes-spark-enginemanager.zip

WORKDIR /opt/ihome/linkis-ujes-spark-enginemanager/bin
ENTRYPOINT ["/opt/ihome/linkis-ujes-spark-enginemanager/bin/startup.sh"]