<!--
Thank you for sending the PR! We appreciate you spending the time to work on these changes.
You can learn more about contributing to Apache Linkis here: https://linkis.apache.org/community/how-to-contribute
Happy contributing!
-->

### What is the purpose of the change

EngineConn-Core defines the the abstractions and interfaces of the EngineConn core functions.
The Engine Service in Linkis 0.x is refactored, EngineConn will handle the engine connection 
and session management.

### Related issues/PRs

Related issues: close #590 close #591
Related pr:#591


### Brief change log

- Define the core abstraction and interfaces of the EngineConn Factory;
- Define the core abstraction and interfaces of Executor Manager.


### Checklist

- [x] I have read the [Contributing Guidelines on pull requests](https://github.com/facebook/docusaurus/blob/main/CONTRIBUTING.md#pull-requests).
- [ ] I have explained the need for this PR and the problem it solves
- [ ] I have explained the changes or the new features added to this PR
- [ ] I have added tests corresponding to this change
- [ ] I have updated the documentation to reflect this change
- [ ] I have verified that this change is backward compatible (If not, please discuss on the [Linkis mailing list](https://linkis.apache.org/community/how-to-subscribe) first)
- [ ] **If this is a code change**: I have written unit tests to fully verify the new behavior.



<!--

Note

1. Mark the PR title as `[WIP] title` until it's ready to be reviewed.
   如果PR还未准备好被review，请在标题上添加[WIP]标识(WIP work in progress)

2. Always add/update tests for any changes unless you have a good reason.
   除非您有充分的理由，否则任何修改都需要添加/更新测试
   
3. Always update the documentation to reflect the changes made in the PR.
   始终更新文档以反映 PR 中所做的更改  
   
4. After the PR is submitted, please pay attention to the execution result of git action check. 
   If there is any failure, please adjust it in time
   PR提交后，请关注git action check 执行结果，关键的check失败时，请及时修正
   
5. Before the pr is merged, if the commit is missing, you can continue to commit the code
    在未合并前，如果提交有遗漏，您可以继续提交代码 

6. After you submit PR, you can add assistant WeChat, the WeChat QR code is 
   https://user-images.githubusercontent.com/7869972/176336986-d6b9be8f-d1d3-45f1-aa45-8e6adf5dd244.png 
   您提交pr后，可以添加助手微信，微信二维码为
   https://user-images.githubusercontent.com/7869972/176336986-d6b9be8f-d1d3-45f1-aa45-8e6adf5dd244.png

-->
