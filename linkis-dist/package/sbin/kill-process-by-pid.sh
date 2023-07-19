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

cd `dirname $0`
cd ..
INSTALL_HOME=`pwd`
# set LINKIS_HOME
if [ "$LINKIS_HOME" = "" ]; then
  export LINKIS_HOME=$INSTALL_HOME
fi
if [ "$LINKIS_LOG_DIR" = "" ]; then
  export LINKIS_LOG_DIR="$LINKIS_HOME/logs"
fi
ecmPid=`cat $LINKIS_HOME/pid/linkis_cg-engineconnmanager.pid`
month=`date '+%Y-%m'`
export killLogFile=$LINKIS_LOG_DIR/linkis-cg-engineconnmanager-kill-$month.log

if [ -f '$killLogFile' ];then
        echo "logfile exsts."
else
        echo "not exists"
        echo '' >> $killLogFile
        sudo chown hadoop:hadoop $killLogFile
        sudo chmod 777 $killLogFile

fi

function kill_proc_by_pid() {
    killProcessPID=$1
    echo "`date '+%Y-%m-%d %H:%M:%S'` Starting to kill parent process ${killProcessPID}"
    echo "`date '+%Y-%m-%d %H:%M:%S'` Starting to kill parent process ${killProcessPID}" >> $killLogFile
    #KILL PARENT FIRST
    cnt=0
    MAX_RETRY=6
    if [ "${killProcessPID}" != "${ecmPid}" ]; then
        pkill -TERM -P ${killProcessPID}
        while ps -p ${killProcessPID} > /dev/null && [ $cnt -lt $MAX_RETRY ]
        do
            kill -15 ${killProcessPID}
            let cnt++
            sleep 5s
        done
        if ps -p ${killProcessPID} > /dev/null; then
            kill -9 ${killProcessPID}
            echo "`date '+%Y-%m-%d %H:%M:%S'` Killed parent process ${killProcessPID} with \'-9\' signal. May not be able to kill gracefully(yarn application may still be alive)" 1>&2
            echo "`date '+%Y-%m-%d %H:%M:%S'` Killed parent process ${killProcessPID} with \'-9\' signal. May not be able to kill gracefully(yarn application may still be alive)"  >> $killLogFile 2>&1
        else
            echo "`date '+%Y-%m-%d %H:%M:%S'` Finished to kill parent process ${killProcessPID}"
            echo "`date '+%Y-%m-%d %H:%M:%S'` Finished to kill parent process ${killProcessPID}" >> $killLogFile 2>&1
        fi
    else
      echo "ERROR: ECM killProcessPID $ecmPid cannot be kill"
      echo "ERROR: ECM killProcessPID $ecmPid cannot be kill" >>$killLogFile 2>&1
    fi
    echo "`date '+%Y-%m-%d %H:%M:%S'` Finished killing process and its ecmPid ${ecmPid} sub-process ${killProcessPID}"
    echo "`date '+%Y-%m-%d %H:%M:%S'` Finished killing process and its ecmPid ${ecmPid} sub-process ${killProcessPID}"  >> $killLogFile  2>&1
}

kill_proc_by_pid $@