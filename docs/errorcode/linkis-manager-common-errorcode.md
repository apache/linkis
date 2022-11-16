## linkis-manager-common  errorcode

| module name(模块名) | error code(错误码)  | describe(描述) |enumeration name(枚举)| Exception Class(类名)|
| -------- | -------- | ----- |-----|-----|
|linkis-manager-common |10022|The tag resource was created later than the used resource was created(无需清理该标签的资源,该标签资源的创建时间晚于已用资源的创建时间)|RESOURCE_LATER_CREATED|ManagerCommonErrorCodeSummary|
|linkis-manager-common |11002|Unsupported operation: multiplied(不支持的操作：multiplied)|OPERATION_MULTIPLIED|ManagerCommonErrorCodeSummary|
|linkis-manager-common |11003|Not supported resource result policy (不支持的资源结果策略)|NOT_RESOURCE_POLICY|ManagerCommonErrorCodeSummary|
|linkis-manager-common |11003|Not supported resource result type(不支持的资源结果类型)|NOT_RESOURCE_RESULT_TYPE|ManagerCommonErrorCodeSummary|
|linkis-manager-common |11003|Not supported resource type:{0}(不支持的资源类型)|NOT_RESOURCE_TYPE|ManagerCommonErrorCodeSummary|
|linkis-manager-common |11003|Not supported resource serializable string(不支持资源可序列化字符串)|NOT_RESOURCE_STRING|ManagerCommonErrorCodeSummary|
|linkis-manager-common |11006|Failed to request external resource(请求外部资源失败)|FAILED_REQUEST_RESOURCE|ManagerCommonErrorCodeSummary|
|linkis-manager-common |11006|Get the Yarn queue information exception(获取Yarn队列信息异常)|YARN_QUEUE_EXCEPTION|ManagerCommonErrorCodeSummary|
|linkis-manager-common |11006|Get the Yarn Application information exception.(获取Yarn Application信息异常)|YARN_APPLICATION_EXCEPTION|ManagerCommonErrorCodeSummary|
|linkis-manager-common |11006|Only support fairScheduler or capacityScheduler, not support schedulerType:{0}(仅支持 fairScheduler 或 capacityScheduler)|YARN_NOT_EXISTS_QUEUE|ManagerCommonErrorCodeSummary|
|linkis-manager-common |11006|Only support fairScheduler or capacityScheduler, schedulerType(仅支持 fairScheduler 或 capacityScheduler、schedulerType):|ONLY_SUPPORT_FAIRORCAPA|ManagerCommonErrorCodeSummary|
|linkis-manager-common |11006|Get active Yarn resourcemanager from:{0} exception.(从 {0} 获取主 Yarn resourcemanager 异常)|GET_YARN_EXCEPTION|ManagerCommonErrorCodeSummary|
|linkis-manager-common |11201| No resource available found for em:{0}(没有为 em 找到可用的资源)|NO_RESOURCE_AVAILABLE|ManagerCommonErrorCodeSummary|
|linkis-manager-common |11201|The resource tag has no resource, please check the resource in the database. Label(资源标签没有资源,请检查数据库中的资源.标签):{0}|NO_RESOURCE|ManagerCommonErrorCodeSummary|
|linkis-manager-common |110012|No ExternalResourceRequester found for resource type:{0}(找不到资源类型的 ExternalResourceRequester)|NO_FOUND_RESOURCE_TYPE|ManagerCommonErrorCodeSummary|
|linkis-manager-common |110013|No suitable ExternalResourceProvider found for cluster:{0}(没有为集群找到合适的 ExternalResourceProvider)|NO_SUITABLE_CLUSTER|ManagerCommonErrorCodeSummary|
|linkis-manager-common |110022|Resource label:{0} has no usedResource, please check, refuse request usedResource(资源标签：{0}没有usedResource，请检查，拒绝请求usedResource)|REFUSE_REQUEST|ManagerCommonErrorCodeSummary|
|linkis-manager-common |120010|Only admin can read all user's resource.(只有管理员可以读取所有用户的资源.)|ONLY_ADMIN_READ|ManagerCommonErrorCodeSummary|
|linkis-manager-common |120011|Only admin can reset user's resource.(只有管理员可以重置用户的资源.)|ONLY_ADMIN_RESET|ManagerCommonErrorCodeSummary|