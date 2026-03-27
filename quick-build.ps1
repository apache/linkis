#Requires -Version 5.1
<#
.SYNOPSIS
    Linkis 混合编译脚本 (PowerShell 版本)

.DESCRIPTION
    解决方案: 先并行编译所有模块，再串行打包 linkis-dist
    这样既能获得并行编译的性能提升，又能保证产物完整性
    预期效果: 性能提升 40-50%，产物与串行编译完全一致

.PARAMETER Threads
    并行编译线程数，默认为 "1C" (使用 CPU 核心数)

.PARAMETER SkipTests
    是否跳过测试，默认为 $true

.PARAMETER V2
    编译 2.x 版本 (Hadoop 2 + Spark 2 + Hive 2)，默认编译 3.x 版本

.EXAMPLE
    .\quick-build.ps1
    使用默认设置编译 3.x 版本

.EXAMPLE
    .\quick-build.ps1 -V2
    编译 2.x 版本

.EXAMPLE
    .\quick-build.ps1 -Threads 4
    使用 4 线程编译

.EXAMPLE
    .\quick-build.ps1 -V2 -Threads 4
    编译 2.x 版本，使用 4 线程
#>

param(
    [string]$Threads = "1C",
    [switch]$SkipTests = $true,
    [switch]$V2 = $false
)

$ErrorActionPreference = "Stop"
$OutputEncoding = [System.Text.Encoding]::UTF8

# 颜色定义
function Write-ColorOutput {
    param(
        [string]$Message,
        [string]$Color = "White"
    )
    Write-Host $Message -ForegroundColor $Color
}

function Write-Banner {
    param([string]$Text)
    Write-Host ""
    Write-ColorOutput "╔════════════════════════════════════════════════════════╗" "Cyan"
    Write-ColorOutput "║$($Text.PadLeft(29 + $Text.Length/2).PadRight(58))║" "Cyan"
    Write-ColorOutput "╚════════════════════════════════════════════════════════╝" "Cyan"
    Write-Host ""
}

function Format-Duration {
    param([TimeSpan]$Duration)
    if ($Duration.TotalMinutes -ge 1) {
        return "{0}分{1}秒" -f [int]$Duration.TotalMinutes, $Duration.Seconds
    }
    return "{0}秒" -f [int]$Duration.TotalSeconds
}

# ============================================================
# 主程序开始
# ============================================================

Write-Banner "Linkis 混合编译模式 (Hybrid Build)"

Write-ColorOutput "📋 编译策略:" "Yellow"
Write-Host "   [1/2] 并行编译所有模块 (跳过 linkis-dist) - 使用 -T $Threads"
Write-Host "   [2/2] 串行打包 linkis-dist - 确保产物完整"
Write-Host ""
if ($V2) {
    Write-ColorOutput "🔧 版本: 2.x (Hadoop 2.7.2 + Spark 2.4.3 + Hive 2.3.3)" "Yellow"
} else {
    Write-ColorOutput "🔧 版本: 3.x (Hadoop 3.3.4 + Spark 3.2.1 + Hive 3.1.3) [默认]" "Yellow"
}
Write-Host ""
Write-ColorOutput ("⏱️  开始时间: " + (Get-Date -Format "yyyy-MM-dd HH:mm:ss")) "Yellow"
Write-Host ""

$TotalStartTime = Get-Date
$SkipTestsArg = if ($SkipTests) { "-DskipTests" } else { "" }
$V2ProfileArg = if ($V2) { "-Phadoop-2,spark-2,hive-2 -Dhadoop.profile=2" } else { "" }

# ============================================================
# Step 1: 并行编译所有模块（跳过 linkis-dist）
# ============================================================
Write-ColorOutput "[1/2] 🚀 并行编译所有模块..." "Green"
$cmd = "mvn clean install -T $Threads $SkipTestsArg $V2ProfileArg -pl `"!:linkis-dist`""
Write-Host "执行: $cmd"
Write-Host ""

$Step1Start = Get-Date

try {
    $mvnArgs = @("clean", "install", "-T", $Threads)
    if ($SkipTestsArg) { $mvnArgs += $SkipTestsArg }
    if ($V2) {
        $mvnArgs += "-Phadoop-2,spark-2,hive-2"
        $mvnArgs += "-Dhadoop.profile=2"
    }
    $mvnArgs += @("-pl", "!:linkis-dist")

    & mvn $mvnArgs
    if ($LASTEXITCODE -ne 0) {
        throw "Maven 编译失败，退出码: $LASTEXITCODE"
    }
} catch {
    Write-ColorOutput "❌ 步骤 1 编译失败: $_" "Red"
    exit 1
}

$Step1End = Get-Date
$Step1Duration = $Step1End - $Step1Start

Write-Host ""
Write-ColorOutput ("✅ 步骤 1 完成! 耗时: " + (Format-Duration $Step1Duration)) "Green"
Write-Host ""

# ============================================================
# Step 2: 串行编译 linkis-dist
# ============================================================
Write-ColorOutput "[2/2] 📦 串行打包 linkis-dist..." "Green"
$cmd = "mvn install -pl :linkis-dist $SkipTestsArg $V2ProfileArg"
Write-Host "执行: $cmd"
Write-Host ""

$Step2Start = Get-Date

try {
    $mvnArgs = @("install", "-pl", ":linkis-dist")
    if ($SkipTestsArg) { $mvnArgs += $SkipTestsArg }
    if ($V2) {
        $mvnArgs += "-Phadoop-2,spark-2,hive-2"
        $mvnArgs += "-Dhadoop.profile=2"
    }

    & mvn $mvnArgs
    if ($LASTEXITCODE -ne 0) {
        throw "Maven 打包失败，退出码: $LASTEXITCODE"
    }
} catch {
    Write-ColorOutput "❌ 步骤 2 编译失败: $_" "Red"
    exit 1
}

$Step2End = Get-Date
$Step2Duration = $Step2End - $Step2Start

Write-Host ""
Write-ColorOutput ("✅ 步骤 2 完成! 耗时: " + (Format-Duration $Step2Duration)) "Green"
Write-Host ""

# ============================================================
# 显示结果
# ============================================================
$TotalEndTime = Get-Date
$TotalDuration = $TotalEndTime - $TotalStartTime

Write-Banner "编译完成!"

Write-ColorOutput "📊 耗时统计:" "Yellow"
Write-Host ("   步骤 1 (并行编译模块): " + (Format-Duration $Step1Duration))
Write-Host ("   步骤 2 (串行打包):     " + (Format-Duration $Step2Duration))
Write-Host "   ────────────────────────────"
Write-ColorOutput ("   总耗时: " + (Format-Duration $TotalDuration)) "Green"
Write-Host ""

# 检查产物
$DistDir = Join-Path $PSScriptRoot "linkis-dist\target\apache-linkis-1.8.0-bin"
if (Test-Path $DistDir) {
    $Files = Get-ChildItem -Path $DistDir -Recurse -File
    $FileCount = $Files.Count
    $TotalSize = ($Files | Measure-Object -Property Length -Sum).Sum
    $SizeFormatted = if ($TotalSize -ge 1GB) {
        "{0:N2} GB" -f ($TotalSize / 1GB)
    } elseif ($TotalSize -ge 1MB) {
        "{0:N0} MB" -f ($TotalSize / 1MB)
    } else {
        "{0:N0} KB" -f ($TotalSize / 1KB)
    }

    Write-ColorOutput "📦 产物信息:" "Yellow"
    Write-Host "   目录: $DistDir"
    Write-Host "   文件数: $FileCount"
    Write-Host "   总大小: $SizeFormatted"
    Write-Host ""

    # 检查关键目录
    Write-ColorOutput "🔍 关键模块检查:" "Yellow"
    $Modules = @(
        "linkis-cg-engineconnmanager",
        "linkis-cg-entrance",
        "linkis-cg-linkismanager"
    )

    foreach ($Module in $Modules) {
        $ModulePath = Join-Path $DistDir "linkis-package\lib\linkis-computation-governance\$Module"
        if (Test-Path $ModulePath) {
            Write-ColorOutput "   ✅ $Module" "Green"
        } else {
            Write-ColorOutput "   ❌ $Module (缺失!)" "Red"
        }
    }
    Write-Host ""
}

Write-ColorOutput "🎉 混合编译完成!" "Green"
Write-Host ("   结束时间: " + (Get-Date -Format "yyyy-MM-dd HH:mm:ss"))
Write-Host ""
