# 阶段0：需求澄清记录

## 澄清问题与回答

### 问题1: webLogin 标识传递方式应该使用哪种？
**回答**: 使用 HTTP Header

**说明**:
- 前端在web页面登录时，在HTTP header中传递 `webLogin` 标识
- 后端从header读取该标识，默认值为 `false`
- 这种方式更符合RESTful规范，不影响现有请求body结构

### 问题2: 拦截 hadoop 用户的方式如何实现？
**回答**: 前缀匹配（推荐）

**说明**:
- 将 `hadoop` 加入现有的 `PROHIBIT_LOGIN_PREFIX` 配置中
- 配置值变为: `hadoop,hduser,shduser`
- 复用现有的前缀匹配逻辑，无需新增配置项

## 确认的需求要点

1. **webLogin标识**:
   - 来源: HTTP Header
   - Header名称: `webLogin`
   - 默认值: `false`
   - 当值为 `true` 时表示Web页面登录

2. **拦截逻辑**:
   - 当 `PROHIBIT_LOGIN_SWITCH=true` 且 `webLogin=true` 时启用拦截
   - 检查用户名是否以系统用户前缀开头
   - 系统用户前缀默认值更新为: `hadoop,hduser,shduser`

3. **不受影响的场景**:
   - Client客户端登录 (webLogin=false 或不传)
   - 其他API渠道登录

4. **错误信息**:
   - 统一返回: "System users are prohibited from logging in（系统用户禁止登录）！"
