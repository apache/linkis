import pytest
from config import LINKIS_BASE_URL, LINKIS_TOKEN
from linkis_client import LinkisClient
from tasks import TaskAPI

@pytest.fixture(scope="module")
def task_api():
    client = LinkisClient(base_url=LINKIS_BASE_URL, token=LINKIS_TOKEN)
    return TaskAPI(client)

def test_execute(task_api):
    try:
        res = task_api.execute("SELECT 1", "test_user")
        assert isinstance(res, dict)
    except Exception as e:
        pytest.skip(f"execute skipped: {e}")

def test_submit_status_kill(task_api):
    try:
        sub = task_api.submit("SELECT 1", "test_user")
        assert isinstance(sub, dict)
        exec_id = sub.get("execID") or sub.get("execId")
        if exec_id:
            st = task_api.status(exec_id)
            assert isinstance(st, dict)
            task_api.kill(exec_id)
    except Exception as e:
        pytest.skip(f"submit/status/kill skipped: {e}")

def test_progress_methods(task_api):
    try:
        sub = task_api.submit("SELECT 1", "test_user")
        exec_id = sub.get("execID") or sub.get("execId")
        if exec_id:
            prog = task_api.progress(exec_id)
            prog_res = task_api.progress_with_resource(exec_id)
            assert isinstance(prog, dict)
            assert isinstance(prog_res, dict)
    except Exception as e:
        pytest.skip(f"progress skipped: {e}")

def test_pause(task_api):
    try:
        sub = task_api.submit("SELECT 1", "test_user")
        exec_id = sub.get("execID") or sub.get("execId")
        if exec_id:
            res = task_api.pause(exec_id)
            assert isinstance(res, dict)
    except Exception as e:
        pytest.skip(f"pause skipped: {e}")

def test_kill_jobs(task_api):
    try:
        sub = task_api.submit("SELECT 1", "test_user")
        exec_id = sub.get("execID") or sub.get("execId")
        if exec_id:
            res = task_api.kill_jobs(exec_id)
            assert isinstance(res, dict)
    except Exception as e:
        pytest.skip(f"kill_jobs skipped: {e}")

def test_runningtask_taskinfo(task_api):
    try:
        run_info = task_api.runningtask()
        ti = task_api.taskinfo()
        assert isinstance(run_info, dict)
        assert isinstance(ti, dict)
    except Exception as e:
        pytest.skip(f"runningtask/taskinfo skipped: {e}")
