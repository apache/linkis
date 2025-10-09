import pytest
from config import LINKIS_BASE_URL, LINKIS_TOKEN
from linkis_client import LinkisClient, LinkisError


@pytest.fixture(scope="module")
def client():
    return LinkisClient(base_url=LINKIS_BASE_URL, token=LINKIS_TOKEN)


def test_client_init(client):
    assert client.base_url == LINKIS_BASE_URL
    assert client.token == LINKIS_TOKEN
    assert hasattr(client, "_session")


def test_client_get_fail(client):
    with pytest.raises(LinkisError):
        client.get("/api/not_exist_path")


def test_client_post_fail(client):
    with pytest.raises(LinkisError):
        client.post("/api/not_exist_path", json_body={})
