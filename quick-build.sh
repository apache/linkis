#!/bin/bash
#
# Linkis 混合编译脚本 (Linux/macOS)
#
# 解决方案：分阶段编译
#   Stage 1: 并行编译基础模块 (跳过 hbase 和 linkis-dist)
#   Stage 2: 串行编译 hbase 模块 (依赖基础模块)
#   Stage 3: 并行编译剩余模块和 linkis-dist
#
# 预期效果：性能提升 40-50%，产物与串行编译完全一致
#
# 用法：./quick-build.sh [选项]
#   -t, --threads <N>      并行线程数，默认为 1C (CPU 核心数)
#   -s, --skip-tests       跳过测试 (默认)
#   -r, --run-tests        运行测试
#   --v2                   编译默认版本 (Hadoop 2.7.2 + Spark 2.4.3 + Scala 2.11 + Hive 2.3.3), 同默认
#   --v3                   编译 Apache 发布版本 (Hadoop 3.3.4 + Spark 3.2.1 + Scala 2.12 + Hive 3.1.3), 同 -Papache
#   --hadoop <VER>         指定 Hadoop 完整版本号 (如 3.3.4, 2.7.2)
#   --spark <VER>          指定 Spark 完整版本号 (如 3.4.4, 3.2.1, 2.4.3)
#   --hive <VER>           指定 Hive 完整版本号 (如 3.1.3, 2.3.3)
#   --scala <VER>          指定 Scala 完整版本号 (如 2.12.17, 2.11.12)
#   --revision <VER>       指定 revision 版本号 (默认：1.8.0)
#   -h, --help             显示帮助
#
# 版本说明:
#   默认版本：Hadoop 2.7.2 + Spark 2.4.3 + Scala 2.11.12 + Hive 2.3.3
#   -Pspark-3: Spark 3.4.4 + Scala 2.12.17 (Hadoop/Hive 保持默认)
#   -Papache / --v3: Hadoop 3.3.4 + Spark 3.2.1 + Scala 2.12.17 + Hive 3.1.3
#
# 示例:
#   ./quick-build.sh                           使用默认版本编译 (Hadoop 2.7.2 + Spark 2.4.3 + Scala 2.11 + Hive 2.3.3)
#   ./quick-build.sh --v2                      同默认版本 (仅用于明确指定 v2)
#   ./quick-build.sh --v3                      编译 Apache 发布版本 (同 -Papache)
#   ./quick-build.sh -Pspark-3                 只升级 Spark 到 3.4.4 + Scala 2.12.17
#   ./quick-build.sh --revision 1.8.0-spark2   指定 revision 编译 (用于区分不同版本)
#   ./quick-build.sh --spark 3.4.4             指定 Spark 3.4.4 编译
#   ./quick-build.sh --hadoop 3.3.4 --spark 3.4.4 --hive 3.1.3  指定完整版本组合
#   ./quick-build.sh --spark 3.4.4 --scala 2.12.17 --hadoop 3.3.1  自定义版本组合
#   ./quick-build.sh -t 4                      使用 4 线程编译
#   ./quick-build.sh -r                        运行测试
#
# 注意：--hadoop/--spark/--hive/--scala 参数优先级高于 --v2/--v3，可覆盖 Profile 中的版本
#

set -e

# 默认参数
THREADS="1C"
SKIP_TESTS=true
V2_MODE=false
V3_MODE=false
CUSTOM_VERSION_MODE=false
HADOOP_VERSION=""
SPARK_VERSION=""
HIVE_VERSION=""
SCALA_VERSION=""
REVISION=""

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
    echo "用法：$0 [选项]"
    echo ""
    echo "选项:"
    echo "  -t, --threads <N>      并行线程数，默认为 1C (CPU 核心数)"
    echo "  -s, --skip-tests       跳过测试 (默认)"
    echo "  -r, --run-tests        运行测试"
    echo "  --v2                   编译默认版本 (Hadoop 2.7.2 + Spark 2.4.3 + Scala 2.11 + Hive 2.3.3), 同默认"
    echo "  --v3                   编译 Apache 发布版本 (Hadoop 3.3.4 + Spark 3.2.1 + Scala 2.12 + Hive 3.1.3), 同 -Papache"
    echo "  --hadoop <VER>         指定 Hadoop 完整版本号 (如 3.3.4, 2.7.2)"
    echo "  --spark <VER>          指定 Spark 完整版本号 (如 3.4.4, 3.2.1, 2.4.3)"
    echo "  --hive <VER>           指定 Hive 完整版本号 (如 3.1.3, 2.3.3)"
    echo "  --scala <VER>          指定 Scala 完整版本号 (如 2.12.17, 2.11.12)"
    echo "  --revision <VER>       指定 revision 版本号 (默认：1.8.0)"
    echo "  -h, --help             显示帮助"
    echo ""
    echo "版本参数说明:"
    echo "  --hadoop/--spark/--hive/--scala 可单独使用，也可与 --v2/--v3 组合使用"
    echo "  当与 --v2/--v3 同时使用时，指定的版本会覆盖对应预设版本的默认值"
    echo ""
    echo "编译方式说明:"
    echo "  默认/-Pspark-2: Hadoop 2.7.2 + Spark 2.4.3 + Scala 2.11.12 + Hive 2.3.3"
    echo "  -Pspark-3: Spark 3.4.4 + Scala 2.12.17 (Hadoop/Hive 保持默认)"
    echo "  -Papache / --v3: Hadoop 3.3.4 + Spark 3.2.1 + Scala 2.12.17 + Hive 3.1.3"
    echo ""
    echo "示例:"
    echo "  ./quick-build.sh                                    默认版本 (Hadoop 2.7.2 + Spark 2.4.3)"
    echo "  ./quick-build.sh --v2                               同默认版本"
    echo "  ./quick-build.sh --v3                               Apache 发布版本 (同 -Papache)"
    echo "  ./quick-build.sh --spark 3.4.4                      只升级 Spark 到 3.4.4"
    echo "  ./quick-build.sh --v3 --spark 3.4.4 --hadoop 3.3.1  Apache 基础上修改 Spark 和 Hadoop"
    echo "  ./quick-build.sh --hadoop 3.3.4 --spark 3.2.1 --hive 3.1.3  指定完整版本组合"
    echo "  ./quick-build.sh --revision 1.8.0-spark2            指定 revision 编译"
    echo "  ./quick-build.sh -t 4                               使用 4 线程编译"
    echo "  ./quick-build.sh -r                                 运行测试编译"
    echo ""
    echo "双版本编译 (分别执行两次，避免 Maven 仓库覆盖):"
    echo "  ./quick-build.sh                                    # 编译默认版本 (Spark 2.4.3)"
    echo "  ./quick-build.sh --v3 --revision 1.8.0-apache       # 编译 Apache 版本 (Spark 3.2.1)"
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
        --v2)
            V2_MODE=true
            shift
            ;;
        --v3)
            V3_MODE=true
            shift
            ;;
        --hadoop)
            CUSTOM_VERSION_MODE=true
            HADOOP_VERSION="$2"
            shift 2
            ;;
        --spark)
            CUSTOM_VERSION_MODE=true
            SPARK_VERSION="$2"
            shift 2
            ;;
        --hive)
            CUSTOM_VERSION_MODE=true
            HIVE_VERSION="$2"
            shift 2
            ;;
        --scala)
            CUSTOM_VERSION_MODE=true
            SCALA_VERSION="$2"
            shift 2
            ;;
        --revision)
            REVISION="$2"
            shift 2
            ;;
        -h|--help)
            show_help
            exit 0
            ;;
        *)
            echo "未知选项：$1"
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

# Profile 参数
PROFILE_ARG=""
if [ "$V3_MODE" = true ]; then
    # --v3 模式：使用 -Papache profile (Hadoop 3.3.4 + Spark 3.2.1 + Scala 2.12 + Hive 3.1.3)
    PROFILE_ARG="-Papache"
else
    # 默认模式 (包括 --v2): 使用默认配置 (Hadoop 2.7.2 + Spark 2.4.3 + Scala 2.11 + Hive 2.3.3)
    # 不需要额外指定 profile
    PROFILE_ARG=""
fi

# 自定义版本参数（优先级高于 --v2/--v3）
CUSTOM_VERSION_ARGS=""
if [ -n "$HADOOP_VERSION" ]; then
    CUSTOM_VERSION_ARGS="$CUSTOM_VERSION_ARGS -Dhadoop.version=$HADOOP_VERSION"
fi
if [ -n "$SPARK_VERSION" ]; then
    CUSTOM_VERSION_ARGS="$CUSTOM_VERSION_ARGS -Dspark.version=$SPARK_VERSION"
fi
if [ -n "$HIVE_VERSION" ]; then
    CUSTOM_VERSION_ARGS="$CUSTOM_VERSION_ARGS -Dhive.version=$HIVE_VERSION"
fi
if [ -n "$SCALA_VERSION" ]; then
    CUSTOM_VERSION_ARGS="$CUSTOM_VERSION_ARGS -Dscala.version=$SCALA_VERSION"
fi

# Revision 参数
REVISION_ARG=""
if [ -n "$REVISION" ]; then
    REVISION_ARG="-Drevision=$REVISION"
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

# 确定显示的版本信息
if [ "$CUSTOM_VERSION_MODE" = true ]; then
    # 自定义版本模式
    VERSION_DISPLAY="自定义版本 ("
    [ -n "$HADOOP_VERSION" ] && VERSION_DISPLAY="$VERSION_DISPLAY Hadoop $HADOOP_VERSION"
    [ -n "$SPARK_VERSION" ] && VERSION_DISPLAY="$VERSION_DISPLAY Spark $SPARK_VERSION"
    [ -n "$HIVE_VERSION" ] && VERSION_DISPLAY="$VERSION_DISPLAY Hive $HIVE_VERSION"
    [ -n "$SCALA_VERSION" ] && VERSION_DISPLAY="$VERSION_DISPLAY Scala $SCALA_VERSION"
    VERSION_DISPLAY="$VERSION_DISPLAY )"
    DEFAULT_REVISION="1.8.0"
elif [ "$V3_MODE" = true ]; then
    # --v3 模式：Apache 发布版本
    VERSION_DISPLAY="Apache 发布版本 (Hadoop 3.3.4 + Spark 3.2.1 + Scala 2.12.17 + Hive 3.1.3)"
    DEFAULT_REVISION="1.8.0-apache"
else
    # 默认模式 (包括 --v2)
    VERSION_DISPLAY="默认版本 (Hadoop 2.7.2 + Spark 2.4.3 + Scala 2.11.12 + Hive 2.3.3)"
    DEFAULT_REVISION="1.8.0"
fi

# 如果未指定 revision，使用默认值
if [ -z "$REVISION" ]; then
    REVISION_ARG="-Drevision=$DEFAULT_REVISION"
fi

echo ""
echo -e "${BLUE}╔════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║         Linkis 混合编译模式 (Hybrid Build)             ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${YELLOW}📋 编译策略:${NC}"
echo "   [1/3] 并行编译基础模块 (跳过 hbase 和 linkis-dist) - 使用 -T $THREADS"
echo "   [2/3] 串行编译 hbase 模块 (依赖基础模块)"
echo "   [3/3] 并行编译剩余模块和 linkis-dist"
echo ""
echo -e "${YELLOW}🔧 版本：${VERSION_DISPLAY}${NC}"
echo -e "${YELLOW}📦 Revision: ${REVISION_ARG#-Drevision=}${NC}"
echo ""
echo -e "${YELLOW}⏱️  开始时间：$(date '+%Y-%m-%d %H:%M:%S')${NC}"
echo ""

# 记录开始时间
START_TIME=$(date +%s)

# ============================================================
# Step 1: 并行编译基础模块（跳过 hbase 和 linkis-dist）
# ============================================================
echo -e "${GREEN}[1/3] 🚀 并行编译基础模块...${NC}"
echo "执行：mvn clean install -T $THREADS $SKIP_TESTS_ARG $PROFILE_ARG $CUSTOM_VERSION_ARGS $REVISION_ARG -pl '!org.apache.linkis:linkis-engineconn-plugin-hbase','!org.apache.linkis:linkis-engineconn-plugin-hbase-module','!org.apache.linkis:hbase-shims-1.2.1','!org.apache.linkis:hbase-shims-1.4.3','!org.apache.linkis:hbase-shims-2.2.6','!org.apache.linkis:hbase-shims-2.5.3','!org.apache.linkis:linkis-dist'"
echo ""

cd "$PROJECT_DIR"
STEP1_START=$(date +%s)

mvn clean install -T $THREADS $SKIP_TESTS_ARG $PROFILE_ARG $CUSTOM_VERSION_ARGS $REVISION_ARG -pl '!org.apache.linkis:linkis-engineconn-plugin-hbase','!org.apache.linkis:linkis-engineconn-plugin-hbase-module','!org.apache.linkis:hbase-shims-1.2.1','!org.apache.linkis:hbase-shims-1.4.3','!org.apache.linkis:hbase-shims-2.2.6','!org.apache.linkis:hbase-shims-2.5.3','!org.apache.linkis:linkis-dist'

STEP1_END=$(date +%s)
STEP1_TIME=$((STEP1_END - STEP1_START))

echo ""
echo -e "${GREEN}✅ 步骤 1 完成！耗时：${STEP1_TIME} 秒 ($(format_duration $STEP1_TIME))${NC}"
echo ""

# ============================================================
# Step 2: 串行编译 hbase 模块（依赖基础模块）
# ============================================================
echo -e "${GREEN}[2/3] 📦 串行编译 hbase 模块...${NC}"
echo "执行：mvn install -pl org.apache.linkis:linkis-engineconn-plugin-hbase-module -am $SKIP_TESTS_ARG $PROFILE_ARG $CUSTOM_VERSION_ARGS $REVISION_ARG"
echo ""

STEP2_START=$(date +%s)

# 编译 hbase 模块（使用 -am 确保依赖也被编译，但基础模块已编译会直接跳过）
mvn install -pl org.apache.linkis:linkis-engineconn-plugin-hbase-module -am $SKIP_TESTS_ARG $PROFILE_ARG $CUSTOM_VERSION_ARGS $REVISION_ARG

STEP2_END=$(date +%s)
STEP2_TIME=$((STEP2_END - STEP2_START))

echo ""
echo -e "${GREEN}✅ 步骤 2 完成！耗时：${STEP2_TIME} 秒 ($(format_duration $STEP2_TIME))${NC}"
echo ""

# ============================================================
# Step 3: 并行编译剩余模块和 linkis-dist
# ============================================================
echo -e "${GREEN}[3/3] 🚀 并行编译 linkis-dist...${NC}"
echo "执行：mvn install -T $THREADS -pl :linkis-dist $SKIP_TESTS_ARG $PROFILE_ARG $CUSTOM_VERSION_ARGS $REVISION_ARG"
echo ""

STEP3_START=$(date +%s)

mvn install -T $THREADS -pl :linkis-dist $SKIP_TESTS_ARG $PROFILE_ARG $CUSTOM_VERSION_ARGS $REVISION_ARG

STEP3_END=$(date +%s)
STEP3_TIME=$((STEP3_END - STEP3_START))

echo ""
echo -e "${GREEN}✅ 步骤 3 完成！耗时：${STEP3_TIME} 秒 ($(format_duration $STEP3_TIME))${NC}"
echo ""

# ============================================================
# 显示结果
# ============================================================
END_TIME=$(date +%s)
TOTAL_TIME=$((END_TIME - START_TIME))

CURRENT_REVISION=${REVISION_ARG#-Drevision=}
DIST_DIR="$PROJECT_DIR/linkis-dist/target/apache-linkis-${CURRENT_REVISION}-bin"

echo -e "${BLUE}╔════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║                    编译完成！                          ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${YELLOW}📊 耗时统计:${NC}"
echo "   步骤 1 (并行编译基础模块): ${STEP1_TIME} 秒 ($(format_duration $STEP1_TIME))"
echo "   步骤 2 (串行编译 hbase):    ${STEP2_TIME} 秒 ($(format_duration $STEP2_TIME))"
echo "   步骤 3 (并行编译剩余模块):  ${STEP3_TIME} 秒 ($(format_duration $STEP3_TIME))"
echo "   ──────────────────────────────────"
echo -e "   ${GREEN}总耗时：${TOTAL_TIME} 秒 ($(format_duration $TOTAL_TIME))${NC}"
echo ""

# 检查产物
if [ -d "$DIST_DIR" ]; then
    FILE_COUNT=$(find "$DIST_DIR" -type f 2>/dev/null | wc -l)
    DIR_SIZE=$(du -sh "$DIST_DIR" 2>/dev/null | cut -f1)

    echo -e "${YELLOW}📦 产物信息:${NC}"
    echo "   目录：$DIST_DIR"
    echo "   文件数：$FILE_COUNT"
    echo "   总大小：$DIR_SIZE"
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
else
    echo -e "${YELLOW}⚠️  产物目录不存在：$DIST_DIR${NC}"
    echo ""
fi

echo -e "${GREEN}🎉 混合编译完成!${NC}"
echo "   结束时间：$(date '+%Y-%m-%d %H:%M:%S')"
echo ""
