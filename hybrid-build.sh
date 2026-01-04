#!/bin/bash
#
# Linkis 混合编译脚本 (Linux/macOS)
#
# 解决方案: 先并行编译所有模块，再串行打包 linkis-dist
# 这样既能获得并行编译的性能提升，又能保证产物完整性
#
# 预期效果: 性能提升 40-50%，产物与串行编译完全一致
#
# 用法: ./hybrid-build.sh [选项]
#   -t, --threads <N>   并行线程数，默认为 1C (CPU核心数)
#   -s, --skip-tests    跳过测试 (默认)
#   -r, --run-tests     运行测试
#   -h, --help          显示帮助
#

set -e

# 默认参数
THREADS="1C"
SKIP_TESTS=true

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$SCRIPT_DIR"

# 帮助信息
show_help() {
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  -t, --threads <N>   并行线程数，默认为 1C (CPU核心数)"
    echo "  -s, --skip-tests    跳过测试 (默认)"
    echo "  -r, --run-tests     运行测试"
    echo "  -h, --help          显示帮助"
    echo ""
    echo "示例:"
    echo "  $0                  使用默认设置编译"
    echo "  $0 -t 4             使用 4 线程编译"
    echo "  $0 -r               运行测试"
}

# 解析参数
while [[ $# -gt 0 ]]; do
    case $1 in
        -t|--threads)
            THREADS="$2"
            shift 2
            ;;
        -s|--skip-tests)
            SKIP_TESTS=true
            shift
            ;;
        -r|--run-tests)
            SKIP_TESTS=false
            shift
            ;;
        -h|--help)
            show_help
            exit 0
            ;;
        *)
            echo "未知选项: $1"
            show_help
            exit 1
            ;;
    esac
done

# 构建 Maven 参数
SKIP_TESTS_ARG=""
if [ "$SKIP_TESTS" = true ]; then
    SKIP_TESTS_ARG="-DskipTests"
fi

# 格式化时间
format_duration() {
    local seconds=$1
    local minutes=$((seconds / 60))
    local secs=$((seconds % 60))
    if [ $minutes -gt 0 ]; then
        echo "${minutes}分${secs}秒"
    else
        echo "${secs}秒"
    fi
}

echo ""
echo -e "${BLUE}╔════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║         Linkis 混合编译模式 (Hybrid Build)             ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${YELLOW}📋 编译策略:${NC}"
echo "   [1/2] 并行编译所有模块 (跳过 linkis-dist) - 使用 -T $THREADS"
echo "   [2/2] 串行打包 linkis-dist - 确保产物完整"
echo ""
echo -e "${YELLOW}⏱️  开始时间: $(date '+%Y-%m-%d %H:%M:%S')${NC}"
echo ""

# 记录开始时间
START_TIME=$(date +%s)

# ============================================================
# Step 1: 并行编译所有模块（跳过 linkis-dist）
# ============================================================
echo -e "${GREEN}[1/2] 🚀 并行编译所有模块...${NC}"
echo "执行: mvn clean install -T $THREADS $SKIP_TESTS_ARG -pl '!:linkis-dist'"
echo ""

cd "$PROJECT_DIR"
STEP1_START=$(date +%s)

mvn clean install -T $THREADS $SKIP_TESTS_ARG -pl '!:linkis-dist'

STEP1_END=$(date +%s)
STEP1_TIME=$((STEP1_END - STEP1_START))

echo ""
echo -e "${GREEN}✅ 步骤 1 完成! 耗时: ${STEP1_TIME} 秒 ($(format_duration $STEP1_TIME))${NC}"
echo ""

# ============================================================
# Step 2: 串行编译 linkis-dist
# ============================================================
echo -e "${GREEN}[2/2] 📦 串行打包 linkis-dist...${NC}"
echo "执行: mvn install -pl :linkis-dist $SKIP_TESTS_ARG"
echo ""

STEP2_START=$(date +%s)

mvn install -pl :linkis-dist $SKIP_TESTS_ARG

STEP2_END=$(date +%s)
STEP2_TIME=$((STEP2_END - STEP2_START))

echo ""
echo -e "${GREEN}✅ 步骤 2 完成! 耗时: ${STEP2_TIME} 秒 ($(format_duration $STEP2_TIME))${NC}"
echo ""

# ============================================================
# 显示结果
# ============================================================
END_TIME=$(date +%s)
TOTAL_TIME=$((END_TIME - START_TIME))

echo -e "${BLUE}╔════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║                    编译完成!                           ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${YELLOW}📊 耗时统计:${NC}"
echo "   步骤 1 (并行编译模块): ${STEP1_TIME} 秒 ($(format_duration $STEP1_TIME))"
echo "   步骤 2 (串行打包):     ${STEP2_TIME} 秒 ($(format_duration $STEP2_TIME))"
echo "   ────────────────────────────"
echo -e "   ${GREEN}总耗时: ${TOTAL_TIME} 秒 ($(format_duration $TOTAL_TIME))${NC}"
echo ""

# 检查产物
DIST_DIR="$PROJECT_DIR/linkis-dist/target/apache-linkis-1.8.0-bin"
if [ -d "$DIST_DIR" ]; then
    FILE_COUNT=$(find "$DIST_DIR" -type f 2>/dev/null | wc -l)
    DIR_SIZE=$(du -sh "$DIST_DIR" 2>/dev/null | cut -f1)

    echo -e "${YELLOW}📦 产物信息:${NC}"
    echo "   目录: $DIST_DIR"
    echo "   文件数: $FILE_COUNT"
    echo "   总大小: $DIR_SIZE"
    echo ""

    # 检查关键目录
    echo -e "${YELLOW}🔍 关键模块检查:${NC}"
    for module in "linkis-cg-engineconnmanager" "linkis-cg-entrance" "linkis-cg-linkismanager"; do
        module_path="$DIST_DIR/linkis-package/lib/linkis-computation-governance/$module"
        if [ -d "$module_path" ]; then
            echo -e "   ${GREEN}✅ $module${NC}"
        else
            echo -e "   ${RED}❌ $module (缺失!)${NC}"
        fi
    done
    echo ""
fi

echo -e "${GREEN}🎉 混合编译完成!${NC}"
echo "   结束时间: $(date '+%Y-%m-%d %H:%M:%S')"
echo ""
