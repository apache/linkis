#!/usr/bin/env bash
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

basepath=$(cd `dirname $0`; pwd)
dependencies_path=$basepath/all_dependencies
if [[ -d $dependencies_path ]];then
  echo "rm -r -f dependencies_path"
  rm -r -f $dependencies_path
fi
cd ../../
mvn dependency:copy-dependencies -DexcludeGroupIds=org.apache.linkis -DincludeScope=runtime -DoutputDirectory=$dependencies_path
ls $dependencies_path | sort -n > $basepath/known-dependencies.txt
rm -r -f $dependencies_path