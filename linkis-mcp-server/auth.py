from typing import Dict, Any
from linkis_client import LinkisClient
from config import API_PATHS

class AuthAPI:
    def __init__(self, client: LinkisClient):
        self.client = client

    def login(self, username: str, password: str) -> Dict[str, Any]:
        payload = {"userName": username, "password": password}
        return self.client.post(API_PATHS["login"], payload)
