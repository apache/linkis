## linkis-udf-common   errorcode

| module name(模块名) | error code(错误码)  | describe(描述) |enumeration name(枚举)| Exception Class(类名)|
| -------- | -------- | ----- |-----|-----|
|linkis-udf-common  |202011|UserName is empty(用户名为空)|USER_NAME_EMPTY|UdfCommonErrorCodeSummary|
|linkis-udf-common  |202011|UdfId is empty(udfId为空)|UDFID_EMPTY|UdfCommonErrorCodeSummary|
|linkis-udf-common  |202011|Only manager can share udf(只有manager可以共享 udf)|ONLY_MANAGER_SHARE|UdfCommonErrorCodeSummary|
|linkis-udf-common  |202011|Only manager can publish udf(只有manager可以发布 udf)|ONLY_MANAGER_PUBLISH|UdfCommonErrorCodeSummary|
|linkis-udf-common  |202011|Can't find udf by this id(通过这个 id 找不到 udf)|NOT_FIND_UDF|UdfCommonErrorCodeSummary|
|linkis-udf-common  |202011|UserList cat not be null(userList cat 不为空)|USERLIST_NOT_NULL|UdfCommonErrorCodeSummary|
|linkis-udf-common  |202011|The handover user can't be null(切换用户不能为空)|HANDOVER_NOT_NULL|UdfCommonErrorCodeSummary|
|linkis-udf-common  |202011|Admin users cannot hand over UDFs to regular users.(管理员用户不能移交UDF给普通用户！)|ADMIN_NOT_HANDOVER_UDF|UdfCommonErrorCodeSummary|
|linkis-udf-common  |202011|CreateUser must be consistent with the operation user(创建用户必须和操作用户一致)|MUST_OPERATION_USER|UdfCommonErrorCodeSummary|
|linkis-udf-common  |202011|The name of udf is the same name. Please rename it and rebuild it.(udf的名字重名，请改名后重建)|UDF_SAME_NAME|UdfCommonErrorCodeSummary|
|linkis-udf-common  |202011|Category name cannot be empty!(分类名不能为空！)|CATEGORY_NOT_EMPTY|UdfCommonErrorCodeSummary|
|linkis-udf-common  |202011|File read exception(文件读取异常):|FILE_READ_EXCEPTION|UdfCommonErrorCodeSummary|
|linkis-udf-common  |202011|Upload to bml failed, file path is(上传到bml失败，文件路径为):|UPLOAD_BML_FAILED|UdfCommonErrorCodeSummary|
|linkis-udf-common  |202011|Upload to bml failed, msg(上传到bml异常！msg):|UPLOAD_BML_FAILED_MSG|UdfCommonErrorCodeSummary|
|linkis-udf-common  |202011|A jar with the same name already exists in the user's udf!(用户的udf已存在同名jar！)|JAR_SAME_NAME|UdfCommonErrorCodeSummary|
|linkis-udf-common  |202011|id Can not be empty(不能为空)|ID_NOT_EMPTY|UdfCommonErrorCodeSummary|
|linkis-udf-common  |202011|This user does not have a personal function directory!(该用户没有个人函数目录!)|NOT_FUNCTION_DIRECTORY|UdfCommonErrorCodeSummary|
|linkis-udf-common  |202011|Current user must be consistent with the modified user(当前用户必须和修改用户一致)|CURRENT_MUST_MODIFIED|UdfCommonErrorCodeSummary|
|linkis-udf-common  |202011|Current user must be consistent with the user created(当前用户必须和创建用户一致)|CURRENT_MUST_MODIFIED_CREATED|UdfCommonErrorCodeSummary|
|linkis-udf-common  |202011|UDF type modification is not allowed(不允许修改 UDF 类型)|UDF_MODIFICATION_NOT_ALLOWED|UdfCommonErrorCodeSummary|
|linkis-udf-common  |202011|The name of udf is not allowed modified.(udf的名字禁止修改！)|NOT_ALLOWED_MODIFIED|UdfCommonErrorCodeSummary|
|linkis-udf-common  |202011|Can't found latestVersion for the udf(找不到 udf 的最新版本)|NOT_FOUND_VERSION|UdfCommonErrorCodeSummary|
|linkis-udf-common  |202011|The handoverUser has same name udf.(被移交用户包含重名udf)|HANDOVER_SAME_NAME_UDF|UdfCommonErrorCodeSummary|
|linkis-udf-common  |202011|The publish operation is not supported for non-shared udfs!(非共享udf不支持发布操作！)|OPERATION_NOT_SUPPORTED|UdfCommonErrorCodeSummary|
|linkis-udf-common  |202011|Bml rollback version abnormal(bml回滚版本异常)|ROLLBACK_VERSION_ABNORMAL|UdfCommonErrorCodeSummary|
|linkis-udf-common  |202011|The udf of jar type content does not support downloading and viewing(jar类型内容的udf不支持下载查看)|JAR_NOT_DOWNLOADING|UdfCommonErrorCodeSummary|
|linkis-udf-common  |202011|There is a Jar package function with the same name(存在同名的Jar包函数)： |SAME_NAME_FUNCTION|UdfCommonErrorCodeSummary|
|linkis-udf-common  |202011|Do not support sharing to yourself!(不支持分享给自己!) |NOT_SHARING_YOURSELF|UdfCommonErrorCodeSummary|
|linkis-udf-common  |202011|Duplicate file name(文件名重复)|DUPLICATE_FILE_NAME|UdfCommonErrorCodeSummary|
|linkis-udf-common  |202011|Failed to copy resource to anotherUser(无法将资源复制到另一个用户):|FAILED_COPY_RESOURCE|UdfCommonErrorCodeSummary|
|linkis-udf-common  |202011|Shared users cannot contain empty usernames(共享用户不能包含空用户名)|SHARED_USERS_NOT_EMPTY|UdfCommonErrorCodeSummary|
|linkis-udf-common  |202011|Username can only contain alphanumeric underscores(用户名只能包含字母数字下划线)|CONTAIN_NUMERIC_SCORES|UdfCommonErrorCodeSummary|
|linkis-udf-common  |202011|User contains the same name udf!(用户包含同名udf！)|CONTAINS_SAME_NAME|UdfCommonErrorCodeSummary|
|linkis-udf-common  |202011|Shared udf name Already exists, please edit the name and re-share(分享的udf的名字已存在，请修改名字后重新进行分享)|ALREADY_EXISTS_SHARE|UdfCommonErrorCodeSummary|
|linkis-udf-common  |202011|Only the udf loaded by the shared user can be set to expire(只有被共享用户加载的udf可以设置过期)|SHARED_USER_EXPIRE|UdfCommonErrorCodeSummary|
|linkis-udf-common  |202011|User There are two root directory directories,user(用户存在两个根目录目录,用户):|DIRECTORY_DIRECTORIES|UdfCommonErrorCodeSummary|
|linkis-udf-common  |202011|User the parent directory is not yours,user(用户父目录不是你的,用户):|DIRECTORY_PARENT|UdfCommonErrorCodeSummary|

