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

function list_descendants ()
{
  ppid=$1
  local children=$(pgrep -P ${ppid})

  for spid in $children
  do
    list_descendants "$spid"
  done

  sub_pid_list=(${sub_pid_list[@]} ${children[@]})
}
function kill_proc_by_pid() {
    pid=$1
    sub_pid_list=()

    echo "`date '+%Y-%m-%d %H:%M:%S'` Starting to kill parent process ${pid}"
    #KILL PARENT FIRST
    cnt=0
    MAX_RETRY=6
    while kill -0 ${pid} > /dev/null 2>&1 && [ $cnt -lt $MAX_RETRY ]
    do
        list_descendants ${pid}
        kill -15 ${pid}
        let cnt++
        sleep 5s
    done
    if kill -0 ${pid} > /dev/null 2>&1; then
        list_descendants ${pid}
        kill -9 ${pid}
        echo "`date '+%Y-%m-%d %H:%M:%S'` Killed parent process ${pid} with \'-9\' signal. May not be able to kill gracefully(yarn application may still be alive)" 1>&2
    else
        echo "`date '+%Y-%m-%d %H:%M:%S'` Finished to kill parent process ${pid}"
    fi

    #REMOVE DUPLICATE
    sub_pid_list=($(echo ${sub_pid_list[*]} | sed 's/ /\n/g' | sort | uniq))
    echo "sub_pid_list:${sub_pid_list[*]}"
    #THEN KILL CHILDREN
    for sub_pid in ${sub_pid_list[@]}; do
        cnt=0
        MAX_RETRY=6
        while kill -0 ${sub_pid} > /dev/null 2>&1 && [ $cnt -lt $MAX_RETRY ]
        do
            kill -15 ${sub_pid}
            echo "to Killed sub-process ${sub_pid}"
            let cnt++
            sleep 5s
            if kill -0 ${sub_pid} > /dev/null 2>&1; then
              let 1+1
            else
              echo "`date '+%Y-%m-%d %H:%M:%S'` Killed sub-process ${sub_pid}"
            fi
        done
        if kill -0 ${sub_pid} > /dev/null 2>&1; then
            kill -9 ${sub_pid}
            echo "`date '+%Y-%m-%d %H:%M:%S'` Killed sub-process ${sub_pid} with \'-9\' signal. May not be able to kill gracefully(yarn application may still be alive)" 1>&2
        fi
    done

    echo "`date '+%Y-%m-%d %H:%M:%S'` Finished killing process and its sub-process ${pid}"
}


kill_proc_by_pid $@