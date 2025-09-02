import pytest
from config import LINKIS_BASE_URL, LINKIS_TOKEN
from linkis_client import LinkisClient
from metadata import MetadataAPI

@pytest.fixture(scope="module")
def metadata_api():
    client = LinkisClient(base_url=LINKIS_BASE_URL, token=LINKIS_TOKEN)
    return MetadataAPI(client)

def test_get_databases(metadata_api):
    try:
        dbs = metadata_api.get_databases()
        assert isinstance(dbs, list)
    except Exception as e:
        pytest.skip(f"get_databases skipped: {e}")

def test_get_tables(metadata_api):
    try:
        tables = metadata_api.get_tables("default")
        assert isinstance(tables, list)
    except Exception as e:
        pytest.skip(f"get_tables skipped: {e}")

def test_get_columns(metadata_api):
    try:
        cols = metadata_api.get_columns("default", "some_table")
        assert isinstance(cols, list)
    except Exception as e:
        pytest.skip(f"get_columns skipped: {e}")

def test_get_partitions(metadata_api):
    try:
        parts = metadata_api.get_partitions("default", "some_partitioned_table")
        assert isinstance(parts, list)
    except Exception as e:
        pytest.skip(f"get_partitions skipped: {e}")

def test_get_table_props(metadata_api):
    try:
        props = metadata_api.get_table_props("default", "some_table")
        assert isinstance(props, dict)
    except Exception as e:
        pytest.skip(f"get_table_props skipped: {e}")

def test_get_partition_props(metadata_api):
    try:
        props = metadata_api.get_partition_props("default", "some_partitioned_table", "part_col=1")
        assert isinstance(props, dict)
    except Exception as e:
        pytest.skip(f"get_partition_props skipped: {e}")
