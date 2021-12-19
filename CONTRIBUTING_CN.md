# Contributing

| **版本管理信息表** |                                   |
| ----------- | --------------------------------- |
| 现行版本        | 1.2 版                   |
| 现行版本发布日期    | 2021 年 12 月 17 日                    |
| 修订信息        | 1. 因仓库移交至apache，以及Linkis-Doc文档迁移至linkis官网，修改对应链接 |

非常感谢贡献Linkis项目！在参与贡献之前，请仔细阅读以下指引。

## 一、贡献范畴

### 1.1 Bug 反馈与修复

我们建议无论是 Bug 反馈还是修复，都先创建一个 Issue 来仔细描述 Bug 的状况，以助于社区可以通过 Issue 记录来找到和回顾问题以及代码。Bug 反馈 Issue 通常需要包含**完整描述 Bug 的信息**以及**可复现的场景**，这样社区才能快速定位导致 Bug 的原因并修复它。包含 `#bug` 标签的打开的 Issue 都是需要被修复的。

### 1.2 功能交流、实现、重构

在交流过程中，详细描述新功能（或重构）的细节、机制和使用场景，能够促使它更好更快地被实现（包括测试用例和代码，及CI/CD相关工作）。**如果计划实现一个重大的功能（或重构），请务必通过 Issue 或其他方式与核心开发团队进行沟通**，这样大家能以最效率的方式来推进它。包含 `#feature` 标签的打开的 Issue 都是需要被实现的新功能，包含 `#enhancement` 标签打开的 Issue 都是需要改进重构的功能。

### 1.3 Issue 答疑

帮助回答 Issue 中的使用问题是为 Linkis 社区做贡献的一个非常有价值的方式；社区中总会有新用户不断进来，在帮助新用户的同时，也可以展现您的专业知识。

### 1.4 文档改进

Linkis 文档位于[Linkis官网](https://linkis.apache.org/zh-CN/docs/latest/introduction/) ，文档的补充完善对于Linkis 的发展也至关重要。

### 1.5 其他
包括参与和帮助组织社区交流、社区运营活动等，其他能够帮助Linkis 项目和社区的活动。

---

## 二、贡献流程

### 2.1 分支结构

Linkis 源码可能会产生一些临时分支，但真正有明确意义的只有以下三个分支：  
- master: 最近一次稳定 release 的源码，偶尔会多几次 hotfix 提交；
- release-*: 稳定的release 版本； 
- dev-*: 主要开发分支；
- feature-*: 针对某些较大、需要社区联合开发的新特性的开发分支

请注意：大特性的dev分支，在命名时除了版本号，还会加上相应的命名说明，如：dev-0.10.0-flink，指0.10.0的flink特性开发分支。

### 2.2 开发指引

Linkis 前后端代码共用同一个代码库，但在开发上是分离的。在着手开发之前，请先将 Linkis 项目 fork 一份到自己的 Github Repositories 中， 开发时请基于自己 Github Repositories 中的 Linkis 代码库进行开发。

我们建议克隆 dev-* 分支来开发，这样在向 Linkis 主项目提交 PR 时合并冲突的可能性会小很多

```bash
git clone https://github.com/yourname/incubator-linkis.git --branch dev-*
```

#### 2.2.1 后端

用户配置在项目根目录 /config/ 下，项目启动脚本和升级补丁脚本在项目根目录 /bin/ 下， 后端代码及核心配置在 server/ 目录下, 日志在项目根目录 /log/ 下。注意：此处所指项目根目录都指环境变量 LINKIS_HOME 所配置的目录，在使用 IDE 开发过程中也需要配置环境变量，如 Idea 关于环境变量加载的优先级：`Run/Debug Configurations` 中配置的 `Environment variables` —>  IDE缓存的系统环境变量。

##### 2.2.1.1 目录结构

1. 脚本

```
├── bin                   # 脚本目录
  ├── install.sh              # 一键部署脚本
  ├── start-all.sh   # 一键启动脚本
  └── stop-all.sh    # 一键停止脚本
```

2. 配置

```
├── config                          # 用户配置目录
  ├── config.sh         # 一键部署的配置文件
  ├── db.sh   # 一键部署的数据库配置
```

3. 代码目录结构
   
   详见 [Linkis代码目录结构](https://linkis.apache.org/zh-CN/docs/latest/deployment/sourcecode_hierarchical_structure)

4. 日志目录

```
├── logs        # 日志根目录
```

##### 2.2.1.2 环境变量

     配置系统环境变量或 IDE 环境变量 LINKIS_HOME，推荐优先使用 IDE 环境变量。

##### 2.2.1.3 数据库

1. 自行创建 Linkis 系统数据库；
2. 修改 conf/db.sh 中的数据库相应信息并执行bin/install.sh 或 直接在数据库客户端导入 db/linkis_*.sql。

##### 2.2.1.4 配置文件

   修改 每个微服务resources/ 目录下 `application.yml` 文件，配置相关属性。

##### 2.2.1.5 打包

1. 打完整 release 包需要修改根目录下 /assembly/src/main/assembly/assembly.xml 中相关版本信息，然后在根目录下执行: `mvn clean package` 即可；
2. 打 每个模块 的包可直接在 模块目录下执行 `mvn clean package`。

### 2.3 Issue 提交指引
- 如果您还不知道怎样向开源项目发起 PR，请参考[About issues](https://docs.github.com/en/github/managing-your-work-on-github/about-issues)
- Issue 名称，应一句话简单描述您的问题或建议；为了项目的国际化推广，请用英文，或中英文双语书写 issue.
- 每个Issue，请至少带上component 和type 两个label，如component=Computation Governance/EngineConn，type=Improvement.参考:[issue #590](https://github.com/apache/incubator-linkis/issues/590)

### 2.3 Pull Request(PR) 提交指引

- 如果您还不知道怎样向开源项目发起 PR，请参考[About pull requests](https://docs.github.com/en/github/collaborating-with-issues-and-pull-requests/about-pull-requests)
- 无论是 Bug 修复，还是新功能开发，请将 PR 提交到 dev-* 分支。
- PR 和提交名称遵循 `<type>(<scope>): <subject>` 原则，详情可以参考阮一峰的 [Commit message 和 Change log 编写指南](http://www.ruanyifeng.com/blog/2016/01/commit_message_change_log.html) 这篇文章。
- 如果 PR 中包含新功能，理应将文档更新包含在本次 PR 中。
- 如果本次 PR 尚未准备好合并，请在名称头部加上 [WIP] 前缀（WIP = work-in-progress）。
- 所有提交到 dev-* 分支的提交至少需要经过一次 Review 才可以被合并

### 2.4 Review 标准

在贡献代码之前，可以了解一下什么样的提交在 Review 中是受欢迎的。简单来说，如果一项提交能带来尽可能多增益和尽可能少的副作用或风险，那它被合并的几率就越高，Review 的速度也会越快。风险大、价值低的提交是几乎不可能被合并的，并且有可能会被拒绝 Review。

#### 2.4.1 增益

- 修复导致 Bug 的主要原因
- 添加或修复一个大量用户亟需的功能或问题
- 简单有效
- 容易测试，有测试用例
- 减少复杂度以及代码量
- 经社区讨论过的、确定需要改进的问题

#### 2.4.2 副作用和风险

- 仅仅修复 Bug 的表面现象
- 引入复杂度高的新功能
- 为满足小众需求添加复杂度
- 改动稳定的现有API或语义
- 导致其他功能不能正常运行
- 添加大量依赖
- 随意改变依赖版本
- 一次性提交大量代码或改动

#### 2.4.3 Reviewer 注意事项

- 请使用建设性语气撰写评论
- 如果需要提交者进行修改，请明确说明完成此次 Pull Request 所需要修改的所有内容
- 如果某次 PR 在合并后发现带来了新问题，Reviewer 需要向 PR 作者联系并沟通解决问题；如果无法联系到 PR 作者，Reviewer 需要将此次 PR 进行还原

---

## 三、贡献进阶

### 3.1 关于 Committers（Collaborators）

#### 3.1.1 如何成为 Committer

如果您对 Linkis 提过颇具价值的 PR 并且被合并，或是连续贡献超过半年，且至少主导过一次版本的发布，您可以通过官方微信群找到Linkis项目的一个 PMC ，如果他愿意提名您为 committer，并愿意为您陈述您的贡献给所有 PMC和Committer，那么接下来会发起一次投票；PMC和其他 Committers 将会一起投票决定是否允许您的加入，如果得到足够票数，您将成为 Linkis 项目的 Committer。

#### 3.1.2 Committer 的权利

- 可以加入官方开发者微信群，参与讨论和制定Linkis开发计划
- 可以对 Issue 进行管理，包括关闭、添加标签
- 可以创建和管理项目分支，master、dev-* 分支除外
- 可以对提交到 dev-* 分支的 PR 进行 Review
- 可以申请成为 Committee 成员

### 3.2 关于 Committee

#### 3.2.1 如何成为 Committee 成员

如果您是 Linkis 项目的 Committer，并且您贡献的所有内容得到了其他 Committee 成员的认可，您可以申请成为 Linkis Committee 成员，其他 Committee 成员将会一起投票决定是否允许您的加入，如果全票通过，您将成为 Linkis Committee 成员。

#### 3.2.2 Committee 成员的权利

- 可以合并其他 Committers 和贡献者提交到 dev-** 分支的 PR
- 可以参与决定Linkis项目的roadmap和发展方向
- 可以参与新版本发布
