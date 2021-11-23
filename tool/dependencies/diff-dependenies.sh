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

workDir=$(cd "$(dirname "$0")"; pwd)
cd ${workDir}; echo "enter work dir:$(pwd)"

libPath=../../assembly-combined-package/assembly-combined/target/apache-linkis-*-combined-dist/lib

if [ ! -d $libPath ];then
    echo "directory[$libPath] is not exist ,please check it!!"
    exit 1
fi
#find   $libPath  -type f -name "*.jar" ! -name "linkis-*.jar" -printf "%f\n"
find   $libPath  -type f -name "*.jar" ! -name "linkis-*.jar" -printf "%f\n" |sort -u >third-party-dependencies.txt

#echo "find dependencies:"
#echo "===============libs================="
#cat third-party-dependencies.txt
#echo "===================================="

#diff -a -w -B -U0 <(sort < known-dependencies.txt) <(sort < third-party-dependencies.txt)
echo "===============diff result================="
diff -a -w -B -U0   known-dependencies.txt third-party-dependencies.txt