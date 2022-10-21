## linkis-gateway-core  errorcode

| module name(模块名) | error code(错误码)  | describe(描述) |enumeration name(枚举)| Exception Class(类名)|
| -------- | -------- | ----- |-----|-----|
|linkis-gateway-core |11010|Cannot find a correct serviceId for parsedServiceId {}, service list is:{}(无法为 parsedServiceId {} 找到正确的 serviceId，服务列表为：{})|CANNOT_SERVICEID|LinkisGatewayCoreErrorCodeSummary|
|linkis-gateway-core |11011|Cannot route to the corresponding service, URL: {} RouteLabel: {}(无法路由到相应的服务，URL：{} RouteLabel: {})|CANNOT_ROETE_SERVICE|LinkisGatewayCoreErrorCodeSummary|
|linkis-gateway-core |11011|There are no services available in the registry URL (注册表 URL 中没有可用的服务):|NO_SERVICES_REGISTRY|LinkisGatewayCoreErrorCodeSummary|
|linkis-gateway-core |11011|There is no route label service with the corresponding app name (没有对应app名称的路由标签服务)|NO_ROUTE_SERVICE|LinkisGatewayCoreErrorCodeSummary|
|linkis-gateway-core |11012|Cannot find an instance in the routing chain of serviceId {} , please retry (在 serviceId {} 的路由链中找不到实例，请重试)|CANNOT_INSTANCE|LinkisGatewayCoreErrorCodeSummary|
|linkis-gateway-core |18000|get requestBody failed!(获取 requestBody 失败！)|GET_REQUESTBODY_FAILED|LinkisGatewayCoreErrorCodeSummary|