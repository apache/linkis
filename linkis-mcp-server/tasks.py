from typing import Dict, Any
from linkis_client import LinkisClient
from config import API_PATHS

class TaskAPI:
    def __init__(self, client: LinkisClient):
        self.client = client

    def execute(self, code: str, execute_user: str, engine_type: str = "spark") -> Dict[str, Any]:
        payload = {
            "executeUser": execute_user,
            "executionCode": code,
            "engineType": engine_type
        }
        return self.client.post(API_PATHS["execute"], payload)

    def submit(self, code: str, execute_user: str, engine_type: str = "spark") -> Dict[str, Any]:
        payload = {
            "executeUser": execute_user,
            "executionCode": code,
            "engineType": engine_type
        }
        return self.client.post(API_PATHS["submit"], payload)

    def kill(self, exec_id: str) -> Dict[str, Any]:
        path = API_PATHS["kill"].format(id=exec_id)
        return self.client.get(path)

    def kill_jobs(self, exec_id: str) -> Dict[str, Any]:
        path = API_PATHS["kill_jobs"].format(id=exec_id)
        return self.client.get(path)

    def pause(self, exec_id: str) -> Dict[str, Any]:
        path = API_PATHS["pause"].format(id=exec_id)
        return self.client.get(path)

    def progress(self, exec_id: str) -> Dict[str, Any]:
        path = API_PATHS["progress"].format(id=exec_id)
        return self.client.get(path)

    def progress_with_resource(self, exec_id: str) -> Dict[str, Any]:
        path = API_PATHS["progress_with_resource"].format(id=exec_id)
        return self.client.get(path)

    def status(self, exec_id: str) -> Dict[str, Any]:
        path = API_PATHS["status"].format(id=exec_id)
        return self.client.get(path)

    def runningtask(self) -> Dict[str, Any]:
        return self.client.get(API_PATHS["runningtask"])

    def taskinfo(self) -> Dict[str, Any]:
        return self.client.get(API_PATHS["taskinfo"])
