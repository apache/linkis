Feature: 全局历史页面引擎版本展示增强
  在全局历史页面的引擎列显示完整的引擎版本信息，解决用户无法区分不同spark引擎版本的问题

  作为Linkis用户
  我希望在全局历史页面的引擎列看到完整的引擎版本信息
  以便了解任务使用的具体引擎版本（如spark-2.4.3或spark-3.4.4）

  Background:
    Given 系统已启动
    And 用户已登录Linkis系统
    And 全局历史页面功能正常

  Rule: 必须保持现有全局历史页面功能不受影响

    @regression @critical
    Scenario: 增强后全局历史页面其他列展示正常
      Given 数据库中有100条历史任务记录
      When 用户访问全局历史页面
      And 查看历史任务列表
      Then 任务列表应该成功加载
      And 除引擎列外的其他列应该显示正确
      And 行为应该与增强前完全一致

    @regression
    Scenario: 增强后筛选功能正常
      Given 全局历史页面已加载
      When 用户使用筛选条件查询任务（如按时间范围、状态等）
      Then 搜索应该成功
      And 结果应该与筛选条件匹配

    @regression
    Scenario: 增强后分页功能正常
      Given 全局历史页面数据量超过一页（共200条记录）
      And 当前页码为1
      When 用户切换到第2页
      Then 应该显示第2页的数据
      And 每页显示数量应该正确

    @regression
    Scenario: 增强后任务详情查看功能正常
      Given 全局历史页面已加载
      When 用户点击某条任务的"查看"按钮
      Then 应该跳转到历史详情页面
      And 详情页应该正确显示该任务的日志、代码、结果等信息

  Rule: 支持在引擎列显示完整的引擎版本信息

    @smoke @new-feature
    Scenario: 成功显示spark-2.4.3引擎版本
      Given 全局历史页面有一条使用spark-2.4.3引擎的任务
      When 用户访问全局历史页面
      Then 引擎列应该显示"LINKISCLI / sql / spark-2.4.3"
      And 用户可以清楚识别引擎版本为spark-2.4.3

    @smoke @new-feature
    Scenario: 成功显示spark-3.4.4引擎版本
      Given 全局历史页面有一条使用spark-3.4.4引擎的任务
      When 用户访问全局历史页面
      Then 引擎列应该显示"LINKISCLI / sql / spark-3.4.4"
      And 用户可以清楚识别引擎版本为spark-3.4.4

    @new-feature
    Scenario: 同一页面显示不同引擎版本的任务
      Given 全局历史页面有5条使用spark-2.4.3引擎的任务
      And 全局历史页面有5条使用spark-3.4.4引擎的任务
      When 用户访问全局历史页面
      Then 引擎列应该正确显示所有任务的引擎版本
      And 用户可以区分不同任务的引擎版本

    @new-feature
    Scenario: 引擎列显示格式正确
      Given 全局历史页面有一条任务
      And 该任务的labels字段为"LINKISCLI/sql/spark-2.4.3"
      When 用户访问全局历史页面
      Then 引擎列应该显示"LINKISCLI / sql / spark-2.4.3"
      And 格式应该为"应用 / 任务类型 / 引擎版本"

  Rule: 引擎版本展示应基于正确的labels字段数据

    @new-feature
    Scenario: 使用labels字段数据
      Given 后端返回的任务数据包含labels字段
      And labels字段格式为层级字符串
      When 全局历史页面加载任务列表
      Then 前端应该正确解析labels字段
      And 引擎列应该基于labels字段的内容显示

  Rule: UI展示应考虑用户体验

    @ui
    Scenario: 列宽度适配内容
      Given 引擎列显示包含完整版本信息
      When 引擎列内容较长时
      Then 列宽度应该能够容纳完整内容
      Or 内容应该合理截断并提供tooltip显示完整信息

    @ui
    Scenario: 保持现有UI风格一致
      Given 全局历史页面引擎列已增强
      When 用户查看引擎列
      Then 样式应该与页面其他列保持一致
      And 字体、颜色、对齐方式应该符合现有规范

  Rule: 所有任务都包含完整的版本信息

    @new-feature
    Scenario: 历史任务都包含版本信息
      Given 全局历史页面有历史任务记录
      When 用户浏览任务列表
      Then 所有任务的引擎列都应该显示完整的版本信息
      And 不应该存在缺失版本信息的情况
