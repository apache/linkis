import json
import os

import json
import os

CONFIG_PATH = os.path.join(os.path.dirname(__file__), "config.json")

if not os.path.exists(CONFIG_PATH):
    raise FileNotFoundError(f"未找到配置文件: {CONFIG_PATH}")

with open(CONFIG_PATH, "r", encoding="utf-8") as f:
    all_cfg = json.load(f)

env_mode = all_cfg.get("current_env", "dev")
if env_mode not in all_cfg:
    raise ValueError(f"未知配置环境: {env_mode}，可选值: {list(all_cfg.keys())}")

cfg = all_cfg[env_mode]

LINKIS_BASE_URL = cfg.get("LINKIS_BASE_URL")
LINKIS_TOKEN = cfg.get("LINKIS_TOKEN")
LINKIS_ENV = cfg.get("LINKIS_ENV", env_mode)

if not LINKIS_BASE_URL:
    raise ValueError(f"[{env_mode}] LINKIS_BASE_URL 未配置")
if not LINKIS_TOKEN:
    raise ValueError(f"[{env_mode}] LINKIS_TOKEN 未配置")

# API 路径集中管理
API_PATHS = {
    # Auth
    "login": "/api/rest_j/v1/user/login",

    # Metadata
    "get_columns": "/api/rest_j/v1/metadataQuery/getColumns",
    "get_databases": "/api/rest_j/v1/metadataQuery/getDatabases",
    "get_tables": "/api/rest_j/v1/metadataQuery/getTables",
    "get_partitions": "/api/rest_j/v1/metadataQuery/getPartitions",
    "get_table_props": "/api/rest_j/v1/metadataQuery/getTableProps",
    "get_partition_props": "/api/rest_j/v1/metadataQuery/getPartitionProps",

    # DataSource
    "displaysql": "/api/rest_j/v1/datasource/displaysql",
    "get_table_fields_info": "/api/rest_j/v1/datasource/getTableFieldsInfo",
    "get_table_statistic_info": "/api/rest_j/v1/datasource/getTableStatisticInfo",

    # Tasks
    "execute": "/api/rest_j/v1/entrance/execute",
    "submit": "/api/rest_j/v1/entrance/submit",
    "kill": "/api/rest_j/v1/entrance/{id}/kill",
    "kill_jobs": "/api/rest_j/v1/entrance/{id}/killJobs",
    "pause": "/api/rest_j/v1/entrance/{id}/pause",
    "progress": "/api/rest_j/v1/entrance/{id}/progress",
    "progress_with_resource": "/api/rest_j/v1/entrance/{id}/progressWithResource",
    "status": "/api/rest_j/v1/entrance/{id}/status",
    "runningtask": "/api/rest_j/v1/entrance/api/metrics/runningtask",
    "taskinfo": "/api/rest_j/v1/entrance/api/metrics/taskinfo"
}
