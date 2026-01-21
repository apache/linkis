# 任务上下文

## 基本信息
- **任务名称**: spark-task-diagnosis
- **需求类型**: NEW (新增功能)
- **创建时间**: 2025-12-24
- **当前阶段**: 已完成
- **执行模式**: 快速模式
- **状态**: 已完成

## 需求摘要
在jobhistory模块中添加接口，用于将诊断信息更新至linkis_ps_job_history_diagnosis表中，诊断信息存入diagnosisContent，diagnosisSource存入doctoris，然后在entrance诊断之后调用该接口更新诊断结果。

## 已完成阶段
- [x] 阶段0: 需求澄清 - 确认需求细节和实现方式
- [x] 阶段1: 需求分析 - 生成需求分析文档
- [x] 阶段2: 设计方案 - 生成技术设计方案
- [x] 阶段3: 代码开发 - 完成代码修改
- [x] 阶段4: 测试用例 - 生成测试用例文档

## 代码变更

### 修改的文件
1. **JobReqDiagnosisUpdate.scala**
   - 新增RPC协议类，用于封装诊断更新请求
   - 包含jobHistoryId、diagnosisContent、diagnosisSource三个字段
   - 提供apply方法用于快速创建实例

2. **JobHistoryQueryServiceImpl.scala**
   - 新增updateDiagnosis方法，使用@Receiver注解接收RPC请求
   - 实现诊断记录的创建和更新逻辑
   - 支持根据jobHistoryId和diagnosisSource查询诊断记录
   - 修复setUpdatedTime方法名错误，改为正确的setUpdatedDate

3. **EntranceServer.scala**
   - 在任务诊断完成后，调用updateDiagnosis接口更新诊断结果
   - 构造JobReqDiagnosisUpdate请求，设置diagnosisSource为"doctoris"
   - 通过RPC发送请求到jobhistory服务

## 配置说明

```properties
# 任务诊断开关
linkis.task.diagnosis.enable=true

# 任务诊断引擎类型
linkis.task.diagnosis.engine.type=spark

# 任务诊断超时时间（毫秒）
linkis.task.diagnosis.timeout=300000
```

## 调用流程
1. EntranceServer定时检查运行超时的Spark任务
2. 对超时任务调用doctoris实时诊断API
3. 诊断完成后，通过RPC调用jobhistory的updateDiagnosis接口
4. Jobhistory服务将诊断结果存入linkis_ps_job_history_diagnosis表
5. 前端或其他服务可以通过查询该表获取诊断结果