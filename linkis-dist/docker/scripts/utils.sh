#!/bin/bash
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
#

download() {
  TAR_URL=$1
  TAR_FILE=$2
  HARD_LINK_ROOT=$3

  mkdir -p ${TAR_CACHE_ROOT}

  if [ ! -f ${TAR_CACHE_ROOT}/${TAR_FILE} ]; then
    echo "- downloading ${TAR_FILE} to ${TAR_CACHE_ROOT} from ${TAR_URL}"
    curl -L ${TAR_URL} -o ${TAR_CACHE_ROOT}/${TAR_FILE}
  else
    echo "- ${TAR_FILE} already exists in ${TAR_CACHE_ROOT}, downloading skipped."
  fi

  echo "- cp: ${TAR_CACHE_ROOT}/${TAR_FILE} -> ${HARD_LINK_ROOT}/${TAR_FILE} "
  rm -rf ${HARD_LINK_ROOT}/${TAR_FILE}
  # ln maybe cause invalid cross-device link
  cp  ${TAR_CACHE_ROOT}/${TAR_FILE}  ${HARD_LINK_ROOT}/${TAR_FILE}
}

WORK_DIR=`cd $(dirname $0); pwd -P`

PROJECT_ROOT=${WORK_DIR}/../..
PROJECT_TARGET=${PROJECT_ROOT}/target
TAR_CACHE_ROOT=${HOME}/.linkis-build-cache
