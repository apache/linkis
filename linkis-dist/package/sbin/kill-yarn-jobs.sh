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

# Query and kill unfinished YARN tasks based on yarnID （根据yarnID查询并杀死未结束的yarn任务）
set -x

declare -A is_unfinish_state=(\
	['ACCEPTED']=true \
	['RUNNING']=true \
	['SUBMITTED']=true \
)

declare -A kill_status
KILLED=0
UNFIN=1
FIN=2
NOTFOUND=3
SENT=4
ERR=255

STATUS_NOT_FOUND="NOT_FOUND"
STATUS_ERR="ERROR"
YARN_APP_STATUS_KILLED="KILLED"

source $LINKIS_CONF_DIR/linkis-env.sh
YARN_EXE_PATH="yarn"
if [[ -d "$HADOOP_HOME" && -d $HADOOP_HOME ]]; then
    YARN_EXE_PATH="$HADOOP_HOME/bin/yarn"
fi

function check_status() {
	yarn_id=$1
	result=`$YARN_EXE_PATH application -status ${yarn_id} 2>/dev/null`
	exitcode=$?
	if [ ${exitcode} != "0" ];then
		indicator=`echo "${result}" | grep -i -E "doesn't exist in RM" | wc -l`
		if [ ${indicator} != "0" ]; then
			# case 1: app goes to history server, which suggest job is done
			# case 2: false appid, which should not happen
			echo "`date '+%Y-%m-%d %H:%M:%S'` Cannot find yarn application id=${yarn_id}. It's probably finished and went to history server."
			return $NOTFOUND
		else 
			echo "`date '+%Y-%m-%d %H:%M:%S'` [ERROR] Could not get yarn job status, id=${yarn_id}"
			return $ERR
		fi
	else
		status=`echo "${result}" | grep -i "state" |grep -v -i "final" | awk -F ":" '{print $2}' | sed 's/^\s*//;s/\s*$//' | tr '[a-z]' '[A-Z]'`
        if [ -z "$status" ]; then 
        	echo $STATUS_ERR
        	return $ERR
        else
        	echo "`date '+%Y-%m-%d %H:%M:%S'` Yarn application id=${yarn_id}, status=${status}"
        fi

		if [ "${is_unfinish_state[${status}]}" = true ]; then
			return $UNFIN
	    elif [[ ${status} = $YARN_APP_STATUS_KILLED ]]; then
	    	echo "`date '+%Y-%m-%d %H:%M:%S'` Yarn application id=${yarn_id} has already finished(status=SUCCEEDED/KILLED/FAILED)."
	    	return $KILLED
	    else
	    	echo "`date '+%Y-%m-%d %H:%M:%S'` Yarn application id=${yarn_id} has already finished(status=SUCCEEDED/KILLED/FAILED)."
	    	return $FIN
	    fi
	fi
}

function try_kill() {
	yarn_id=$1
	check_status "$yarn_id"
	exitcode=$?
	if (( $exitcode != $ERR  )) && (( $exitcode == $UNFIN )); then
        do_kill ${yarn_id}
        return $?
	fi
	return $exitcode
}

function do_kill(){
	yarn_id=$1
	echo "`date '+%Y-%m-%d %H:%M:%S'` Starting to kill yarn application id=${yarn_id}"
	result=`$YARN_EXE_PATH application -kill ${yarn_id} 2>/dev/null`
	exitcode=$?
	if (( ${exitcode} != 0  )) ; then
		indicator0=`echo "${result}" | grep -i -E "has already been killed|has already succeeded|has already failed|has already finished" | wc -l`
		indicator1=`echo "${result}" | grep -i -E "doesn't exist in RM" | wc -l`
		if [ ${indicator0} != "0" ]; then
			echo "`date '+%Y-%m-%d %H:%M:%S'` Yarn application id=${yarn_id} has alread finished(status=SUCCEEDED/KILLED/FAILED)."
			return $FIN
		elif [ ${indicator1} != "0" ];then
			echo "`date '+%Y-%m-%d %H:%M:%S'` Could not find  yarn application id=${yarn_id}"
			return $NOTFOUND
		else
			echo "`date '+%Y-%m-%d %H:%M:%S'` [ERROR] Could not kill yarn application id=${yarn_id}."
			return $ERR
		fi
	else
		echo "`date '+%Y-%m-%d %H:%M:%S'` Successfully send kill to yarn for application id=${yarn_id}."
		return $SENT
	fi
}

function wait_kill_complete() {
	retryCnt=0
	maxRetry=300
	ids="$@"
	num_all=$#
	num_done=0
	while (( $retryCnt < $maxRetry )); do
		if (( $num_done >= $num_all )); then
			return 0
		fi
		sleep 2s
		((retryCnt++))
		for yarn_id in ${ids[@]}
		do
			if [[ ${kill_status[${yarn_id}]} = $FIN ]] || [[ ${kill_status[${yarn_id}]} = $NOTFOUND ]] || [[ ${kill_status[${yarn_id}]} = $KILLED ]]; then
				continue
			fi
	        check_status "$yarn_id"
			exitcode=$?
			let kill_status[${yarn_id}]=$exitcode
			if [[ $exitcode = $KILLED ]] || [[ $exitcode = $FIN ]] || [[ $exitcode = $NOTFOUND ]]; then
				((num_done++))
		    fi
		done
	done
	echo "`date '+%Y-%m-%d %H:%M:%S'` After 10mins. Yarn application id=${yarn_id} is still not complete. Kill Failed."
	return 0
}

function do_kill_yarn_ids() {

	i=0
	ids="$@"
	for yarn_id in ${ids[@]}
	do
		try_kill "${yarn_id}"
		exitcode=$?
		let kill_status[${yarn_id}]=$exitcode
		if (( $exitcode == $SENT )); then
			kill_sent[$i]=$yarn_id
			((i++))
		fi
	done

	if (( $i != 0)); then
		wait_kill_complete "${kill_sent[@]}"
		exitcode=$?

		for yarn_id in "${kill_sent[@]}"
		do
			if [[ ${kill_status[${yarn_id}]} == $KILLED ]]; then
		        echo "`date '+%Y-%m-%d %H:%M:%S'` Successfully killed yarn application id=${yarn_id}."
	        elif [[ ${kill_status[${yarn_id}]} == $FIN ]]; then
				echo "`date '+%Y-%m-%d %H:%M:%S'` Yarn application id=${yarn_id} has already finished(status=SUCCEEDED/KILLED/FAILED)."
			elif [[ ${kill_status[${yarn_id}]} == $NOTFOUND ]]; then
				echo "`date '+%Y-%m-%d %H:%M:%S'` Cannot find yarn application id=${yarn_id}. It's probably finished and went to history server."
			fi
		done
	fi

	for yarn_id in ${ids[@]}
	do
	    if [[ ${kill_status[${yarn_id}]} == $ERR ]];then
	    	exit -1
		fi
	done

	exit 0
}

function timeout(){

    wait_for=$1

    shift && command="$@"

    $command &

    command_pid=$!

    (sleep ${wait_for}; kill_by_pid ${command_pid} )&

    watchdog=$!

    sleep 2s 

    sleep_pid=`ps --ppid ${watchdog} | awk '{ if(NR>1) print $1;}'`

    wait ${command_pid} > /dev/null 2>&1
    exit_code=$?

    if [ -z ${sleep_pid} ]; then
        exit 0
    fi

    if kill -0 ${sleep_pid} > /dev/null 2>&1; then     #command terminate normally
        kill $sleep_pid > /dev/null 2>&1
        if [ ${exit_code} -ne 0 ]; then
            exit ${exit_code}
        fi
    else
        echo "Fail to run kill_yarn_jobs: Timeout when killing yarn jobs."
        exit 1
    fi
}

function run_kill_with_timeout(){
    if grep '^[[:digit:]]*$' <<< "$1" > /dev/null 2>&1;then
        wait_for=$1
        shift && proc_name="$@"
    else
        wait_for=600
        proc_name="$@"
    fi

    timeout ${wait_for} do_kill_yarn_ids $@
}

function kill_by_pid() {
    pid=$1
    sub_pid_list=`pstree -p ${pid} | awk -F "[()]" '{for(i=0;i<=NF;i++) if ($i~/^[0-9]+$/)print $i}'`

    echo "`date '+%Y-%m-%d %H:%M:%S'` Starting to kill process ${pid}"
    if kill -0 ${pid} > /dev/null 2>&1; then
        kill ${pid}
    fi
    sleep 2
    if kill -0 ${pid} > /dev/null 2>&1; then
        sleep 5
        kill -9 ${pid}
    fi
    echo "`date '+%Y-%m-%d %H:%M:%S'` Killed process ${pid}"

    for sub_pid in ${sub_pid_list[@]}; do
        if kill -0 ${sub_pid} > /dev/null 2>&1; then
            kill -9 ${sub_pid}
            echo "`date '+%Y-%m-%d %H:%M:%S'` Killed subprocess ${sub_pid}"
        fi
    done

    echo "`date '+%Y-%m-%d %H:%M:%S'` Finished killing process and its subprocess ${pid}"
}


run_kill_with_timeout $@