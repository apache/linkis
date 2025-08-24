import pytest
from config import LINKIS_BASE_URL, LINKIS_TOKEN
from linkis_client import LinkisClient
from auth import AuthAPI

@pytest.fixture(scope="module")
def auth_api():
    client = LinkisClient(base_url=LINKIS_BASE_URL, token=LINKIS_TOKEN)
    return AuthAPI(client)

def test_login_fail(auth_api):
    with pytest.raises(Exception):
        auth_api.login("fake_user", "wrong_password")
