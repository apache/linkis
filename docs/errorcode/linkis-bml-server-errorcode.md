## linkis-bml-server  errorcode

| module name(模块名) | error code(错误码)  | describe(描述) |enumeration name(枚举)| Exception Class(类名)|
| -------- | -------- | ----- |-----|-----|
|linkis-bml-server |60036|Store cannot be accessed without login or expired login(未登录或登录过期，无法访问物料库)|CANNOT_ACCESSED_EXPIRED|BmlServerErrorCodeSummary|
|linkis-bml-server |60050|The first upload of the resource failed(首次上传资源失败)|FIRST_UPLOAD_FAILED|BmlServerErrorCodeSummary|
|linkis-bml-server |60051|Failed to update resources(更新资源失败)|FAILED_UPDATE_RESOURCES|BmlServerErrorCodeSummary|
|linkis-bml-server |70068|Failed to download the resource(下载资源失败)|FAILED_DOWNLOAD_RESOURCE|BmlServerErrorCodeSummary|
|linkis-bml-server |70068|Sorry, the query for version information failed(抱歉，查询版本信息失败)(下载资源失败)|QUERY_VERSION_FAILED|BmlServerErrorCodeSummary|
|linkis-bml-server |70068|Failed to get all system resource information(获取系统所有资源信息失败)|FAILED_ALL_INFORMATION|BmlServerErrorCodeSummary|
|linkis-bml-server |70068| Failed to delete the resource version(删除资源版本失败)|DELETE_VERSION_FAILED|BmlServerErrorCodeSummary|
|linkis-bml-server |70068|Delete resource operation failed(删除资源操作失败)|DELETE_OPERATION_FAILED|BmlServerErrorCodeSummary|
|linkis-bml-server |70068|The bulk delete resource operation failed(批量删除资源操作失败)|BULK_DELETE_FAILED|BmlServerErrorCodeSummary|
|linkis-bml-server |70068|ResourceID:{0} is empty, illegal or has been deleted (resourceId{0}:为空,非法或者已被删除!)|RESOURCEID_BEEN_DELETED|BmlServerErrorCodeSummary|
|linkis-bml-server |70068|version :{} is empty, illegal or has been deleted (version:为空,非法或者已被删除!)|VERSION_BEEN_DELETED|BmlServerErrorCodeSummary|
|linkis-bml-server |70068|Failed to obtain resource basic information (获取资源基本信息失败)|FAILED_RESOURCE_BASIC|BmlServerErrorCodeSummary|
|linkis-bml-server |78531| has expired and cannot be downloaded(已经过期,不能下载)|EXPIRED_CANNOT_DOWNLOADED|BmlServerErrorCodeSummary|
|linkis-bml-server |75569|You do not have permission to download this resource (您没有权限下载此资源)|NOT_HAVE_PERMISSION|BmlServerErrorCodeSummary|
|linkis-bml-server |75570| {} does not have edit permission on project {}. Upload resource failed ({} 对工程 {} 没有编辑权限, 上传资源失败)|NOT_PROJECT_PERMISSION|BmlServerErrorCodeSummary|
|linkis-bml-server |78361|If the material has not been uploaded or has been deleted, please call the upload interface first(resourceId:{}之前未上传物料,或物料已被删除,请先调用上传接口.!)|MATERIAL_NOTUPLOADED_DELETED|BmlServerErrorCodeSummary|
|linkis-bml-server |78361|Bulk deletion of  resourceIDS parameters is null(批量删除资源操作传入的resourceIds参数为空)|PARAMETERS_IS_NULL|BmlServerErrorCodeSummary|
|linkis-bml-server |78361|Bulk deletion of unpassed resourceIDS parameters(批量删除未传入resourceIds参数)|BULK_DELETION_PARAMETERS|BmlServerErrorCodeSummary|
|linkis-bml-server |78361|You did not pass a valid ResourceID(您未传入有效的resourceId)|NOT_BALID_RESOURCEID|BmlServerErrorCodeSummary|
|linkis-bml-server |78361|The passed ResourceID or version is illegal or has been deleted(传入的resourceId或version非法,或已删除)|ILLEGAL_OR_DELETED|BmlServerErrorCodeSummary|
|linkis-bml-server |78361|ResourceID and version are required to delete the specified version(删除指定版本，需要指定resourceId 和 version)|DELETE_SPECIFIED_VERSION|BmlServerErrorCodeSummary|
|linkis-bml-server |78361|The ResourceID you submitted is invalid (您提交的resourceId无效)|SUBMITTED_INVALID|BmlServerErrorCodeSummary|
|linkis-bml-server |78361|The basic information of the resource is not passed into the ResourceId parameter or the parameter is illegal(获取资源基本信息未传入resourceId参数或参数非法)|PARAMETER_IS_ILLEGAL|BmlServerErrorCodeSummary|

