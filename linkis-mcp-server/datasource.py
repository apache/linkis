from typing import Dict, Any
from linkis_client import LinkisClient
from config import API_PATHS

class DataSourceAPI:
    def __init__(self, client: LinkisClient):
        self.client = client

    def displaysql(self, datasource_id: int, sql: str) -> Dict[str, Any]:
        payload = {"id": datasource_id, "sql": sql}
        return self.client.post(API_PATHS["displaysql"], payload)

    def get_table_fields_info(self, datasource_id: int, table: str) -> Dict[str, Any]:
        params = {"id": datasource_id, "table": table}
        return self.client.get(API_PATHS["get_table_fields_info"], params)

    def get_table_statistic_info(self, datasource_id: int, table: str) -> Dict[str, Any]:
        params = {"id": datasource_id, "table": table}
        return self.client.get(API_PATHS["get_table_statistic_info"], params)
