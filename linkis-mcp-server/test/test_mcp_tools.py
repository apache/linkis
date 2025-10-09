import pytest
import mcp_server

def test_mcp_tools_exist():
    tools = mcp_server.mcp.list_tools()
    expected_tools = [
        "login", "get_databases", "get_tables", "get_columns", "get_partitions",
        "get_table_props", "get_partition_props", "displaysql",
        "get_table_fields_info", "get_table_statistic_info", "execute", "submit",
        "status", "progress", "progress_with_resource", "pause", "kill", "kill_jobs",
        "runningtask", "taskinfo", "qa_ask", "qa_add", "qa_topics", "qa_reload"
    ]
    for tool in expected_tools:
        assert any(t.name == tool for t in tools)
