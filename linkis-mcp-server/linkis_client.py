from __future__ import annotations

import json
from typing import Any, Dict, Optional, Union

import requests

from config import LINKIS_BASE_URL, LINKIS_TOKEN, LINKIS_ENV

class LinkisError(Exception):
    def __init__(self, message: str, status: Optional[int] = None, payload: Any = None):
        super().__init__(message)
        self.status = status
        self.payload = payload

    def __str__(self) -> str:
        base = super().__str__()
        if self.status is not None:
            base += f" (HTTP {self.status})"
        if self.payload is not None:
            base += f" | payload={self.payload}"
        return base


def _join_url(base_url: str, path: str) -> str:
    return f"{base_url.rstrip('/')}/{path.lstrip('/')}"


def _clean_params(d: Optional[Dict[str, Any]]) -> Dict[str, Any]:
    return {k: v for k, v in (d or {}).items() if v is not None}


def _interpolate_path(path_template: str, path_params: Optional[Dict[str, Any]]) -> str:
    if not path_params:
        return path_template
    out = path_template
    for k, v in path_params.items():
        out = out.replace(f"{{{k}}}", requests.utils.quote(str(v), safe=""))
    return out


def _safe_resp_payload(resp: requests.Response) -> Any:
    try:
        return resp.json()
    except Exception:
        return resp.text


class LinkisClient:

    def __init__(
        self,
        base_url: Optional[str] = None,
        token: Optional[str] = None,
        username: Optional[str] = None,
        password: Optional[str] = None,
        timeout: Optional[Union[int, float]] = None,
        verify_ssl: Optional[bool] = True,
        extra_headers: Optional[Dict[str, str]] = None,
    ):
        self.base_url = (base_url or LINKIS_BASE_URL).strip()
        if not self.base_url:
            raise ValueError("LINKIS_BASE_URL 未配置")

        self.token = (token or LINKIS_TOKEN).strip() if (token or LINKIS_TOKEN) else None
        self.username = username
        self.password = password
        self.timeout = float(timeout if timeout is not None else 30)
        self.verify_ssl = verify_ssl

        self._session = requests.Session()
        self._session.headers.update({"Accept": "*/*"})
        if self.token:
            self._session.headers.update({"Authorization": f"Bearer {self.token}"})
        if extra_headers:
            self._session.headers.update(extra_headers)

        if self.username and self.password:
            try:
                self.login(self.username, self.password)
            except Exception as e:
                print(f"[LinkisClient] Auto login failed: {e}")

    def login(self, user_name: str, password: str) -> Dict[str, Any]:
        path = "/api/rest_j/v1/user/login"
        url = _join_url(self.base_url, path)
        payload = {"userName": user_name, "password": password}

        resp = self._session.post(url, json=payload, timeout=self.timeout, verify=self.verify_ssl)
        if not resp.ok:
            raise LinkisError("Login failed", status=resp.status_code, payload=_safe_resp_payload(resp))

        data = _safe_resp_payload(resp)
        return data

    def get(
        self,
        path: str,
        params: Optional[Dict[str, Any]] = None,
        path_params: Optional[Dict[str, Any]] = None,
    ) -> Any:
        url = _join_url(self.base_url, _interpolate_path(path, path_params))
        resp = self._session.get(url, params=_clean_params(params), timeout=self.timeout, verify=self.verify_ssl)
        return self._handle_response(resp)

    def post(
        self,
        path: str,
        json_body: Optional[Dict[str, Any]] = None,
        path_params: Optional[Dict[str, Any]] = None,
    ) -> Any:
        url = _join_url(self.base_url, _interpolate_path(path, path_params))
        headers = {"Content-Type": "application/json"}
        resp = self._session.post(
            url,
            data=None if json_body is None else json.dumps(json_body),
            headers=headers,
            timeout=self.timeout,
            verify=self.verify_ssl,
        )
        return self._handle_response(resp)

    def _handle_response(self, resp: requests.Response) -> Any:
        if not resp.ok:
            raise LinkisError("Request failed", status=resp.status_code, payload=_safe_resp_payload(resp))

        try:
            return resp.json()
        except ValueError:
            return resp.text
