# How To Contribute

> For more information, see the official website [How to contribute to the project](https://linkis.apache.org/community/how-to-contribute)

Thank you very much for contributing to the Linkis project! Before participating in the contribution, please read the following guidelines carefully.

## 1. Contribution category

### 1.1 Bug feedback and fixes

We recommend that whether it is a bug feedback or a fix, an issue is first created to describe the bug in detail, so that the community can find and review the problem and code through the issue record. Bug feedback Issues usually need to include **complete description of the bug information** and **reproducible scenarios** so that the community can quickly locate the cause of the bug and fix it. All open issues that contain the `#bug` tag need to be fixed.

### 1.2 Functional communication, implementation, refactoring

In the communication process, a detailed description, mechanisms and usage scenarios of the new function (or refactoring) can promote it to be implemented better and faster (including test cases and codes, and CI/CD related work). **If you plan to implement a major feature (or refactoring), be sure to communicate with the core development team through Issue or other means** so that everyone can promote it in the most efficient way. Issues opened with the label of `#feature` are all new features that need to be implemented, and issues opened with the label of `#enhancement` are all features that need to be improved and refactored.

### 1.3 Issue Q&A

Helping answering the questions in the Linkis community is a very valuable way to contribute; there will always be new users in the community that will keep coming in. While helping new users, you can also show your expertise.

### 1.4 Documentation Refinements

You can find linkis documentations at [linkis-Website](https://linkis.apache.org/docs/latest/introduction), and the supplement of the document is also crucial to the development of Linkis.

### 1.5 Other

Including participating in and helping to organize community exchanges, community operation activities, etc., and other activities that can help the Linkis project and the community.

## 2. How to Contribution

### 2.1 Branch structure

The Linkis source code may have some temporary branches, but only the following three branches are really meaningful:

- master: The source code of the latest stable release, and occasionally several hotfix submissions;
- release-*: stable release version;
- dev-*: main development branch;

#### 2.1.1 Concept

- Upstream repository: <https://github.com/apache/linkis> The apache repository of linkis is called Upstream repository in the text
- Fork repository: From <https://github.com/apache/linkis> fork to your own personal repository called Fork repository

#### 2.1.2   Synchronize Upstream Repository

> Synchronize the latest code of the Upstream repository branch to your own Fork repository

- Step1 Enter the user project page and select the branch to be updated
- Step2 Click Fetch upstream under the code download button, select Fetch and merge (if the branch of your own Fork repository is accidentally polluted, you can delete the branch and synchronize the new branch of the Upstream repository to your own Fork repository, see the guide [Synchronize Upstream] The latest code of the warehouse branch to its own Fork warehouse] (#213-synchronize the new branch of the Upstream warehouse to its own Fork warehouse))
![update-code](https://user-images.githubusercontent.com/7869972/176622158-52da5a80-6d6a-4f06-a099-ff65887d002c.png)

#### 2.1.3  Synchronize New Branch

>Synchronize the new branch of the Upstream repository to your own Fork repository

Scenario: There is a new branch in the Upstream warehouse, but the forked library does not have this branch (you can choose to delete and re-fork, but the changes that have not been merged to the original warehouse will be lost)

Operate in your own clone's local project

- Step1 Add the apacheUpstream repository image to the local

```shell script
git remote add apache git@github.com:apache/linkis.git
```

- Step2 Pull the apache image information to the local

```shell script
git fetch apache
```

- Step3 Create a local branch based on the new branch that needs to be synced

```shell script
git checkout -b dev-1.1.4 apache/dev-1.1.4
```

- Step4 Push the local branch to your own warehouse. If your own warehouse does not have the dev-1.1.4 branch, the dev-1.1.4 branch will be created

```shell script
git push origin dev-1.1.4:dev-1.1.4
```

- Step5 Delete the upstream branch

```shell script
git remote remove apache
```

- Step6 Update the branch

```shell script
git pull
```

#### 2.1.4 The process of a pr

- Step1 Confirm the base branch of the current development (usually the current version in progress, such as the version 1.1.0 currently under development by the community, then the branch is dev-1.1.0, if you are not sure, you can ask in the community group or at @relevant classmates in the issue)

- Step2 Synchronize the latest code of the Upstream warehouse branch to your own Fork warehouse branch, see the guide [2.1.2 Synchronize Upstream Repository]

- Step3 Based on the development branch, pull the new fix/feature branch (do not modify it directly on the original branch, if the subsequent PR is merged in the squash method, the submitted commit records will be merged into one)

```shell script
git checkout -b dev-1.1.4-fix dev-1.1.4
git push origin dev-1.1.4-fix:dev-1.1.4-fix
```

- Step4 Develop
- Step5 Submit pr (if it is in progress and the development has not been completely completed, please add the WIP logo to the pr title, such as `[WIP] Dev 1.1.1 Add junit test code for [linkis-common]`; associate the corresponding issue, etc.)
- Step6 Waiting to be merged
- Step7 Delete the fix/future branch (you can do this on the github page)

```shell script
git branch -d dev-1.1.4-fix
git push
```

Please note: For the dev branch of major features, in addition to the version number, the corresponding naming description will be added, such as: dev-0.10.0-flink, which refers to the flink feature development branch of 0.10.0.

### 2.2 Development Guidelines

Linkis front-end and back-end code share the same code base, but are separated in development. Before starting development, please fork a copy of the Linkis project to your Github Repositories, and develop based on the Linkis code base in your Github Repositories.

We recommend to clone the dev branch and name it dev-fix for development. At the same time, create a new dev-fix branch in your own warehouse and modify it directly on the original branch. If the subsequent PR is merged in the squash method, the submitted commit records will be merged into one

```shell script
#pull the branch
git clone https://github.com/{githubid}/linkis.git --branch dev

#Generate local dev-fix branch according to dev
git checkout -b dev-fix dev

#Push the local dev-fix branch to your own repository
git push origin dev-fix dev-fix
```

### 2.3 Issue submission guidelines

- If you still don’t know how to initiate a PR to an open source project, please refer to [About issues](https://docs.github.com/en/github/managing-your-work-on-github/about-issues)
- Issue name, which should briefly describe your problem or suggestion in one sentence; for the international promotion of the project, please write the issue in English or both Chinese and English
- For each Issue, please bring at least two labels, component and type, such as component=Computation Governance/EngineConn, type=Improvement. Reference: [issue #590](https://github.com/apache/linkis/issues/590)

### 2.4 Pull Request(PR) Submission Guidelines

- If you still don’t know how to initiate a PR to an open source project, please refer to [About pull requests](https://docs.github.com/en/github/collaborating-with-issues-and-pull-requests/about-pull-requests)
  Whether it is a bug fix or a new feature development, please submit a PR to the dev-* branch
- PR and submission name follow the principle of `<type>(<scope>): <subject>`, for details, please refer to [Commit message and Change log writing guide](https://linkis.apache.org/community/development-specification/commit-message)
- If the PR contains new features, the document update should be included in this PR
- If this PR is not ready to merge, please add [WIP] prefix to the head of the name (WIP = work-in-progress)
- All submissions to dev-* branches need to go through at least one review before they can be merged

### 2.5 Review Standard

Before contributing code, you can find out what kind of submissions are popular in Review. Simply put, if a submission can bring as many gains as possible and as few side effects or risks as possible, the higher the probability of it being merged, the faster the review will be. Submissions with high risk and low value are almost impossible to merge, and may be rejected Review.

#### 2.5.1 Gain

- Fix the main cause of the bug
- Add or fix a function or problem that a large number of users urgently need
- Simple and effective
- Easy to test, with test cases
- Reduce complexity and amount of code
- Issues that have been discussed by the community and identified for improvement

#### 2.5.2 Side effects and risks

- Only fix the surface phenomenon of the bug
- Introduce new features with high complexity
- Add complexity to meet niche needs
- Change stable existing API or semantics
- Cause other functions to not operate normally
- Add a lot of dependencies
- Change the dependency version at will
- Submit a large number of codes or changes at once

#### 2.5.3 Reviewer notes

- Please use a constructive tone to write comments
- If you need to make changes by the submitter, please clearly state all the content that needs to be modified to complete the Pull Request
- If a PR is found to have brought new problems after the merger, the Reviewer needs to contact the PR author and communicate to solve the problem; if the PR author cannot be contacted, the Reviewer needs to restore the PR

---

## 3. Outstanding Contributor

### 3.1 About Committers (Collaborators)

#### 3.1.1 How to become Committer

If you have submitted a valuable PR to Linkis and have been merged, or contributed continuously for more than half a year, and have led the release of at least one version, you can find a PMC of the Linkis project through the official WeChat group, if he is willing to nominate you as a committer , And are willing to state your contribution to all PMC and Committer, then a vote will be initiated; PMC and other Committers will vote together to decide whether to allow you to join, if you get enough votes, you will become Committer of the Linkis project .

#### 3.1.2 Committer's

- You can join the official developer WeChat group to participate in discussions and formulate Linkis development plans
- Can manage Issues, including closing and adding tags
- Can create and manage project branches, except for master and dev-* branches
- You can review the PR submitted to the dev-* branch
- Can apply to become a Committee member

### 3.2 About Committee

#### 3.2.1 How to become a Committee member

If you are the Committer of the Linkis project, and all your contributions have been recognized by other Committee members, you can apply to become a member of the Linkis Committee, and other Committee members will vote together to decide whether to allow you to join. If you pass unanimously, you will become a member of the Linkis Committee.

#### 3.2.2 Rights of Committee members

- You can merge PRs submitted by other Committers and contributors to the dev-** branch
- Participate in determining the roadmap and development direction of the Linkis project
- Can participate in the new version release
