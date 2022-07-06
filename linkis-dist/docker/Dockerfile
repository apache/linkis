#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

######################################################################
# linkis image
######################################################################

ARG BASE_IMAGE=centos:7

FROM ${BASE_IMAGE} as linkis

ARG JDK_VERSION=1.8.0-openjdk
ARG JDK_BUILD_REVISION=1.8.0.332.b09-1.el7_9
ARG LINKIS_VERSION=1.1.3
ARG MYSQL_JDBC_VERSION=5.1.49
ARG BUILD_TYPE=dev
ARG LINKIS_SYSTEM_USER="hadoop"
ARG LINKIS_SYSTEM_UID="9001"

WORKDIR /opt/linkis

RUN useradd -r -s /bin/bash -u ${LINKIS_SYSTEM_UID} -g root -G wheel ${LINKIS_SYSTEM_USER}

# TODO: remove install mysql client when schema-init-tools is ready
RUN yum install -y \
       vim unzip curl sudo krb5-workstation sssd crontabs python-pip \
       java-${JDK_VERSION}-${JDK_BUILD_REVISION} \
       java-${JDK_VERSION}-devel-${JDK_BUILD_REVISION} \
       mysql \
    && yum clean all

RUN mkdir -p /opt/tmp \
    && mkdir -p /etc/linkis-conf \
    && mkdir -p /var/logs/linkis

ENV JAVA_HOME /etc/alternatives/jre
ENV LINKIS_CONF_DIR /etc/linkis-conf
ENV LINKIS_HOME /opt/linkis

ADD apache-linkis-${LINKIS_VERSION}-incubating-bin.tar.gz /opt/tmp/

# Put mysql-connector-java-*.jar package into the image only in development mode
RUN if [ "$BUILD_TYPE" = "dev" ] ; then \
      curl -L -o /opt/tmp/mysql-connector-java-${MYSQL_JDBC_VERSION}.jar \
        https://repo1.maven.org/maven2/mysql/mysql-connector-java/${MYSQL_JDBC_VERSION}/mysql-connector-java-${MYSQL_JDBC_VERSION}.jar; \
    fi

RUN mv /opt/tmp/linkis-package/* /opt/linkis/ \
    && cp /opt/tmp/mysql-connector-java-${MYSQL_JDBC_VERSION}.jar /opt/linkis/lib/linkis-commons/public-module/ \
    && cp /opt/tmp/mysql-connector-java-${MYSQL_JDBC_VERSION}.jar /opt/linkis/lib/linkis-spring-cloud-services/linkis-mg-gateway/ \
    && chmod g+w -R /opt/linkis && chown ${LINKIS_SYSTEM_USER}:${LINKIS_SYSTEM_GROUP} -R /opt/linkis \
    && chmod g+w -R /etc/linkis-conf && chown ${LINKIS_SYSTEM_USER}:${LINKIS_SYSTEM_GROUP} -R /etc/linkis-conf  \
    && chmod g+w -R /var/logs/linkis && chown ${LINKIS_SYSTEM_USER}:${LINKIS_SYSTEM_GROUP} -R /var/logs/linkis \
    && chmod a+x /opt/linkis/bin/* \
    && chmod a+x /opt/linkis/sbin/* \
    && rm -rf /opt/tmp

USER ${LINKIS_SYSTEM_USER}

ENTRYPOINT ["/bin/bash"]


######################################################################
# linkis web image
# TODO: refactor
######################################################################
FROM nginx:1.19.6 as linkis-web
COPY web/dist/dist /usr/share/nginx/html
