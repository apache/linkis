from typing import Dict, Any
from linkis_client import LinkisClient
from config import API_PATHS

class MetadataAPI:
    def __init__(self, client: LinkisClient):
        self.client = client

    def get_columns(self, database: str, table: str) -> Dict[str, Any]:
        params = {"database": database, "table": table}
        return self.client.get(API_PATHS["get_columns"], params)

    def get_databases(self) -> Dict[str, Any]:
        return self.client.get(API_PATHS["get_databases"])

    def get_tables(self, database: str) -> Dict[str, Any]:
        params = {"database": database}
        return self.client.get(API_PATHS["get_tables"], params)

    def get_partitions(self, database: str, table: str) -> Dict[str, Any]:
        params = {"database": database, "table": table}
        return self.client.get(API_PATHS["get_partitions"], params)

    def get_table_props(self, database: str, table: str) -> Dict[str, Any]:
        params = {"database": database, "table": table}
        return self.client.get(API_PATHS["get_table_props"], params)

    def get_partition_props(self, database: str, table: str, partition: str) -> Dict[str, Any]:
        params = {"database": database, "table": table, "partition": partition}
        return self.client.get(API_PATHS["get_partition_props"], params)
