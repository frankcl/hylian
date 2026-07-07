"""会话管理器，对齐 Java ``SessionManager`` + ``SessionListener`` + 容器空闲回收 + ``Sweeper``。

统一销毁出口 ``invalidate``：删存储 + 对 token 会话通知服务端 ``removeActivity``；
后台 daemon 线程周期回收空闲超时会话。
"""

from __future__ import annotations

import logging
import threading
import time

from ..hylian_client import HylianClient
from .store import SessionData, SessionStore

logger = logging.getLogger(__name__)


class SessionManager:
    def __init__(
        self,
        store: SessionStore,
        client: HylianClient,
        *,
        idle_seconds: int = 1800,
        reap_interval_seconds: int = 60,
    ) -> None:
        self.store = store
        self.client = client
        self.idle_seconds = idle_seconds
        self.reap_interval_seconds = reap_interval_seconds
        self._reaper: threading.Thread | None = None
        self._stop = threading.Event()

    # ---- 会话生命周期 ----

    def get_or_create(self, sid: str | None) -> tuple[SessionData, bool]:
        """按 sid 取会话；取不到则新建。返回 ``(会话, 是否新建)``。"""
        if sid:
            data = self.store.get(sid)
            if data is not None:
                data.last_access = int(time.time() * 1000)
                return data, False
        return self.store.create(), True

    def touch(self, data: SessionData) -> None:
        data.last_access = int(time.time() * 1000)
        self.store.save(data)

    def mark_token_session(self, data: SessionData) -> None:
        """标记会话已登录/换过 token（对齐 ``SessionManager.putTokenSession``）。"""
        data.is_token_session = True
        self.store.save(data)

    def invalidate(self, sid: str | None) -> None:
        """销毁会话的统一出口：超时/``/api/logout``/``/api/sweep`` 都走这里。

        对齐 Java ``SessionManager.invalidate`` + ``SessionListener.sessionDestroyed``。
        """
        if not sid:
            return
        data = self.store.get(sid)
        if data is None:
            logger.warning("Session %s not found", sid)
            return
        self.store.remove(sid)
        self._remove_activity(data)
        logger.debug("Session %s invalidated", sid)

    def _remove_activity(self, data: SessionData) -> None:
        """会话销毁时注销服务端应用登录活动（仅 token 会话，对齐 ``SessionListener.removeActivity``）。"""
        if not data.is_token_session:
            return
        try:
            if self.client.remove_activity(data.sid):
                logger.info("Unregister activity success for session %s", data.sid)
            else:
                logger.warning("Unregister activity failed for session %s", data.sid)
        except Exception:  # noqa: BLE001 - 通知失败不应影响本地销毁
            logger.warning("Unregister activity error for session %s", data.sid, exc_info=True)

    # ---- 空闲回收（对齐 setMaxInactiveInterval + Sweeper）----

    def start_reaper(self) -> None:
        if self._reaper is not None:
            return
        self._stop.clear()
        self._reaper = threading.Thread(
            target=self._reap_loop, name="hylian-session-reaper", daemon=True
        )
        self._reaper.start()

    def stop_reaper(self) -> None:
        self._stop.set()
        if self._reaper is not None:
            self._reaper.join(timeout=self.reap_interval_seconds + 1)
            self._reaper = None

    def _reap_loop(self) -> None:
        while not self._stop.wait(self.reap_interval_seconds):
            try:
                self._reap_once()
            except Exception:  # noqa: BLE001
                logger.warning("Session reap error", exc_info=True)

    def _reap_once(self) -> None:
        deadline_ms = int(time.time() * 1000) - self.idle_seconds * 1000
        for sid in self.store.all_ids():
            data = self.store.get(sid)
            if data is not None and data.last_access < deadline_ms:
                logger.debug("Session %s idle-expired", sid)
                self.invalidate(sid)
