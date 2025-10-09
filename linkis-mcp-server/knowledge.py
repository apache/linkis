from typing import List, Dict, Optional

class KnowledgeEntry:
    def __init__(self, id: str, question: str, answer: str,
                 tags: Optional[List[str]] = None, aliases: Optional[List[str]] = None):
        self.id = id
        self.question = question
        self.answer = answer
        self.tags = tags or []
        self.aliases = aliases or []

    def matches(self, query: str) -> bool:
        query_lower = query.lower()
        return (query_lower in self.question.lower() or
                any(query_lower in alias.lower() for alias in self.aliases) or
                query_lower == self.id.lower())


class KnowledgeQA:
    def __init__(self):
        self._entries: List[KnowledgeEntry] = []
        self._load_entries()

    def _load_entries(self):
        raw_data = [
            {
                "id": "api-entrance-submit-sql",
                "question": "如何提交一条 SQL 任务",
                "answer": "POST /api/rest_j/v1/entrance/submit\n必填: executionContent.code=SQL文本, executionContent.runType=sql, labels.engineType(如 hive-1.2.1), labels.userCreator(如 IDE_user)",
                "tags": ["api", "task", "sql"],
                "aliases": ["提交SQL任务", "SQL任务提交接口", "SQL 提交"]
            },
            {
                "id": "api-entrance-submit-script",
                "question": "如何提交脚本任务并指定语言",
                "answer": "POST /api/rest_j/v1/entrance/submit\nexecutionContent.code=脚本, executionContent.runType=python/shell/spark, labels.engineType 与引擎匹配",
                "tags": ["api", "task", "script"],
                "aliases": ["提交脚本", "提交 Python 任务", "提交 Shell 任务"]
            },
            {
                "id": "api-entrance-status",
                "question": "如何查询任务状态",
                "answer": "GET /api/rest_j/v1/entrance/{execID}/status\n路径参数: execID=任务ID",
                "tags": ["api", "task", "status"],
                "aliases": ["任务状态查询", "查询状态"]
            },
            {
                "id": "api-entrance-progress",
                "question": "如何查询任务进度",
                "answer": "GET /api/rest_j/v1/entrance/{execID}/progress\n返回阶段进度与总进度",
                "tags": ["api", "task", "progress"],
                "aliases": ["任务进度", "进度查询"]
            },
            {
                "id": "api-entrance-log",
                "question": "如何获取任务日志",
                "answer": "GET /api/rest_j/v1/entrance/{execID}/log?fromLine=1&size=200\n参数: fromLine 起始行, size 行数",
                "tags": ["api", "task", "log"],
                "aliases": ["查看日志", "拉取日志"]
            },
            {
                "id": "api-entrance-result-list",
                "question": "如何列出任务的结果集",
                "answer": "GET /api/rest_j/v1/entrance/{execID}/resultset\n返回结果集索引与基本信息",
                "tags": ["api", "task", "result"],
                "aliases": ["结果集列表", "列出结果集"]
            },
            {
                "id": "api-entrance-result",
                "question": "如何获取任务结果集",
                "answer": "GET /api/rest_j/v1/entrance/{execID}/resultset/{index}?size=500\n参数: index 结果集序号, size 返回行数",
                "tags": ["api", "task", "result"],
                "aliases": ["下载结果集", "查看结果集"]
            },
            {
                "id": "api-entrance-kill",
                "question": "如何杀死任务",
                "answer": "POST /api/rest_j/v1/entrance/{execID}/kill",
                "tags": ["api", "task", "kill"],
                "aliases": ["终止任务", "取消任务", "停止任务"]
            },
            {
                "id": "api-metadata-databases",
                "question": "如何查询数据源下的数据库列表",
                "answer": "GET /api/rest_j/v1/metadataQuery/getDatabases?dataSourceName=xxx&system=hive",
                "tags": ["api", "metadata", "database"],
                "aliases": ["获取数据库列表", "列出数据库"]
            },
            {
                "id": "api-metadata-tables",
                "question": "如何查询数据库下的表列表",
                "answer": "GET /api/rest_j/v1/metadataQuery/getTables?dataSourceName=xxx&database=xxx&system=hive",
                "tags": ["api", "metadata", "table"],
                "aliases": ["获取表列表", "列出表"]
            },
            {
                "id": "api-metadata-columns",
                "question": "如何查询表的字段信息",
                "answer": "GET /api/rest_j/v1/metadataQuery/getColumns?dataSourceName=xxx&database=xxx&table=xxx&system=hive",
                "tags": ["api", "metadata", "column"],
                "aliases": ["获取表结构", "查看字段信息"]
            },
            {
                "id": "api-metadata-partitions",
                "question": "如何查询表的分区信息",
                "answer": "GET /api/rest_j/v1/metadataQuery/getPartitions?dataSourceName=xxx&database=xxx&table=xxx&system=hive",
                "tags": ["api", "metadata", "partition"],
                "aliases": ["查看分区", "分区信息查询"]
            },
            {
                "id": "api-metadata-tableinfo",
                "question": "如何获取表的详细信息",
                "answer": "GET /api/rest_j/v1/metadataQuery/getTableInfo?dataSourceName=xxx&database=xxx&table=xxx&system=hive",
                "tags": ["api", "metadata", "table"],
                "aliases": ["表信息", "表详情"]
            },
            {
                "id": "api-datasource-list",
                "question": "如何获取已注册数据源列表",
                "answer": "GET /api/rest_j/v1/datasource/getAll",
                "tags": ["api", "datasource", "list"],
                "aliases": ["列出数据源", "获取数据源列表"]
            },
            {
                "id": "api-datasource-detail",
                "question": "如何根据 ID 获取数据源详情",
                "answer": "GET /api/rest_j/v1/datasource/get?id=123",
                "tags": ["api", "datasource", "detail"],
                "aliases": ["数据源详情", "查询数据源"]
            },
            {
                "id": "api-datasource-search",
                "question": "如何按名称搜索数据源",
                "answer": "GET /api/rest_j/v1/datasource/search?name=xxx",
                "tags": ["api", "datasource", "search"],
                "aliases": ["搜索数据源", "按名称查数据源"]
            },
            {
                "id": "api-datasource-create",
                "question": "如何创建数据源",
                "answer": "POST /api/rest_j/v1/datasource/create\nBody: name, type(如 hive/mysql), connectParams(主机/端口/库/用户名等), labels(可选)",
                "tags": ["api", "datasource", "create"],
                "aliases": ["新建数据源", "注册数据源"]
            },
            {
                "id": "api-datasource-update",
                "question": "如何更新数据源配置",
                "answer": "POST /api/rest_j/v1/datasource/update\nBody: id, 需更新的字段，如 connectParams 或 labels",
                "tags": ["api", "datasource", "update"],
                "aliases": ["修改数据源", "编辑数据源"]
            },
            {
                "id": "api-datasource-delete",
                "question": "如何删除数据源",
                "answer": "POST /api/rest_j/v1/datasource/delete\nBody: id 或 ids 列表",
                "tags": ["api", "datasource", "delete"],
                "aliases": ["移除数据源", "删除数据源"]
            },
            {
                "id": "api-datasource-test",
                "question": "如何测试数据源连通性",
                "answer": "POST /api/rest_j/v1/datasource/testConnect\nBody: type, connectParams(与创建一致)",
                "tags": ["api", "datasource", "test"],
                "aliases": ["测试连接", "连通性测试"]
            },
            {
                "id": "api-datasource-displaysql",
                "question": "如何生成建表 DDL",
                "answer": "POST /api/rest_j/v1/datasource/displaysql\nBody 可包含 table/schema 字段，用于生成建库建表 DDL",
                "tags": ["api", "datasource", "ddl"],
                "aliases": ["生成DDL", "建表语句"]
            },
            {
                "id": "api-jobhistory-list",
                "question": "如何分页查询任务历史",
                "answer": "GET /api/rest_j/v1/jobhistory/list?user=xxx&pageNow=1&pageSize=20",
                "tags": ["api", "task", "history"],
                "aliases": ["任务历史查询", "任务列表"]
            },
            {
                "id": "api-jobhistory-detail",
                "question": "如何获取历史任务详情",
                "answer": "GET /api/rest_j/v1/jobhistory/{jobID}",
                "tags": ["api", "task", "history"],
                "aliases": ["任务详情", "历史任务详情"]
            },
            {
                "id": "cfg-engine-type",
                "question": "labels.engineType 如何配置",
                "answer": "labels.engineType 需与部署的引擎标识一致，如 hive-1.2.1, spark-2.4.7。示例: {\"labels\":{\"engineType\":\"hive-1.2.1\",\"userCreator\":\"IDE_user\"}}",
                "tags": ["config", "engine"],
                "aliases": ["engineType 配置", "引擎类型标签"]
            },
            {
                "id": "cfg-user-creator",
                "question": "labels.userCreator 有什么作用",
                "answer": "用于标识请求来源或创建者类型，例如 IDE_user、scheduler_xxx，便于路由和审计",
                "tags": ["config", "labels"],
                "aliases": ["userCreator 配置", "用户创建者标签"]
            },
            {
                "id": "cfg-timeout",
                "question": "任务超时时间如何设置",
                "answer": "可通过 runtime.max.askExecutorTimes 或 engineConn.timeout 控制任务超时（毫秒），具体取决于你的引擎与网关实现",
                "tags": ["config", "timeout"],
                "aliases": ["设置超时", "执行超时配置"]
            },
            {
                "id": "cfg-queue",
                "question": "如何指定资源队列",
                "answer": "通常通过 labels.queue 或执行参数中设置队列名（如 YARN 队列），也可在引擎侧配置默认队列",
                "tags": ["config", "resource"],
                "aliases": ["设置队列", "资源队列配置"]
            },
            {
                "id": "cfg-script-path",
                "question": "如何记录脚本来源路径",
                "answer": "可在 source.scriptPath 中传入源脚本路径（如 /tmp/demo.sql），便于审计与追踪",
                "tags": ["config", "source"],
                "aliases": ["scriptPath", "脚本路径"]
            },
            {
                "id": "cfg-vars",
                "question": "如何在任务中传递变量",
                "answer": "可通过 params.variable 传递键值对变量，由引擎侧在执行前进行替换/注入",
                "tags": ["config", "params"],
                "aliases": ["任务变量", "参数变量"]
            },
            {
                "id": "err-11001",
                "question": "Linkis 报错 11001 是什么原因",
                "answer": "11001 通常表示认证失败或未登录。请检查登录状态、凭据有效性以及网关鉴权配置",
                "tags": ["error", "auth"],
                "aliases": ["错误码 11001", "未登录 11001"]
            },
            {
                "id": "err-12001",
                "question": "参数错误 12001 如何处理",
                "answer": "检查必填参数是否缺失，类型是否正确（如 envId 应为字符串），并参考接口文档修正",
                "tags": ["error", "params"],
                "aliases": ["错误码 12001", "参数错误 12001"]
            },
            {
                "id": "err-13002",
                "question": "执行引擎不可用 13002 怎么办",
                "answer": "该错误表示目标执行引擎未启动或标签不匹配，请检查引擎服务状态、资源队列与 labels.engineType",
                "tags": ["error", "engine"],
                "aliases": ["错误码 13002", "引擎不可用"]
            },
            {
                "id": "err-14001",
                "question": "权限不足 14001 如何排查",
                "answer": "确认调用用户是否具备操作权限（数据源、数据库、表级权限），检查网关与后端鉴权策略",
                "tags": ["error", "permission"],
                "aliases": ["错误码 14001", "无权限 14001"]
            },
            {
                "id": "err-15001",
                "question": "任务执行超时 15001 如何处理",
                "answer": "适当提升超时阈值（如 engineConn.timeout），优化 SQL/脚本，或检查资源队列拥塞情况",
                "tags": ["error", "timeout"],
                "aliases": ["错误码 15001", "执行超时 15001"]
            },
            {
                "id": "err-16001",
                "question": "资源不足 16001 如何处理",
                "answer": "检查队列/集群资源是否充足，调大资源配额或调整并发度，必要时更换空闲队列",
                "tags": ["error", "resource"],
                "aliases": ["错误码 16001", "资源不足 16001"]
            },
            {
                "id": "howto-no-resultset",
                "question": "提交成功但没有结果集怎么办",
                "answer": "确认语句是否产生结果（如 DDL/DML 通常无结果集），检查 resultset 索引列表与权限限制",
                "tags": ["howto", "result"],
                "aliases": ["无结果集", "没有结果"]
            },
            {
                "id": "howto-paginate-result",
                "question": "如何分页获取大结果集",
                "answer": "通过 size 控制每次拉取行数，并循环拉取；若支持 offset/nextToken，可按游标分页",
                "tags": ["howto", "result"],
                "aliases": ["结果集分页", "分页获取结果"]
            },
            {
                "id": "howto-retry",
                "question": "任务失败如何重试",
                "answer": "可重新提交同一脚本/SQL；若系统支持重试 API，优先使用；同时排查失败原因（日志、资源、权限）",
                "tags": ["howto", "task"],
                "aliases": ["失败重试", "任务重试"]
            },
            {
                "id": "howto-choose-engine",
                "question": "如何选择合适的引擎",
                "answer": "按任务类型与生态匹配引擎（如 SQL 选 hive/spark-sql，批处理选 spark，脚本选 python/shell），并配置对应 engineType",
                "tags": ["howto", "engine"],
                "aliases": ["选择引擎", "引擎如何选"]
            },
        ]

        self._entries = [KnowledgeEntry(**item) for item in raw_data]

    def ask(self, query: str, top_k: int = 1) -> List[Dict]:
        matches = [e for e in self._entries if e.matches(query)]
        results = [{
            "id": e.id,
            "question": e.question,
            "answer": e.answer,
            "tags": e.tags,
            "aliases": e.aliases
        } for e in matches]
        return results[:top_k]

    def search_by_tag(self, tag: str) -> List[Dict]:
        tag_lower = tag.lower()
        matches = [e for e in self._entries if tag_lower in (t.lower() for t in e.tags)]
        return [{
            "id": e.id,
            "question": e.question,
            "answer": e.answer,
            "tags": e.tags,
            "aliases": e.aliases
        } for e in matches]

    def add_entry(self, entry: Dict):
        self._entries.append(KnowledgeEntry(**entry))