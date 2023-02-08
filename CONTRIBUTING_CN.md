# 如何参与项目贡献

> 更多信息可以见官网[如何参与项目贡献](https://linkis.apache.org/community/how-to-contribute)

非常感谢贡献 Linkis 项目！在参与贡献之前，请仔细阅读以下指引。

## 一、贡献范畴

### 1.1 Bug 反馈与修复

我们建议无论是 Bug 反馈还是修复，都先创建一个 Issue 来仔细描述 Bug 的状况，以助于社区可以通过 Issue 记录来找到和回顾问题以及代码。Bug 反馈 Issue 通常需要包含**完整描述 Bug 的信息**以及**可复现的场景**，这样社区才能快速定位导致 Bug 的原因并修复它。包含 `#bug` 标签的打开的 Issue 都是需要被修复的。

### 1.2 功能交流、实现、重构

在交流过程中，详细描述新功能（或重构）的细节、机制和使用场景，能够促使它更好更快地被实现（包括测试用例和代码，及 CI/CD 相关工作）。**如果计划实现一个重大的功能（或重构），请务必通过 Issue 或其他方式与核心开发团队进行沟通**，这样大家能以最效率的方式来推进它。包含 `#feature` 标签的打开的 Issue 都是需要被实现的新功能，包含 `#enhancement` 标签打开的 Issue 都是需要改进重构的功能。

### 1.3 Issue 答疑

帮助回答 Issue 中的使用问题是为 Linkis 社区做贡献的一个非常有价值的方式；社区中总会有新用户不断进来，在帮助新用户的同时，也可以展现您的专业知识。

### 1.4 文档改进

Linkis 文档位于[Linkis 官网](https://linkis.apache.org/zh-CN/docs/latest/introduction/) ，文档的补充完善对于 Linkis 的发展也至关重要。

### 1.5 其他

包括参与和帮助组织社区交流、社区运营活动等，其他能够帮助 Linkis 项目和社区的活动。

## 二、贡献流程

### 2.1 分支结构

Linkis 源码可能会产生一些临时分支，但真正有明确意义的只有以下三个分支：  

- master: 最近一次稳定 release 的源码，偶尔会多几次 hotfix 提交；
- release-*: 稳定的 release 版本；
- dev-*: 主要开发分支；

#### 2.1.1 概念

- Upstream 仓库:<https://github.com/apache/linkis> linkis 的 apache 仓库文中称为 Upstream 仓库
- Fork 仓库: 从 <https://github.com/apache/linkis> fork 到自己个人仓库 称为 Fork 仓库  

#### 2.1.2 同步 Upstream 仓库分支最新代码到自己的 Fork 仓库

- step1 进入用户项目页面,选中要更新的分支  
- step2 点击 code 下载按钮下方的 Fetch upstream,选择 Fetch and merge (如自己的 Fork 仓库  该分支不小心污染了，可以删除该分支后，同步 Upstream 仓库新分支到自己的 Fork 仓库  ，参见指引[同步 Upstream 仓库分支最新代码到自己的 Fork 仓库  ](#213-同步 Upstream 仓库新分支到自己的 Fork 仓库  ))
![update-code](https://user-images.githubusercontent.com/7869972/176622158-52da5a80-6d6a-4f06-a099-ff65887d002c.png)

#### 2.1.3 同步 Upstream 仓库新分支到自己的 Fork 仓库  

场景：Upstream 仓库有新增分支，但是 fork 的库没有该分支 (可以选择删除后，重新 fork，但是会丢失未 merge 到原始仓库的变更)

在自己 clone 的本地项目中操作

- step1 添加 apacheUpstream 仓库镜像到本地  

```shell script
git remote add apache git@github.com:apache/linkis.git
```

- step2 拉去 apache 镜像信息到本地  

```shell script
git fetch apache
```

- step3 根据需要同步的新分支来创建本地分支

```shell script
git checkout -b dev-1.1.4 apache/dev-1.1.4
```

- step4 把本地分支 push 到自己的仓库,如果自己的仓库没有 dev-1.1.4 分支，则会创建 dev-1.1.4 分支  

```shell script
git push origin dev-1.1.4:dev-1.1.4
```

- step5 删除 upstream 的分支

```shell script
git remote remove apache
```

- step6 更新分支

```shell script
git pull
```

#### 2.1.4 一个 pr 的流程

- step1 确认当前开发的基础分支（一般是当前进行的中版本，如当前社区开发中的版本 1.1.0，那么分支就是 dev-1.1.0，不确定的话可以在社区群里问下或则在 issue 中@相关同学）

- step2 同步 Upstream 仓库分支最新代码到自己的 Fork 仓库 分支,参见指引 [2.1.2 同步 Upstream 仓库分支最新代码到自己的 Fork 仓库 ]

- step3 基于开发分支，拉取新 fix/feature 分支 (不要直接在原分支上修改，如果后续 pr 以 squash 方式 merge 后，提交的 commit 记录会被合并成一个)

```shell script
git checkout -b dev-1.1.4-fix  dev-1.1.4
git push origin dev-1.1.4-fix:dev-1.1.4-fix
```

- step4  进行开发
- step5  提交 pr(如果是正在进行中,开发还未完全结束，请在 pr 标题上加上 WIP 标识 如 `[WIP] Dev 1.1.1 Add junit test code for [linkis-common]` ;关联对应的 issue 等)
- step6  等待被合并
- step7  删除 fix/future 分支 (可以在 github 页面上进行操作)

```shell script
git branch -d dev-1.1.4-fix
git push
```

请注意：大特性的 dev 分支，在命名时除了版本号，还会加上相应的命名说明，如：dev-0.10.0-flink，指 0.10.0 的 flink 特性开发分支。

### 2.2 开发指引

Linkis 前后端代码共用同一个代码库，但在开发上是分离的。在着手开发之前，请先将 Linkis 项目 fork 一份到自己的 Github Repositories 中， 开发时请基于自己 Github Repositories 中的 Linkis 代码库进行开发。

我们建议克隆 dev 分支命名为 dev-fix 来开发,同时在自己仓库新建 dev-fix 分支，直接在原分支上修改，如果后续 pr 以 squash 方式 merge 后，提交的 commit 记录会被合并成一个

```shell script
#拉取分支
git clone https://github.com/{githubid}/linkis.git --branch dev
#根据 dev 生成本地 dev-fix 分支
git checkout -b dev-fix dev
#把本地 dev-fix 分支推到自己的仓库
git push origin dev-fix dev-fix
```

### 2.3 Issue 提交指引

- 如果您还不知道怎样向开源项目发起 PR，请参考[About issues](https://docs.github.com/en/github/managing-your-work-on-github/about-issues)
- Issue 名称，应一句话简单描述您的问题或建议；为了项目的国际化推广，请用英文，或中英文双语书写 issue
- 每个 Issue，请至少带上 component 和 type 两个 label，如 component=Computation Governance/EngineConn，type=Improvement.参考:[issue #590](https://github.com/apache/linkis/issues/590)

### 2.4 Pull Request(PR) 提交指引

- 如果您还不知道怎样向开源项目发起 PR，请参考[About pull requests](https://docs.github.com/en/github/collaborating-with-issues-and-pull-requests/about-pull-requests)
- 无论是 Bug 修复，还是新功能开发，请将 PR 提交到 dev-* 分支

- PR 和提交名称遵循 `<type>(<scope>): <subject>` 原则，详情可以参考[Commit message 和 Change log 编写指南](https://linkis.apache.org/zh-CN/docs/latest/development/development-specification/commit-message)
- 如果 PR 中包含新功能，理应将文档更新包含在本次 PR 中
- 如果本次 PR 尚未准备好合并，请在名称头部加上 [WIP] 前缀（WIP = work-in-progress）
- 所有提交到 dev-* 分支的提交至少需要经过一次 Review 才可以被合并

### 2.5 Review 标准

在贡献代码之前，可以了解一下什么样的提交在 Review 中是受欢迎的。简单来说，如果一项提交能带来尽可能多增益和尽可能少的副作用或风险，那它被合并的几率就越高，Review 的速度也会越快。风险大、价值低地提交是几乎不可能被合并的，并且有可能会被拒绝 Review。

#### 2.5.1 增益

- 修复导致 Bug 的主要原因
- 添加或修复一个大量用户亟需的功能或问题
- 简单有效
- 容易测试，有测试用例
- 减少复杂度以及代码量
- 经社区讨论过的、确定需要改进的问题

#### 2.5.2 副作用和风险

- 仅仅修复 Bug 的表面现象
- 引入复杂度高的新功能
- 为满足小众需求添加复杂度
- 改动稳定的现有 API 或语义
- 导致其他功能不能正常运行
- 添加大量依赖
- 随意改变依赖版本
- 一次性提交大量代码或改动

#### 2.5.3 Reviewer 注意事项

- 请使用建设性语气撰写评论
- 如果需要提交者进行修改，请明确说明完成此次 Pull Request 所需要修改的所有内容
- 如果某次 PR 在合并后发现带来了新问题，Reviewer 需要向 PR 作者联系并沟通解决问题；如果无法联系到 PR 作者，Reviewer 需要将此次 PR 进行还原

---

## 三、贡献进阶

### 3.1 关于 Committers（Collaborators）

#### 3.1.1 如何成为 Committer

如果您对 Linkis 提过颇具价值的 PR 并且被合并，或是连续贡献超过半年，且至少主导过一次版本的发布，您可以通过官方微信群找到 Linkis 项目的一个 PMC ，如果他愿意提名您为 committer，并愿意为您陈述您的贡献给所有 PMC 和 Committer，那么接下来会发起一次投票；PMC 和其他 Committers 将会一起投票决定是否允许您的加入，如果得到足够票数，您将成为 Linkis 项目的 Committer。

#### 3.1.2 Committer 的权利

- 可以加入官方开发者微信群，参与讨论和制定 Linkis 开发计划
- 可以对 Issue 进行管理，包括关闭、添加标签
- 可以创建和管理项目分支，master、dev-* 分支除外
- 可以对提交到 dev-* 分支的 PR 进行 Review
- 可以申请成为 Committee 成员

### 3.2 关于 Committee

#### 3.2.1 如何成为 Committee 成员

如果您是 Linkis 项目的 Committer，并且您贡献的所有内容得到了其他 Committee 成员的认可，您可以申请成为 Linkis Committee 成员，其他 Committee 成员将会一起投票决定是否允许您的加入，如果全票通过，您将成为 Linkis Committee 成员。

#### 3.2.2 Committee 成员的权利

- 可以合并其他 Committers 和贡献者提交到 dev-** 分支的 PR
- 可以参与决定 Linkis 项目的 roadmap 和发展方向
- 可以参与新版本发布
