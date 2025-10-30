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

source /appcom/Install/linkis/conf/linkis-env.sh

if [ "$LINKIS_LOG_DIR" = "" ]; then
  export LINKIS_LOG_DIR="/data/logs/bdpe-ujes"
fi
# Set LINKIS_HOME
if [ -z "$LINKIS_HOME" ]; then
  export LINKIS_HOME="$INSTALL_HOME"
fi

# Set LINKIS_CONF_DIR
if [ -z "$LINKIS_CONF_DIR" ]; then
  export LINKIS_CONF_DIR="$LINKIS_HOME/conf"
fi

# Read configuration
linkisMainConf="$LINKIS_CONF_DIR/linkis.properties"

linkisLogToolPath="$LINKIS_HOME/admin/tools/linkis-log-tool.sh"
# 任务id
job_id=$1
task_path="$LINKIS_LOG_DIR/task"
json_path="$LINKIS_LOG_DIR/task/json"
gateway_url=$(grep wds.linkis.gateway.url $linkisMainConf | cut -d"=" -f2)
doctor_token=$(grep linkis.doctor.signature.token $linkisMainConf | cut -d"=" -f2)
doctor_url=$(grep linkis.doctor.url $linkisMainConf | cut -d"=" -f2)
system_name=$(grep linkis.system.name $linkisMainConf | cut -d"=" -f2)
# 定义全局历史接口url
export task_list_result_url="$gateway_url/api/rest_j/v1/jobhistory/list?taskID=$job_id&pageNow=1&pageSize=50&isAdminView=true"

# 引擎常量(用于判断是否已经提交到引擎)
submit_engine_constants="Task submit to ec"
# 日志路径(用于查看ec端日志)
engine_local_log_path="EngineConn local log path"
# 链路日志路径
link_log_path="$LINKIS_LOG_DIR"
# 链路日志前缀
linkis_cg="linkis-cg-"
hive_contact="冯朝阁"
spark_contact="冯朝阁"
jdbc_contact="邵宇"
engine_type="" 
# linkis token
token=$(grep wds.linkis.token $linkisMainConf | cut -d"=" -f2)

if [ ! -d $task_path ]; then
  mkdir $task_path
fi

if [ ! -d $json_path ]; then
  mkdir $json_path
fi

# 根据key获取值
function getValueForTasks() {
  value=$(echo "$1" | jq -r ".data.tasks[0].$2")
  echo "$value"
}

function check_tasks_stauts() {
  code=$(echo "$task_list_result" | jq -r ".status")
  if [ "$code" -ne 0 ]; then     # 判断code是否等于0
    echo "获取日志失败，无法执行诊断"
    exit 0
  fi
}

function getValueForDetail() {
  echo $(echo $1 | python -c "import sys, json; print (json.load(sys.stdin)['data'])")
}

function getValueForDoctor() {
  echo $(echo $1 | python -c "import sys, json; print (json.load(sys.stdin)['$2'])")
}

function getDoctorReport() {
  code=$(echo $1 | jq -r ".code" 2>/dev/null || echo "0")
  if [ "$code" -eq 200 ]; then     # 判断code是否等于200
    echo $(echo $1| jq -r ".data.$2[] | select(.conclusion != null and .conclusion.conclusion != \"未检测到异常\" and .conclusion.conclusion != \"运行过程发生错误异常,请根据关键日志和相应的诊断建议进行问题修改\" and .conclusion.conclusion != \"No exception detected\")")
  else
    echo "调用Doctoris失败，无法执行Doctoris诊断，异常日志信息：$1"
    exit 0
  fi
}

function print_color_green(){
  echo -e "\e[32m $1 \e[0m"$2
}

function print_color_red(){
  echo -e "\e[31m $1 \e[0m"$2
}

function print_color_black(){
  echo -e "\e[30m $1 \e[0m"$2
}

function getApplcationId() {
  echo $(cat "$task_path/json/$job_id"_detail.json | grep -oP 'application_\d+_\d+'| tail -n 1)
}


# 全局历史任务列表接口
function get_job_list_by_id() {
  # 将根据任务id查询出来的结果保存起来
  result=$(curl -s -X GET --header "Token-Code: $token" --header "Token-User: hadoop" --header 'Accept: application/json' $task_list_result_url)
  echo "$result"
}

# 日志明细查询
function get_job_detail_log() {
  log_path=$(getValueForTasks "$1" logPath)
  # 定义openLog接口url
  open_log_url="$gateway_url/api/rest_j/v1/filesystem/openLog?path=$log_path"
  # 调用openLog接口
  echo $(curl -s -X GET --header "Token-Code: $token" --header "Token-User: hadoop" --header 'Accept: application/json' $open_log_url)
}

function get_log_info(){
  echo $(cat $task_path"/json/$job_id"_detail.json | grep -a "$1"| uniq | tail -n 1)
}

function get_time_info(){
  echo $(echo "$1"| grep -oP '^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d{3}'| uniq | tail -n 1)
}

function get_instance_info(){
  echo $(echo "$1" | grep -oP 'ServiceInstance\((.*?)(?=\))\)'| uniq | tail -n 1)
}

function get_doctoris_signature(){
  doctor_signature_map=$(java -cp $LINKIS_HOME/lib/linkis-commons/public-module/linkis-common-1.10.0-wds.jar:$LINKIS_HOME/lib/linkis-commons/public-module/* org.apache.linkis.common.utils.SHAUtils $1 $system_name $doctor_token)
  echo $(echo "$doctor_signature_map")
}

# doctor诊断接口
function get_doctor_diagnose() {
  # 将根据任务id查询出来的结果保存起来
  task_list_result=$(get_doctoris_signature "$1")
  # 提取 applicationId
  nonce=$(echo "$task_list_result" | grep -oP 'nonce=(.*?),' | sed 's/nonce=//'| sed 's/,/ /g'| xargs)
  appId=$(echo "$task_list_result" | grep -oP 'app_id=(.*?),' | sed 's/app_id=//'| sed 's/,/ /g'| xargs)
  timestamp=$(echo "$task_list_result" | grep -oP 'timestamp=(.*$)' | sed 's/timestamp=//'| sed 's/}/ /g'| xargs)
  signature=$(echo "$task_list_result" | grep -oP 'signature=(.*?),' | sed 's/signature=//'| sed 's/,/ /g'| xargs)
  doctor_request="$doctor_url/api/v1/external/diagnose/offline/batchApp?applicationId=$1&app_id=$appId&timestamp=$timestamp&nonce=$nonce&signature=$signature"
  doctor_result=$(curl -s -X GET --header 'Accept: application/json' "$doctor_request")
  doctor_report_url="$doctor_url/api/v1/external/diagnose/offline/report?applicationId=$1&app_id=$appId&timestamp=$timestamp&nonce=$nonce&signature=$signature"
  doctor_report_result=$(curl -s -X GET --header 'Accept: application/json' "$doctor_report_url")
  resourcesAnalyze=$(getDoctorReport "$doctor_report_result" "resourcesAnalyze")
  runErrorAnalyze=$(getDoctorReport "$doctor_report_result" "runErrorAnalyze")
  runTimeAnalyze=$(getDoctorReport "$doctor_report_result" "runTimeAnalyze")
  print_color_black "ResourcesAnalyze诊断结果:$(if [ -z "$resourcesAnalyze" ]; then echo "无异常"; else echo "$resourcesAnalyze"; fi)"
  print_color_black "RunErrorAnalyze诊断结果:$(if [ -z "$runErrorAnalyze" ]; then echo "无异常"; else echo "$runErrorAnalyze"; fi)"
  print_color_black "RunTimeAnalyze诊断结果:$(if [ -z "$runTimeAnalyze" ]; then echo "无异常"; else echo "$runTimeAnalyze"; fi)"
}

# -------------------------------------------------------------提供一些帮助查看命令-------------------------------------------------------
help() {
  echo "<-----------------------下面是一些简单命令------------------------------------>"
  echo "NAME"
  echo "    linkis log query tool"
  echo ""
  echo "    linkis-analyze -job jobid info,             查看job 的基础信息 （请求参数 日志目录 等）"
  echo ""
  echo "    linkis-analyze -job jobid info,             查看job相关的日志"
  echo ""
  echo "    linkis-analyze -job jobid ecinfo,           查看job ec日志"
  exit 1
}

# 查看job 的基础信息 （请求参数 日志目录 等）
info() {
  #
  echo "【日志目录说明】"
  echo "【错误日志】$task_path/error/JobId-"$1"_error.log"
  echo "【明细接口返回】$task_path/json/JobId-"$1"_detail.log"
  echo "【链路日志】$task_path/link/JobId-"$1"_entrance.log"
  echo "【链路日志】$task_path/link/JobId-"$1"_linkismanager.log"
  echo "【ec日志】$task_path/ec/JobId-"$1"_engine.log"
}

# 查看job相关的日志
log() {
  echo "【job相关的日志】"
}

# 查看job ec日志
eclog() {
  echo "【ec日志】"
  job_id=$1
  task_list_result_url="$gateway_url/api/rest_j/v1/jobhistory/list?taskID=$1&pageNow=1&pageSize=50&isAdminView=false"
  task_list_result_ec=$(echo $(curl -X GET --header "Token-Code: $token" --header "Token-User: hadoop" --header 'Accept: application/json' $task_list_result_url))
  instance_ec=$(getValueForTasks "$task_list_result_ec" instance)
  instance_arr_ec=(${instance//:/ })
  open_log_result_ec=$(get_job_detail_log "$task_list_result_ec")
  log_detail_ec=$(getValueForDetail "$open_log_result_ec" log)
  echo -e "$log_detail_ec" >$task_path"/json/JobId-"$job_id"_detail.log"

  local_log_path=$(cat $task_path"/json/JobId-$job_id"_detail.log | grep "$engine_local_log_path")
  local_log_path_arr=(${local_log_path//:/ })
  thirdToLastIndex=$((${#local_log_path_arr[@]} - 3))
  server_name=${local_log_path_arr[thirdToLastIndex]}
  lastIndex=$((${#local_log_path_arr[@]} - 1))
  log_path=${local_log_path_arr[lastIndex]}

  echo "【ec服务地址】"$server_name
  echo "【ec日志路径】"$log_path
}

option() {
  while [ -n "$1" ]; do
    case $1 in
    -h)
      help
      break
      ;;
    -help)
      help
      break
      ;;
    -job)
      if [ $3 == "info" ]; then
        info $2
      elif [ $3 == "log" ]; then
        log $2
      elif [ $3 == "eclog" ]; then
        eclog $2
      elif [ $3 == "h" ]; then
        help
      elif [ $3 == "help" ]; then
        help
      else
        echo $3": unknow command"
      fi
      break
      ;;
    *)
      echo $1": unknow option"
      break
      ;;
    esac
  done
}

if [ $# -eq 1 ] && [ $1 == "-help" -o $1 == "-h" ]; then
  option $*
  exit 1
fi

if [ $# -eq 3 ]; then
  option $*
  exit 1
fi

# --------------------------------------------------------------------根据jobid拉取日志到本地-------------------------------------------------------
# 参数检查
function check() {
  if [ $# -ne 1 ]; then
    echo "请输入任务id"
    exit 1
  fi
  # 校验第一个参数合法性，只能是数字
  expr $1 + 0 &>/dev/null
  if [ $? -ne 0 ]; then
    echo "请输入合法的任务id,任务id只能是数字！"
    exit 1
  fi
}

# 检查接口状态
function check_status() {
  code=$(getValueForTasks "$task_list_result" status)
  # 接口返回成功直接返回
  if [[ $code -ne 'Failed' ]]; then
    echo "任务已成功执行，不需要分析日志"
    exit 1
  elif [[ -z $code ]]; then
    echo "不存在的任务，请检查下任务id"
    exit 1
  fi
}

function remote_module_log() {
  module_ip_map=$1
  for module_name in ${!module_ip_map[@]}; do
    hostname=${module_ip_map[$module_name]}
    if [[ $hostname ]]; then
      # 脚本调用 获取远端链路日志
      source $linkisLogToolPath $hostname $job_id $link_log_path 1 $module_name

      if [ $? == 1 ]; then
        exit 0
      fi

      print_color_green "服务日志所在路径：" "$hostname:$port_id($link_log_path)"
      print_color_green "获取的完整日志见：" "$task_path/$job_id/$module_name_$server_name".log
      error_log=$(cat $task_path$job_id/$module_name"_$server_name".log | grep "ERROR")
      if [[ $error_log ]]; then
        print_color_green "$module_name异常日志信息如下："
      fi
      print_color_red "$error_log"
    fi
  done
}

# 获取远程EC日志
function remote_access_to_ec_logs() {
  # 获取local_log_path
  local_log_path=$(cat $task_path"/json/$job_id"_detail.json | grep -a "$engine_local_log_path")
  # 2023-08-12 12:02:32.002 INFO EngineConn local log path: ServiceInstance(linkis-cg-engineconn, gz.xg.bdpdws110001.webank:21715) /data/bdp/linkis/hadoop/20230812/shell/663e2ca0-f5df-42e1-b0b1-34728373eabc/logs 2023-08-12 12:02:32.002 INFO EngineConn local log path: ServiceInstance(linkis-cg-engineconn, gz.xg.bdpdws110001.webank:21715) /data/bdp/linkis/hadoop/20230812/shell/663e2ca0-f5df-42e1-b0b1-34728373eabc/logs
  local_log_path_arr=(${local_log_path//:/ })
  # 数据格式见日志工具脚本使用文档：http://docs.weoa.com/docs/Jbve3vgjEN8zCpT2
  # 获取倒数第三个元素下标
  thirdToLastIndex=$((${#local_log_path_arr[@]} - 3))
  # 服务名
  server_name=${local_log_path_arr[thirdToLastIndex]}
  port_id_s=${local_log_path_arr[$((${#local_log_path_arr[@]} - 2))]}
  port_id=${port_id_s%)*}
  # 最后一个元素下标
  lastIndex=$((${#local_log_path_arr[@]} - 1))
  # 日志地址
  log_path=${local_log_path_arr[lastIndex]}
  title="步骤3：引擎请求成功，任务提交给底层运行"
  # 脚本调用 获取远端ec日志
  source $linkisLogToolPath $server_name $job_id $log_path 0
  error_log=$(cat $task_path/$job_id/engineconn_"$server_name".log | grep "ERROR")
  if [[ $error_log ]]; then
      print_color_red "$title"
      print_color_red "对应的引擎日志所在路径为："$server_name:$port_id"($log_path)"
      print_color_red "异常日志信息如下："
      print_color_red "$(cat $task_path/$job_id/engineconn_"$server_name".log | grep "ERROR")"
      engine_type_prefix=$(echo "$engine_type" | cut -d '-' -f 1)
      case $engine_type_prefix in
        hive)
          contact_user="$hive_contact"
          ;;
        spark)
          contact_user="$spark_contact"
          ;;
        jdbc)
          contact_user="$jdbc_contact"
          ;;
      esac
      print_color_black "可能是您的代码语法错误或者底层执行异常，可以联系$engine_type_prefix运维人员（$contact_user）进行处理"
  else
      print_color_green "$title"
      print_color_green "对应的引擎日志所在路径为：" "$server_name:$port_id($log_path)"
  fi
  application_id=$(cat "$task_path/json/$job_id"_detail.json | grep -a -oP 'application_\d+_\d+'| tail -n 1)
  print_color_green "Yarn appid：" "$application_id"
  if [ -n "$application_id" ]; then
      # 检查 doctor_token 是否为空
      if [ -z "$doctor_token" ]; then
          echo "doctor_token 参数为空，无法执行doctor诊断"
          exit 0
      fi
      # 检查 doctor_url 是否为空
      if [ -z "$doctor_url" ]; then
          echo "doctor_url 参数为空，无法执行doctor诊断"
          exit 0
      fi
        print_color_green "Doctoris诊断报告："
      get_doctor_diagnose $application_id
  else
    print_color_black "Yarn appid为空，不执行Doctoris诊断"
  fi
}

# 获取远端链路日志
function remote_access_to_link_logs() {
  # 格式："instance": "bdpujes110003:9205",
  instance=$(getValueForTasks "$task_list_result" instance)
  instance_arr=(${instance//:/ })
  servername=${instance_arr[0]}
  port_id=${instance_arr[1]}

  # ip: bdpdws110002 ,port: 9101 ,serviceKind: linkis-cg-linkismanager ,ip: bdpujes110003 ,port: 9205 ,serviceKind: linkis-cg-entrance
  error_log_str=$(cat $task_path/json/$job_id"_detail.json" | grep "ERROR")
  error_log=${error_log_str/\"/}
  if [[ $error_log ]]; then
    ip_array=(${error_log//ip: /})
    ip_contain=",bdp"
    ip_arr=()
    # 构造ip数组：bdpdws110002 bdpujes110003 bdpdws110002 bdpujes110003
    for i in "${!ip_array[@]}"; do
      if [[ ${ip_array[i]} =~ $ip_contain ]]; then
        ip_s1=${ip_array[i]}
        ip_s2=${ip_s1/\'/}
        ip_s3=${ip_s2/,/}
        ip=${ip_s3/\}/}
        ip_arr[${#ip_arr[*]} + 1]=$ip
      fi
    done

    linkis_cg_contain=",linkis-cg-"
    module_array=(${error_log//serviceKind: /})
    # 构造模块数组：linkis-cg-linkismanager linkis-cg-entrance linkis-cg-linkismanager linkis-cg-entrance
    module_name_arr=()
    for i in "${!module_array[@]}"; do
      if [[ ${module_array[i]} =~ $linkis_cg_contain ]]; then
        module_name_s1=${module_array[i]}
        # ,linkis-cg-linkismanager, 去掉linkis-cg-之前的字符
        module_name_s2=${module_name_s1#*$linkis_cg}
        module_name_s3=${module_name_s2/\'/}
        module_name_s4=${module_name_s3/,/}
        module_name=${module_name_s4/\}/}
        module_name_arr[${#module_name_arr[*]} + 1]=$linkis_cg$module_name
      fi
    done
    # 构建一个map key:module;value:ip
    declare -A module_ip_map
    for ((i = 1; i <= ${#module_name_arr[@]}; i++)); do
      module_name=${module_name_arr[i]}
      ip=${ip_arr[i]}
      module_ip_map[$module_name]=$ip
    done
    remote_module_log $module_ip_map
  else
    declare -A module_ip_map
    module_ip_map["linkis-cg-entrance"]=$servername
    module_ip_map["linkis-cg-linkismanager"]=$servername
    module_ip_map["linkis-ps-publicservice"]=$servername
    remote_module_log $module_ip_map
  fi
}

# 任务提示信息
function print_task_info() {
  # 查询语句
  #execution_code=$(getValueForTasks "$task_list_result" executionCode)
  # 标签
  labels=$(getValueForTasks "$task_list_result" labels)
  engine_type=$(echo "$labels" | jq -r '.[] | select(startswith("engineType:")) | split(":")[1]')
  user_creator=$(echo "$labels" | jq -r '.[] | select(startswith("userCreator:")) | split(":")[1]')
  # 状态
  status=$(getValueForTasks "$task_list_result" status)
  # 已耗时
  cost_time=$(getValueForTasks "$task_list_result" costTime)
  # 创建时间
  yyyy_mm_dd_hh_mm_ss=$(date -d @$created_time_date "+%Y-%m-%d %H:%M:%S")
  # Entrance实例
  instance=$(getValueForTasks "$task_list_result" instance)
  # EC引擎实例
  engine_instance=$(getValueForTasks "$task_list_result" engineInstance)
  # 请求相关配置参数
  print_color_green "任务id:" "$job_id"
  # 只显示500个字符
#  if [ ${#execution_code} -gt 500 ]; then
#    # 截取前500个字符并加上省略号
#    execution_code="${execution_code:0:500}..."
#  fi
#  print_color_green "查询语句:$execution_code"
  print_color_green "标签:" "$user_creator,$engine_type"
  print_color_green "状态:" "$status"
  print_color_green "已耗时:" "$(($cost_time / 1000))"
  print_color_green "创建时间:" "$yyyy_mm_dd_hh_mm_ss"
  print_color_green "Entrance实例:" "$instance"
  print_color_green "EC引擎实例:" "$engine_instance"
}

# step2 提示信息
function print_step1_echo() {
  title="步骤1：任务进入Linkis排队"
  log_info=$(get_log_info "INFO Your job is accepted")
  check_error_log  "$log_info"  "$title"
  print_color_green "$title：" "如果您的任务卡在这里,需要调整并发,通过管理台--参数配置--调整对应引擎最大并发数(wds.linkis.rm.instance)"
  print_color_black "调度时间：$(get_time_info "$log_info")"
  print_color_black "调度的Entrance服务为：$(get_instance_info "$log_info")"
}

function print_step2_echo() {
  title="步骤2：任务进入Linkis运行，请求引擎中"
  log_info=$(get_log_info "INFO Request LinkisManager:EngineAskAsyncResponse")
  if [ -z "$log_info" ]; then
    print_color_red "$title"
            print_color_red "异常日志信息："
            print_color_red "$(cat $task_path"/json/$job_id"_detail.json | grep 'ERROR'| uniq)"
            echo -e "请联系linkis运维人员，进行排查处理"
    exit 0
  fi
  request_time=$(get_time_info "$log_info")
  linkis_manager_instance=$(get_instance_info "$log_info")
  log_info=$(get_log_info "INFO Succeed to create new ec")
  if [ -z "$log_info" ]; then
      log_info=$(get_log_info "INFO Succeed to reuse ec")
      if [ ! -z "$log_info" ]; then
            print_color_green "$title"
            print_color_black "请求引擎时间：$request_time"
            print_color_black "请求LinkisManager服务：$linkis_manager_instance"
            print_color_black "请求引擎为复用引擎：$(get_instance_info "$log_info")"
      else
            print_color_red "$title"
            print_color_red "请求引擎时间：$request_time"
            print_color_red "请求LinkisManager服务：$linkis_manager_instance"
            print_color_red "异常日志信息："
            print_color_red "$(cat $task_path"/json/$job_id"_detail.json | grep 'ERROR'| uniq)"
            echo -e "请联系linkis运维人员，进行排查处理"
            exit 0
      fi
  else
      print_color_green "$title"
      print_color_black "请求引擎时间：$request_time"
      print_color_black "请求LinkisManager服务：$linkis_manager_instance"
      print_color_black "请求引擎为新建引擎：$(get_instance_info "$log_info")"
  fi
  log_info=$(get_log_info "$submit_engine_constants")
  ec_start_time=$(get_time_info "$log_info")
  duration=$(($(date -d "$ec_start_time" +%s) - $(date -d "$request_time" +%s)))
  print_color_black "请求引擎耗时: $duration 秒"
}

function print_step3_echo() {
  if [[ $open_log_result =~ $submit_engine_constants ]]; then
    # 如果log path存在，需要通过任务id拉取EC端的stdout的日志
    if [[ $open_log_result =~ $engine_local_log_path ]]; then

      # 获取远程EC日志
      remote_access_to_ec_logs
    else
      # 获取 task_submit_to_ec_desc
      task_submit_to_ec_desc=$(cat $task_path"/json/"$job_id"_detail.json" | grep "$submit_engine_constants")
      print_color_green "任务提交给底层运行，但EC日志地址解析失败，请手动查看EC日志" "$task_submit_to_ec_desc"
    fi
  else
    print_color_green "步骤3：引擎请求失败，开始分析服务日志"
    # 获取链路日志
    remote_access_to_link_logs
  fi

}

function check_error_log() {
  log_info=$1
  title=$2
  if [ -z "$log_info" ]; then
      if [ -n "$title" ]; then
        print_color_red "$title"
      fi
    print_color_red "异常日志信息："
    print_color_red "$(cat $task_path"/json/$job_id"_detail.json | grep 'ERROR'| uniq)"
    echo -e "请联系linkis运维人员，进行排查处理"
    exit 0
  fi
}

function check_task_date() {
  # 获取任务创建时间
  created_time=$(getValueForTasks "$task_list_result" createdTime)

  # 截取时间戳
  export created_time_date=$(echo $created_time | cut -b 1-10)
  export yyyy_mm_dd=$(date -d @$created_time_date "+%Y-%m-%d")
  export yyy_mm=$(date -d @$created_time_date "+%Y-%m")

  # 当前日期与任务创建时间相差不能大于90
  days_between=$((($(date +%s) - $created_time_date) / (24 * 60 * 60)))
  if [ $days_between -gt 90 ]; then
    echo "只支持最近90天任务查询"
    exit 1
  fi
}

# 参数校验
check $*
print_color_green "***【任务基础信息】***"
# 任务查询
task_list_result=$(get_job_list_by_id)
# 根据任务列表接口返回判断status
check_tasks_stauts
# 日期校验
check_task_date
# 任务信息展示
print_task_info
# 将log日志写入到文件中
open_log_result=$(get_job_detail_log "$task_list_result")
log_detail=$(getValueForDetail "$open_log_result" log)
echo -e $log_detail >$task_path"/json/"$job_id"_detail.json"
# 诊断信息展示
print_color_green "***【任务诊断信息】***"
# 排队
print_step1_echo
# 运行
print_step2_echo
# 底层
print_step3_echo
