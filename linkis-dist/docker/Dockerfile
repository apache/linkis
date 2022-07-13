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

ARG IMAGE_BASE=centos:7
ARG IMAGE_BASE_WEB=nginx:1.19.6

FROM ${IMAGE_BASE} as linkis

ARG BUILD_TYPE=dev

ARG JDK_VERSION=1.8.0-openjdk
ARG JDK_BUILD_REVISION=1.8.0.332.b09-1.el7_9

ARG MYSQL_JDBC_VERSION=5.1.49

ARG LINKIS_VERSION=0.0.0
ARG LINKIS_SYSTEM_USER="hadoop"
ARG LINKIS_SYSTEM_UID="9001"

ARG LINKIS_HOME=/opt/linkis
ARG LINKIS_CONF_DIR=/etc/linkis-conf
ARG LINKIS_LOG_DIR=/var/logs/linkis

WORKDIR ${LINKIS_HOME}

RUN useradd -r -s /bin/bash -u ${LINKIS_SYSTEM_UID} -g root -G wheel ${LINKIS_SYSTEM_USER}

# TODO: remove install mysql client when schema-init-tools is ready
RUN yum install -y \
       vim unzip curl sudo krb5-workstation sssd crontabs python-pip \
       java-${JDK_VERSION}-${JDK_BUILD_REVISION} \
       java-${JDK_VERSION}-devel-${JDK_BUILD_REVISION} \
       mysql \
    && yum clean all

RUN sed -i "s#^%wheel.*#%wheel        ALL=(ALL)       NOPASSWD: ALL#g" /etc/sudoers

RUN mkdir -p /opt/tmp \
    && mkdir -p ${LINKIS_CONF_DIR} \
    && mkdir -p ${LINKIS_LOG_DIR}

ENV JAVA_HOME /etc/alternatives/jre
ENV LINKIS_CONF_DIR ${LINKIS_CONF_DIR}
ENV LINKIS_CLIENT_CONF_DIR ${LINKIS_CONF_DIR}
ENV LINKIS_HOME ${LINKIS_HOME}

ADD apache-linkis-${LINKIS_VERSION}-incubating-bin.tar.gz /opt/tmp/

RUN mv /opt/tmp/linkis-package/* ${LINKIS_HOME}/ \
    && rm -rf /opt/tmp

# Put mysql-connector-java-*.jar package into the image only in development mode
RUN if [ "$BUILD_TYPE" = "dev" ] ; then \
      curl -L -o ${LINKIS_HOME}/lib/linkis-commons/public-module/mysql-connector-java-${MYSQL_JDBC_VERSION}.jar \
        https://repo1.maven.org/maven2/mysql/mysql-connector-java/${MYSQL_JDBC_VERSION}/mysql-connector-java-${MYSQL_JDBC_VERSION}.jar \
      && cp ${LINKIS_HOME}/lib/linkis-commons/public-module/mysql-connector-java-${MYSQL_JDBC_VERSION}.jar ${LINKIS_HOME}/lib/linkis-spring-cloud-services/linkis-mg-gateway/ ;\
    fi

RUN chmod g+w -R ${LINKIS_HOME} && chown ${LINKIS_SYSTEM_USER}:${LINKIS_SYSTEM_GROUP} -R ${LINKIS_HOME} \
    && chmod g+w -R ${LINKIS_CONF_DIR} && chown ${LINKIS_SYSTEM_USER}:${LINKIS_SYSTEM_GROUP} -R ${LINKIS_CONF_DIR}  \
    && chmod g+w -R ${LINKIS_LOG_DIR} && chown ${LINKIS_SYSTEM_USER}:${LINKIS_SYSTEM_GROUP} -R ${LINKIS_LOG_DIR} \
    && chmod a+x ${LINKIS_HOME}/bin/* \
    && chmod a+x ${LINKIS_HOME}/sbin/*

USER ${LINKIS_SYSTEM_USER}

ENTRYPOINT ["/bin/bash"]


######################################################################
# linkis web image
######################################################################
FROM ${IMAGE_BASE_WEB} as linkis-web

ARG LINKIS_VERSION=0.0.0
ARG LINKIS_HOME=/opt/linkis

ENV LINKIS_WEB_ROOT ${LINKIS_HOME}-web

RUN mkdir -p ${LINKIS_HOME}-web
COPY apache-linkis-web-${LINKIS_VERSION}-dist/dist ${LINKIS_HOME}-web
