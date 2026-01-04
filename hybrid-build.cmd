@echo off
chcp 65001 >nul 2>&1
setlocal EnableDelayedExpansion

REM ============================================================
REM Linkis 混合编译脚本 (Windows 版本)
REM
REM 解决方案: 先并行编译所有模块，再串行打包 linkis-dist
REM 这样既能获得并行编译的性能提升，又能保证产物完整性
REM
REM 预期效果: 性能提升 40-50%%，产物与串行编译完全一致
REM ============================================================

set "THREADS=1C"

echo.
echo ╔════════════════════════════════════════════════════════╗
echo ║         Linkis 混合编译模式 (Hybrid Build)             ║
echo ╚════════════════════════════════════════════════════════╝
echo.
echo 编译策略:
echo    [1/2] 并行编译所有模块 (跳过 linkis-dist) - 使用 -T %THREADS%
echo    [2/2] 串行打包 linkis-dist - 确保产物完整
echo.
echo 开始时间: %date% %time%
echo.

REM 记录开始时间
set "START_TIME=%time%"
call :GetSeconds "%START_TIME%" START_SECONDS

REM ============================================================
REM Step 1: 并行编译所有模块（跳过 linkis-dist）
REM ============================================================
echo [1/2] 并行编译所有模块...
echo 执行: mvn clean install -T %THREADS% -DskipTests -pl "!:linkis-dist"
echo.

set "STEP1_START=%time%"
call :GetSeconds "%STEP1_START%" STEP1_START_SEC

call mvn clean install -T %THREADS% -DskipTests -pl "!:linkis-dist"
if %ERRORLEVEL% neq 0 (
    echo.
    echo [错误] 步骤 1 编译失败!
    exit /b 1
)

set "STEP1_END=%time%"
call :GetSeconds "%STEP1_END%" STEP1_END_SEC
set /a "STEP1_TIME=STEP1_END_SEC-STEP1_START_SEC"
if !STEP1_TIME! lss 0 set /a "STEP1_TIME+=86400"
set /a "STEP1_MIN=STEP1_TIME/60"
set /a "STEP1_SEC=STEP1_TIME%%60"

echo.
echo [OK] 步骤 1 完成! 耗时: !STEP1_TIME! 秒 (!STEP1_MIN!分!STEP1_SEC!秒)
echo.

REM ============================================================
REM Step 2: 串行编译 linkis-dist
REM ============================================================
echo [2/2] 串行打包 linkis-dist...
echo 执行: mvn install -pl :linkis-dist -DskipTests
echo.

set "STEP2_START=%time%"
call :GetSeconds "%STEP2_START%" STEP2_START_SEC

call mvn install -pl :linkis-dist -DskipTests
if %ERRORLEVEL% neq 0 (
    echo.
    echo [错误] 步骤 2 编译失败!
    exit /b 1
)

set "STEP2_END=%time%"
call :GetSeconds "%STEP2_END%" STEP2_END_SEC
set /a "STEP2_TIME=STEP2_END_SEC-STEP2_START_SEC"
if !STEP2_TIME! lss 0 set /a "STEP2_TIME+=86400"
set /a "STEP2_MIN=STEP2_TIME/60"
set /a "STEP2_SEC=STEP2_TIME%%60"

echo.
echo [OK] 步骤 2 完成! 耗时: !STEP2_TIME! 秒 (!STEP2_MIN!分!STEP2_SEC!秒)
echo.

REM ============================================================
REM 计算总时间并显示结果
REM ============================================================
set "END_TIME=%time%"
call :GetSeconds "%END_TIME%" END_SECONDS
set /a "TOTAL_TIME=END_SECONDS-START_SECONDS"
if !TOTAL_TIME! lss 0 set /a "TOTAL_TIME+=86400"
set /a "TOTAL_MIN=TOTAL_TIME/60"
set /a "TOTAL_SEC=TOTAL_TIME%%60"

echo ╔════════════════════════════════════════════════════════╗
echo ║                    编译完成!                           ║
echo ╚════════════════════════════════════════════════════════╝
echo.
echo 耗时统计:
echo    步骤 1 (并行编译模块): !STEP1_TIME! 秒 (!STEP1_MIN!分!STEP1_SEC!秒)
echo    步骤 2 (串行打包):     !STEP2_TIME! 秒 (!STEP2_MIN!分!STEP2_SEC!秒)
echo    ────────────────────────────
echo    总耗时: !TOTAL_TIME! 秒 (!TOTAL_MIN!分!TOTAL_SEC!秒)
echo.

REM 检查产物
set "DIST_DIR=linkis-dist\target\apache-linkis-1.8.0-bin"
if exist "%DIST_DIR%" (
    echo 产物信息:
    echo    目录: %DIST_DIR%

    REM 统计文件数
    set "FILE_COUNT=0"
    for /r "%DIST_DIR%" %%f in (*) do set /a "FILE_COUNT+=1"
    echo    文件数: !FILE_COUNT!
    echo.

    REM 检查关键目录
    echo 关键模块检查:
    if exist "%DIST_DIR%\linkis-package\lib\linkis-computation-governance\linkis-cg-engineconnmanager" (
        echo    [OK] linkis-cg-engineconnmanager
    ) else (
        echo    [X] linkis-cg-engineconnmanager (缺失!)
    )
    if exist "%DIST_DIR%\linkis-package\lib\linkis-computation-governance\linkis-cg-entrance" (
        echo    [OK] linkis-cg-entrance
    ) else (
        echo    [X] linkis-cg-entrance (缺失!)
    )
    if exist "%DIST_DIR%\linkis-package\lib\linkis-computation-governance\linkis-cg-linkismanager" (
        echo    [OK] linkis-cg-linkismanager
    ) else (
        echo    [X] linkis-cg-linkismanager (缺失!)
    )
    echo.
)

echo 混合编译完成!
echo    结束时间: %date% %time%
echo.

exit /b 0

REM ============================================================
REM 函数: 将时间转换为秒数
REM ============================================================
:GetSeconds
set "TIME_STR=%~1"
REM 处理时间格式 HH:MM:SS.CC 或 H:MM:SS.CC
for /f "tokens=1-4 delims=:,." %%a in ("%TIME_STR%") do (
    set /a "HOURS=%%a"
    set /a "MINS=%%b"
    set /a "SECS=%%c"
)
set /a "%~2=HOURS*3600+MINS*60+SECS"
exit /b
