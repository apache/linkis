#!/bin/bash

###############################################################################
# Monitor模块优化 - 远程测试脚本
#
# 功能：通过curl命令验证Monitor模块优化功能
# 使用方法：./remote_curl_test.sh <host> <port>
# 示例：./remote_curl_test.sh localhost 8080
#
# 作者：DevSyncAgent
# 日期：2024-03-24
###############################################################################

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 默认配置
HOST=${1:-localhost}
PORT=${2:-8080}
BASE_URL="http://${HOST}:${PORT}"

# 测试结果统计
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

###############################################################################
# 辅助函数
###############################################################################

# 打印标题
print_title() {
    echo -e "\n${GREEN}========================================${NC}"
    echo -e "${GREEN}$1${NC}"
    echo -e "${GREEN}========================================${NC}\n"
}

# 打印测试步骤
print_step() {
    echo -e "${YELLOW}[TEST]${NC} $1"
}

# 打印成功
print_success() {
    echo -e "${GREEN}[✓]${NC} $1"
    ((PASSED_TESTS++))
    ((TOTAL_TESTS++))
}

# 打印失败
print_failure() {
    echo -e "${RED}[✗]${NC} $1"
    ((FAILED_TESTS++))
    ((TOTAL_TESTS++))
}

# 发送HTTP请求
send_request() {
    local url=$1
    local method=${2:-GET}
    local data=${3:-}

    if [ -z "$data" ]; then
        curl -s -X "$method" "$url" \
            -H "Content-Type: application/json" \
            --connect-timeout 10 \
            --max-time 30
    else
        curl -s -X "$method" "$url" \
            -H "Content-Type: application/json" \
            -d "$data" \
            --connect-timeout 10 \
            --max-time 30
    fi
}

###############################################################################
# 测试函数
###############################################################################

# 测试1: 检查Monitor服务健康状态
test_monitor_health() {
    print_title "测试1: Monitor服务健康检查"

    local url="${BASE_URL}/actuator/health"
    print_step "URL: $url"

    local response=$(send_request "$url")
    local http_code=$(curl -s -o /dev/null -w "%{http_code}" "$url")

    echo "响应码: $http_code"
    echo "响应内容: $response"

    if [ "$http_code" == "200" ]; then
        print_success "Monitor服务健康检查通过"
    else
        print_failure "Monitor服务健康检查失败 (HTTP $http_code)"
    fi
}

# 测试2: 检查诊断日志配置
test_diagnosis_log_config() {
    print_title "测试2: 诊断日志配置检查"

    local url="${BASE_URL}/actuator/env/linkis.monitor.diagnosis.log.enabled"
    print_step "URL: $url"

    local response=$(send_request "$url")
    echo "响应内容: $response"

    if echo "$response" | grep -q "true"; then
        print_success "诊断日志功能已启用"
    else
        print_failure "诊断日志功能配置异常"
    fi
}

# 测试3: 检查日志保留天数配置
test_retention_days_config() {
    print_title "测试3: 日志保留天数配置检查"

    local url="${BASE_URL}/actuator/env/linkis.monitor.diagnosis.log.retention.days"
    print_step "URL: $url"

    local response=$(send_request "$url")
    echo "响应内容: $response"

    if echo "$response" | grep -q "7"; then
        print_success "日志保留天数配置为7天"
    else
        print_failure "日志保留天数配置异常"
    fi
}

# 测试4: 检查诊断功能开关配置
test_diagnosis_enabled_config() {
    print_title "测试4: 诊断功能开关配置检查"

    local url="${BASE_URL}/actuator/env/linkis.monitor.jobHistory.diagnosis.enabled"
    print_step "URL: $url"

    local response=$(send_request "$url")
    echo "响应内容: $response"

    if echo "$response" | grep -q "true"; then
        print_success "诊断功能已启用（默认值，向后兼容）"
    else
        print_failure "诊断功能配置异常"
    fi
}

# 测试5: 检查Alert连接池配置
test_alert_pool_config() {
    print_title "测试5: Alert连接池配置检查"

    # 通过JMX或日志验证连接池大小
    print_step "检查连接池配置（需要查看日志或JMX）"

    echo "提示: 连接池配置验证需要以下方式之一:"
    echo "  1. 查看应用日志: grep 'alert-pool-thread-' logs/linkis.log"
    echo "  2. 使用JConsole连接到JVM"
    echo "  3. 访问JMX端点: $BASE_URL/actuator/jmx"

    local url="${BASE_URL}/actuator/jmx"
    local response=$(send_request "$url")

    if echo "$response" | grep -q "alert"; then
        print_success "Alert连接池JMX信息可访问"
    else
        print_failure "无法访问JMX信息（可能需要启用spring.jmx.enabled=true）"
    fi
}

# 测试6: 检查定时任务配置
test_scheduled_task_config() {
    print_title "测试6: 定时任务配置检查"

    local url="${BASE_URL}/actuator/scheduledtasks"
    print_step "URL: $url"

    local response=$(send_request "$url")
    echo "响应内容: $response"

    if echo "$response" | grep -q "clearExpiredDiagnosisLogs"; then
        print_success "诊断日志清理定时任务已注册"
    else
        print_failure "诊断日志清理定时任务未找到"
    fi
}

# 测试7: 检查线程池信息
test_thread_pool_info() {
    print_title "测试7: 线程池信息检查"

    local url="${BASE_URL}/actuator/metrics/executor.pool.size"
    print_step "URL: $url"

    local response=$(send_request "$url")
    echo "响应内容: $response"

    if echo "$response" | grep -q "alert"; then
        print_success "Alert线程池指标可访问"
    else
        print_failure "无法访问线程池指标"
    fi
}

# 测试8: 触发手动清理（如果提供了端点）
test_manual_cleanup() {
    print_title "测试8: 手动触发日志清理"

    # 假设提供了一个管理端点用于手动触发清理
    local url="${BASE_URL}/api/linkis/monitor/diagnosis/clear"
    print_step "URL: $url (此端点可能不存在，仅用于演示)"

    local response=$(send_request "$url" "POST")
    local http_code=$(curl -s -o /dev/null -w "%{http_code}" "$url" -X POST)

    echo "响应码: $http_code"
    echo "响应内容: $response"

    if [ "$http_code" == "200" ] || [ "$http_code" == "202" ]; then
        print_success "手动清理触发成功"
    elif [ "$http_code" == "404" ]; then
        echo -e "${YELLOW}[INFO]${NC} 手动清理端点不存在（此功能可选）"
        ((TOTAL_TESTS++))
    else
        print_failure "手动清理触发失败 (HTTP $http_code)"
    fi
}

# 测试9: 检查日志文件
test_log_files() {
    print_title "测试9: 检查诊断日志文件"

    local log_dir="${LINKIS_LOG_DIR:-/tmp/linkis/logs}/task"

    print_step "检查日志目录: $log_dir"

    if [ ! -d "$log_dir" ]; then
        echo -e "${YELLOW}[INFO]${NC} 日志目录不存在: $log_dir"
        ((TOTAL_TESTS++))
        return
    fi

    # 检查是否有job_id目录
    local job_count=$(find "$log_dir" -maxdepth 1 -type d -name '[0-9]*' | wc -l)
    echo "发现 $job_count 个job_id目录"

    if [ "$job_count" -gt 0 ]; then
        print_success "发现诊断日志文件"
    else
        echo -e "${YELLOW}[INFO]${NC} 当前无诊断日志文件（正常情况）"
        ((TOTAL_TESTS++))
    fi

    # 检查json目录
    local json_dir="${LINKIS_LOG_DIR:-/tmp/linkis/logs}/json"
    if [ -d "$json_dir" ]; then
        local json_count=$(find "$json_dir" -maxdepth 1 -type f -name '*_detail.json' | wc -l)
        echo "发现 $json_count 个detail JSON文件"
    fi
}

# 测试10: 检查Monitor配置
test_monitor_config() {
    print_title "测试10: Monitor完整配置检查"

    local url="${BASE_URL}/actuator/configprops"
    print_step "URL: $url"

    local response=$(send_request "$url")
    echo "响应内容: $response" | head -n 50

    if echo "$response" | grep -q "monitor.diagnosis"; then
        print_success "Monitor配置信息可访问"
    else
        print_failure "Monitor配置信息不可访问"
    fi
}

###############################################################################
# 主流程
###############################################################################

main() {
    echo -e "${GREEN}"
    echo "╔════════════════════════════════════════════════════════════╗"
    echo "║     Monitor模块优化 - 远程测试脚本                          ║"
    echo "╚════════════════════════════════════════════════════════════╝"
    echo -e "${NC}"

    echo "测试目标: $BASE_URL"
    echo "开始时间: $(date '+%Y-%m-%d %H:%M:%S')"

    # 检查服务是否可访问
    print_step "检查服务可用性..."
    if ! curl -s --connect-timeout 5 "$BASE_URL/actuator/health" > /dev/null 2>&1; then
        echo -e "${RED}[ERROR]${NC} 无法连接到 $BASE_URL"
        echo "请确保:"
        echo "  1. Monitor服务已启动"
        echo "  2. 端口配置正确"
        echo "  3. Actuator端点已启用"
        exit 1
    fi

    echo -e "${GREEN}[OK]${NC} 服务可访问\n"

    # 执行测试
    test_monitor_health
    test_diagnosis_log_config
    test_retention_days_config
    test_diagnosis_enabled_config
    test_alert_pool_config
    test_scheduled_task_config
    test_thread_pool_info
    test_manual_cleanup
    test_log_files
    test_monitor_config

    # 输出测试结果摘要
    print_title "测试结果摘要"
    echo "总测试数: $TOTAL_TESTS"
    echo -e "通过: ${GREEN}$PASSED_TESTS${NC}"
    echo -e "失败: ${RED}$FAILED_TESTS${NC}"
    echo "结束时间: $(date '+%Y-%m-%d %H:%M:%S')"

    if [ $FAILED_TESTS -eq 0 ]; then
        echo -e "\n${GREEN}所有测试通过！${NC}"
        exit 0
    else
        echo -e "\n${RED}部分测试失败，请检查详细日志${NC}"
        exit 1
    fi
}

###############################################################################
# 脚本入口
###############################################################################

# 检查依赖
if ! command -v curl &> /dev/null; then
    echo "错误: 需要安装curl命令"
    exit 1
fi

if ! command -v jq &> /dev/null; then
    echo "警告: 建议安装jq命令以更好地解析JSON响应"
fi

# 显示帮助信息
if [ "$1" == "-h" ] || [ "$1" == "--help" ]; then
    echo "用法: $0 [host] [port]"
    echo ""
    echo "参数:"
    echo "  host    - 服务器地址（默认: localhost）"
    echo "  port    - 端口号（默认: 8080）"
    echo ""
    echo "示例:"
    echo "  $0                          # 使用默认配置"
    echo "  $0 localhost 8080           # 指定主机和端口"
    echo "  $0 192.168.1.100 8080      # 测试远程服务器"
    exit 0
fi

# 执行主流程
main
