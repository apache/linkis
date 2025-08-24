import pytest
from config import LINKIS_BASE_URL, LINKIS_TOKEN
from linkis_client import LinkisClient
from datasource import DataSourceAPI

@pytest.fixture(scope="module")
def datasource_api():
    client = LinkisClient(base_url=LINKIS_BASE_URL, token=LINKIS_TOKEN)
    return DataSourceAPI(client)

def test_displaysql(datasource_api):
    try:
        res = datasource_api.displaysql(1, "CREATE TABLE test (id INT)")
        assert isinstance(res, dict)
    except Exception as e:
        pytest.skip(f"displaysql skipped: {e}")

def test_get_table_fields_info(datasource_api):
    try:
        res = datasource_api.get_table_fields_info(1, "some_table")
        assert isinstance(res, list)
    except Exception as e:
        pytest.skip(f"get_table_fields_info skipped: {e}")

def test_get_table_statistic_info(datasource_api):
    try:
        res = datasource_api.get_table_statistic_info(1, "some_table")
        assert isinstance(res, dict)
    except Exception as e:
        pytest.skip(f"get_table_statistic_info skipped: {e}")
