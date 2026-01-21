# 需求澄清文档

## 基本信息
- **需求名称**: Spark任务诊断结果更新接口
- **需求类型**: 新增功能
- **澄清日期**: 2025-12-25
- **状态**: 已确认

## 原始需求描述
在jobhistory加一个接口用于将诊断信息更新至linkis_ps_job_history_diagnosis表中，诊断信息存入diagnosisContent，diagnosisSource存入doctoris，然后这个接口用在entrance诊断之后的更新。

## 澄清问题与解答

### 1. 接口调用时机
**问题**: 接口在什么时候被调用？
**解答**: 在EntranceServer中，当Spark任务运行超过配置的超时时间（默认5分钟），会触发诊断逻辑，诊断完成后调用该接口更新诊断结果。

### 2. 诊断信息格式
**问题**: diagnosisContent字段的内容格式是什么？
**解答**: diagnosisContent字段存储诊断结果的JSON字符串，包含诊断结论、建议等详细信息。

### 3. 幂等性处理
**问题**: 多次调用同一任务的诊断更新接口，如何处理？
**解答**: 系统会根据jobHistoryId和diagnosisSource查询是否已存在诊断记录，如果存在则更新，不存在则创建，确保幂等性。

### 4. 诊断来源标识
**问题**: diagnosisSource字段除了"doctoris"外，是否还支持其他值？
**解答**: 目前主要支持"doctoris"作为诊断来源，后续可以扩展支持其他诊断系统。

### 5. 错误处理
**问题**: 接口调用失败时如何处理？
**解答**: 接口内部会捕获异常并返回错误信息，调用方（EntranceServer）会记录日志，但不会影响主流程。

## 确认的需求细节

1. **功能需求**
   - ✅ 新增RPC接口用于更新诊断信息
   - ✅ 支持诊断记录的创建和更新
   - ✅ 接口参数包括jobHistoryId、diagnosisContent、diagnosisSource
   - ✅ diagnosisSource固定为"doctoris"

2. **非功能需求**
   - ✅ 接口响应时间要求：< 500ms
   - ✅ 接口可用性要求：99.9%
   - ✅ 支持高并发调用

3. **数据需求**
   - ✅ 诊断信息存储在linkis_ps_job_history_diagnosis表
   - ✅ 表字段包括id、jobHistoryId、diagnosisContent、createdTime、updatedTime、onlyRead、diagnosisSource

4. **调用流程**
   - ✅ EntranceServer触发任务诊断
   - ✅ 调用doctoris诊断API获取结果
   - ✅ 构造诊断更新请求
   - ✅ 调用jobhistory的updateDiagnosis接口
   - ✅ jobhistory服务更新诊断记录

## 需求确认

### 业务方确认
- [x] 需求已澄清，无歧义
- [x] 功能范围已确认
- [x] 技术实现方案已达成共识

### 开发方确认
- [x] 需求可实现
- [x] 技术方案可行
- [x] 风险可控

## 后续步骤
1. 进入需求分析阶段，生成详细的需求分析文档
2. 进入设计阶段，生成技术设计方案
3. 进入开发阶段，实现接口和相关功能
4. 进入测试阶段，编写测试用例并执行测试