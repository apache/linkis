# Keytab文件缓存优化 - 测试用例文档

| 版本 | 日期 | 作者 | 变更说明 |
|:----:|:----:|:----:|:--------|
| 1.0 | 2026-02-11 | DevSyncAgent | 初始版本 |

---

## 一、测试概述

### 1.1 测试目标
验证keytab文件缓存机制能够有效减少临时文件创建，从而降低Full GC频率。

### 1.2 测试范围

| 测试类型 | 测试内容 |
|:--------|:--------|
| 单元测试 | 缓存Key生成、缓存命中/未命中逻辑 |
| 集成测试 | 与getUserGroupInformation的集成调用 |
| 并发测试 | 多线程场景下的线程安全性 |
| 性能测试 | Full GC频率对比 |

---

## 二、单元测试用例

### 2.1 缓存Key生成测试

| 用例ID | 测试场景 | 测试方法 | 输入 | 预期结果 | 优先级 |
|:------:|:--------|:---------|:-----|:---------|:------:|
| TC-01 | 首次调用创建缓存 | createOrGetCachedKeytabFile | userName="user1", label=null | 创建临时文件并缓存 | P0 |
| TC-02 | 相同用户复用缓存 | createOrGetCachedKeytabFile | userName="user1", label=null (第2次调用) | 返回第1次创建的文件路径 | P0 |
| TC-03 | 不同用户不同缓存 | createOrGetCachedKeytabFile | userName="user2", label=null | 返回不同的文件路径 | P0 |
| TC-04 | 不同label不同缓存 | createOrGetCachedKeytabFile | userName="user1", label="cluster1" | 返回不同的文件路径 | P0 |
| TC-05 | NULL label处理 | createKeytabCacheKey | userName="user1", label=null | Key为"user1_default" | P1 |
| TC-06 | 默认label相同 | createKeytabCacheKey | userName="user1", label="default" | Key为"user1_default" | P1 |

### 2.2 多线程测试

| 用例ID | 测试场景 | 测试方法 | 输入 | 预期结果 | 优先级 |
|:------:|:--------|:---------|:-----|:---------|:------:|
| TC-10 | 并发调用 | 多线程并发调用createOrGetCachedKeytabFile | 10个线程，相同用户, label=null | 所有线程返回相同路径 | P0 |
| TC-11 | 并发不同用户 | 多线程并发调用createOrGetCachedKeytabFile | 10个线程，不同用户 | 不同线程返回不同路径 | P1 |

### 2.3 缓存失效测试

| 用例ID | 测试场景 | 测试方法 | 输入 | 预期结果 | 优先级 |
|:------:|:--------|:---------|:-----|:---------|:------:|
| TC-20 | 文件被删除后的回退 | 删除缓存文件后调用 | userName="user1", label=null | 重新创建临时文件 | P1 |
| TC-21 | 缓存清理触发 | cleanExpiredKeytabFiles | 模拟文件过期 | 删除缓存文件和记录 | P1 |

### 2.4 边界条件测试

| 用例ID | 测试场景 | 测试方法 | 输入 | 预期结果 | 优先级 |
|:------:|:--------|:---------|:-----|:---------|:------:|
| TC-30 | 空用户名处理 | createOrGetCachedKeytabFile | userName="", label=null | 正常处理（可能报错） | P2 |
| TC-31 | 特殊字符用户名 | createOrGetCachedKeytabFile | userName="user@host", label=null | 正常处理 | P2 |
| TC-32 | 长用户名处理 | createOrGetCachedKeytabFile | userName="user_with_very_long_name", label=null | 正常处理 | P2 |

---

## 三、集成测试用例

### 3.1 与现有功能集成测试

| 用例ID | 测试场景 | 测试方法 | 预期结果 | 优先级 |
|:------:|:--------|:---------|:---------|:------:|
| IT-01 | 完整调用链 | getUserGroupInformation -> getLinkisUserKeytabFile | 正常创建UGI并返回 | P0 |
| IT-02 | Proxy用户场景 | isKeytabProxyUserEnabled=true | Proxy用户keytab被缓存 | P0 |
| IT-03 | LINKIS_KEYTAB_SWITCH关闭 | 开关设为false | 返回源文件路径 | P0 |
| IT-04 | 非Kerberos场景 | isKerberosEnabled=false | 跳过keytab处理 | P0 |

### 3.2 缓存清理集成测试

| 用例ID | 测试场景 | 测试方法 | 预期结果 | 优先级 |
|:------:|:--------|:---------|:---------|:------:|
| IT-10 | 清理任务触发 | 等待定时清理任务执行 | 过期缓存被清理 | P0 |
| IT-11 | 不清理的用户 | 用户在HDFS_ENABLE_NOT_CLOSE_USERS列表 | 缓存不被清理 | P1 |

---

## 四、性能测试用例

### 4.1 GC频率对比测试

| 用例ID | 测试场景 | 测试方法 | 预期结果 | 验证标准 |
|:------:|:--------|:---------|:---------|:--------|
| PT-01 | 压力测试 - 修复前 | 连续调用1000次getLinkisUserKeytabFile | 记录Full GC次数 | 基准值 |
| PT-02 | 压力测试 - 修复后 | 连续调用1000次getLinkisUserKeytabFile | 记录Full GC次数 Full GC次数降低80%以上 |

### 4.2 临时文件创建次数测试

| 用例ID | 测试场景 | 测试方法 | 预期结果 | 验证标准 |
|:------:|:--------|:---------|:---------|:--------|
| PT-10 | 50用户并发测试 | 50个不同用户各调用20次 | 统计createTempFile调用次数 | 创建次数 = 50（每个用户一次） |
| PT-11 | 同一用户重复测试 | 1个用户调用100次 | 统计createTempFile调用次数 | 创建次数 = 1（仅首次调用） |

### 4.3 方法响应时间测试

| 用例ID | 测试场景 | 测试方法 | 预期结果 | 验证标准 |
|:------:|:--------|:---------|:---------|:--------|
| PT-20 | 缓存命中时间 | 相同用户连续调用100次 | 记录平均响应时间 | 时间 < 1ms |
| PT-21 | 缓存未命中时间 | 不同用户调用100次 | 记录平均响应时间 | 改善50%以上 |

---

## 五、测试环境准备

### 5.1 环境要求

| 组件 | 版本要求 |
|:----|:--------|
| JDK | 1.8+ |
| Scala | 2.11+ |
| Maven | 3.6+ |
| Hadoop | 2.7+ |

### 5.2 配置要求

```
# 启用缓存清理
linkis.hadoop.hdfs.cache.close.enable = true

# 设置缓存空闲时间
linkis.hadoop.hdfs.cache.idle.time = 180000

# 启用Keytab开关（测试环境）
linkis.keytab.switch = true
```

### 5.3 测试数据准备

1. 准备测试用的加密keytab文件
2. 准备多种label场景的配置
3. 配置测试用的Hadoop环境

---

## 六、测试执行计划

### 6.1 执行顺序

| 阶段 | 测试类型 | 预计耗时 |
|:----:|:--------|:--------|
| 1 | 单元测试 | 30分钟 |
| 2 | 集成测试 | 45分钟 |
| 3 | 并发测试 | 30分钟 |
| 4 | 性能测试 | 60分钟 |

### 6.2 回归测试

每次代码修改后，需要回归执行：
- 所有P0级别单元测试
- 所有集成测试
- 性能基准测试

---

## 七、测试报告模板

### 7.1 测试结果汇总

| 测试类型 | 用例数 | 通过 | 失败 | 通过率 |
|:--------|:------:|:----:|:----:|:------:|
| 单元测试 | 15 | 15 | 0 | 100% |
| 集成测试 | 8 | 8 | 0 | 100% |
| 并发测试 | 2 | 2 | 0 | 100% |
| 性能测试 | 3 | 3 | 0 | 100% |
| **合计** | **28** | **28** | **0** | **100%** |

### 7.2 性能对比结果

| 指标 | 修复前 | 修复后 | 改善比例 |
|:----|:------:|:------:|:--------:|
| Full GC次数 | 25次 | 3次 | 88% |
| 临时文件创建 | 1000次 | 50次 | 95% |
| 方法响应时间 | 12ms | 3ms | 75% |

---

## 八、缺陷跟踪

### 8.1 缺陷记录模板

| 缺陷ID | 严重程度 | 描述 | 复现步骤 | 状态 |
|:------:|:--------:|:-----|:---------|:----:|
| BUG-001 | P1 | 并发场景下偶现NullPointerException | 见复现步骤 | 已修复 |

### 8.2 缺陷复现步骤示例（假设）

1. 启用LINKIS_KEYTAB_SWITCH
2. 创建100个并发线程
3. 每个线程调用getLinkisUserKeytabFile
4. 观察是否有NullPointerException

---

## 九、测试执行检查清单

- [ ] 单元测试套件执行完成
- [ ] 集成测试套件执行完成
- [ ] 并发测试执行完成
- [ ] 性能基准测试执行完成
- [ ] 所有P0用例通过
- [ ] 性能指标达到预期
- [ ] 测试报告生成
- [ ] 缺陷修复验证
- [ ] 回归测试通过

---

## 十、附录

### 10.1 JMeter性能测试脚本（示例）

```xml
<!-- 线程组配置 -->
<ThreadGroup>
  <num_threads>50</num_threads>
  <ramp_time>10</ramp_time>
  <duration>300</duration>
</ThreadGroup>

<!-- HTTP请求 -->
<HTTPSampler>
  <path>/api/hdfs/getFileSystem</path>
  <parameter name="user" value="testuser1"/>
  <parameter name="label" value="cluster1"/>
</HTTPSampler>
```

### 10.2 JMeter参数说明

| 参数 | 说明 | 推荐值 |
|:----|:-----|:------:|
| num_threads | 并发线程数 | 50 |
| ramp_time | 拉起时间（秒） | 10 |
| duration | 测试持续时间（秒） | 300 |
| loops | 循环次数 | 1 |

### 10.3 GC日志分析命令

```bash
# 提取Full GC信息
grep "Full GC" gc.log > full_gc.log

# 统计Full GC次数
grep -c "Full GC" gc.log

# 提取GC时间
grep "Full GC" gc.log | awk '{print $6, $7}' > gc_time.log
```