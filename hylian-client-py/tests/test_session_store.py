from hylian_client.sdk.session.store import InMemorySessionStore, SessionData


def test_create_unique_sid():
    store = InMemorySessionStore()
    a = store.create()
    b = store.create()
    assert a.sid and b.sid and a.sid != b.sid
    assert store.get(a.sid) is a
    assert store.get(b.sid) is b


def test_get_missing_returns_none():
    store = InMemorySessionStore()
    assert store.get("nope") is None


def test_save_and_remove():
    store = InMemorySessionStore()
    data = store.create()
    data.token = "tok"
    store.save(data)
    assert store.get(data.sid).token == "tok"
    store.remove(data.sid)
    assert store.get(data.sid) is None


def test_all_ids_snapshot():
    store = InMemorySessionStore()
    ids = {store.create().sid for _ in range(3)}
    assert set(store.all_ids()) == ids


def test_set_token_stamps_refresh_time():
    data = SessionData(sid="s")
    assert data.token_refresh_time is None
    data.set_token("t1")
    assert data.token == "t1"
    assert data.token_refresh_time is not None


def test_clear_resources_keeps_lock():
    data = SessionData(sid="s")
    lock = data.lock
    data.set_token("t")
    data.user = object()
    data.roles = []
    data.permissions = []
    data.refresh_user = True
    data.clear_resources()
    assert data.token is None
    assert data.token_refresh_time is None
    assert data.user is None
    assert data.roles is None
    assert data.permissions is None
    assert data.refresh_user is False
    assert data.lock is lock  # lock 不被清除（对齐 Java removeResources 保留 __LOCK__）
