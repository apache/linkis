from mcp.server.fastmcp import FastMCP

from config import LINKIS_TOKEN, LINKIS_ENV
from linkis_client import LinkisClient
from auth import AuthAPI
from metadata import MetadataAPI
from datasource import DataSourceAPI
from tasks import TaskAPI
from knowledge import KnowledgeQA

mcp = FastMCP("Linkis MCP Server")

# 初始化 Linkis 客户端与各模块
client = LinkisClient(LINKIS_TOKEN)

auth_api = AuthAPI(client)
metadata_api = MetadataAPI(client)
datasource_api = DataSourceAPI(client)
task_api = TaskAPI(client)
qa = KnowledgeQA()


def _ok(data):
    return {"ok": True, "data": data}


def _err(e: Exception):
    return {"ok": False, "error": str(e)}


# ========== Auth ==========
@mcp.tool()
def login(username: str, password: str):
    """用户登录，获取/验证凭据"""
    try:
        return _ok(auth_api.login(username, password))
    except Exception as e:
        return _err(e)


# ========== Metadata ==========
@mcp.tool()
def get_databases():
    try:
        return _ok(metadata_api.get_databases())
    except Exception as e:
        return _err(e)

@mcp.tool()
def get_tables(database: str):
    try:
        return _ok(metadata_api.get_tables(database))
    except Exception as e:
        return _err(e)

@mcp.tool()
def get_columns(database: str, table: str):
    try:
        return _ok(metadata_api.get_columns(database, table))
    except Exception as e:
        return _err(e)

@mcp.tool()
def get_partitions(database: str, table: str):
    try:
        return _ok(metadata_api.get_partitions(database, table))
    except Exception as e:
        return _err(e)

@mcp.tool()
def get_table_props(database: str, table: str):
    try:
        return _ok(metadata_api.get_table_props(database, table))
    except Exception as e:
        return _err(e)

@mcp.tool()
def get_partition_props(database: str, table: str, partition: str):
    try:
        return _ok(metadata_api.get_partition_props(database, table, partition))
    except Exception as e:
        return _err(e)


# ========== DataSource ==========
@mcp.tool()
def displaysql(datasource_id: int, sql: str):
    try:
        return _ok(datasource_api.displaysql(datasource_id, sql))
    except Exception as e:
        return _err(e)

@mcp.tool()
def get_table_fields_info(datasource_id: int, table: str):
    try:
        return _ok(datasource_api.get_table_fields_info(datasource_id, table))
    except Exception as e:
        return _err(e)

@mcp.tool()
def get_table_statistic_info(datasource_id: int, table: str):
    try:
        return _ok(datasource_api.get_table_statistic_info(datasource_id, table))
    except Exception as e:
        return _err(e)


# ========== Tasks ==========
@mcp.tool()
def execute(code: str, execute_user: str, engine_type: str = "spark"):
    try:
        return _ok(task_api.execute(code, execute_user, engine_type))
    except Exception as e:
        return _err(e)

@mcp.tool()
def submit(code: str, execute_user: str, engine_type: str = "spark"):
    try:
        return _ok(task_api.submit(code, execute_user, engine_type))
    except Exception as e:
        return _err(e)

@mcp.tool()
def status(exec_id: str):
    try:
        return _ok(task_api.status(exec_id))
    except Exception as e:
        return _err(e)

@mcp.tool()
def progress(exec_id: str):
    try:
        return _ok(task_api.progress(exec_id))
    except Exception as e:
        return _err(e)

@mcp.tool()
def progress_with_resource(exec_id: str):
    try:
        return _ok(task_api.progress_with_resource(exec_id))
    except Exception as e:
        return _err(e)

@mcp.tool()
def pause(exec_id: str):
    try:
        return _ok(task_api.pause(exec_id))
    except Exception as e:
        return _err(e)

@mcp.tool()
def kill(exec_id: str):
    try:
        return _ok(task_api.kill(exec_id))
    except Exception as e:
        return _err(e)

@mcp.tool()
def kill_jobs(exec_id: str):
    try:
        return _ok(task_api.kill_jobs(exec_id))
    except Exception as e:
        return _err(e)

@mcp.tool()
def runningtask():
    try:
        return _ok(task_api.runningtask())
    except Exception as e:
        return _err(e)

@mcp.tool()
def taskinfo():
    try:
        return _ok(task_api.taskinfo())
    except Exception as e:
        return _err(e)


# ========== Knowledge QA ==========
@mcp.tool()
def qa_ask(question: str, top_k: int = 3):
    try:
        return _ok(qa.ask(question, top_k=top_k))
    except Exception as e:
        return _err(e)

@mcp.tool()
def qa_add(question: str, answer: str, tags: str = "", aliases: str = ""):
    try:
        entry = {
            "question": question,
            "answer": answer,
            "tags": [t.strip() for t in tags.split(",") if t.strip()],
            "aliases": [a.strip() for a in aliases.split(",") if a.strip()],
        }
        created = qa.add_entry(entry, save=True)
        return _ok({"entry": created.__dict__})
    except Exception as e:
        return _err(e)

@mcp.tool()
def qa_topics():
    try:
        return _ok({"topics": qa.topics()})
    except Exception as e:
        return _err(e)

@mcp.tool()
def qa_reload():
    try:
        qa.reload()
        return _ok({"reloaded": True})
    except Exception as e:
        return _err(e)


if __name__ == "__main__":
    print(f"[MCP] Starting Linkis MCP Server | ENV={LINKIS_ENV}")
    mcp.run()
