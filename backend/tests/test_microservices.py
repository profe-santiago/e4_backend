"""
5 tests E2E para la app de microservicios de tickets.
Requiere que los contenedores estén corriendo: docker compose up -d
Ejecutar con: python -m pytest tests/test_microservices.py -v
"""

import os
import time
import requests
import pytest

GW = "http://localhost:8080"
PASSWORD = "Password123!"
TICKET_PRICE_CENTS = 5000  # $50.00 USD — debe coincidir con el precio en published_event


# ─────────────────────────────────────────────────────────────────────────────
# Fixtures de sesión (se ejecutan una sola vez por sesión de pytest)
# ─────────────────────────────────────────────────────────────────────────────

@pytest.fixture(scope="session")
def organizer_token(ts):
    email = f"organizer_{ts}@test.com"
    r = requests.post(f"{GW}/api/v1/auth/register/admin", json={"email": email, "password": PASSWORD}, timeout=30)
    assert r.status_code == 201, f"Registro organizer admin falló: {r.text}"
    return r.json()["token"]


@pytest.fixture(scope="session")
def buyer_token(ts):
    email = f"buyer_{ts}@test.com"
    r = requests.post(f"{GW}/api/v1/auth/register", json={"email": email, "password": PASSWORD}, timeout=30)
    assert r.status_code == 201, f"Registro buyer falló: {r.text}"
    return r.json()["token"]


@pytest.fixture(scope="session")
def ts():
    return int(time.time())


@pytest.fixture(scope="session")
def payment_intent_id():
    """Crea y confirma un PaymentIntent de Stripe en modo test para la saga de pago."""
    import stripe as stripe_lib
    stripe_key = os.environ.get("STRIPE_SECRET_KEY", "")
    if not stripe_key:
        pytest.skip("STRIPE_SECRET_KEY no disponible")
    stripe_lib.api_key = stripe_key
    intent = stripe_lib.PaymentIntent.create(
        amount=TICKET_PRICE_CENTS,
        currency="usd",
        payment_method="pm_card_visa",
        payment_method_types=["card"],
        confirm=True,
    )
    return intent.id


@pytest.fixture(scope="session")
def profile_creds(ts):
    """Usuario dedicado para tests de perfil (distinto de buyer/organizer)."""
    email = f"profile_{ts}@test.com"
    r = requests.post(f"{GW}/api/v1/auth/register",
                      json={"email": email, "password": PASSWORD}, timeout=30)
    assert r.status_code == 201, f"Registro profile user falló: {r.text}"
    return {"token": r.json()["token"], "email": email}


@pytest.fixture(scope="session")
def published_event(organizer_token, ts):
    """Crea y publica un evento. Compartido entre tests."""
    start_date = "2027-06-15T20:00:00"
    payload = {
        "title": f"Festival Test {ts}",
        "description": "Evento de testing automatizado",
        "venue": "Luna Park",
        "city": "Buenos Aires",
        "country": "Argentina",
        "startDate": start_date,
    }
    r = requests.post(
        f"{GW}/api/v1/events",
        json=payload,
        headers={"Authorization": f"Bearer {organizer_token}"},
        timeout=30,
    )
    assert r.status_code == 201, f"Crear evento falló: {r.text}"
    event_id = r.json()["id"]

    # Tipo de ticket
    r = requests.post(
        f"{GW}/api/v1/events/{event_id}/ticket-types",
        json={"name": "General", "description": "Entrada general", "price": 50.0, "currency": "USD", "totalQuantity": 100},
        headers={"Authorization": f"Bearer {organizer_token}"},
        timeout=30,
    )
    assert r.status_code == 201, f"Crear ticket-type falló: {r.text}"
    ticket_type_id = r.json()["id"]

    # Publicar
    r = requests.patch(
        f"{GW}/api/v1/events/{event_id}/status?status=PUBLISHED",
        headers={"Authorization": f"Bearer {organizer_token}"},
        timeout=30,
    )
    assert r.status_code == 200, f"Publicar evento falló: {r.text}"

    return {"event_id": event_id, "ticket_type_id": ticket_type_id}


# ─────────────────────────────────────────────────────────────────────────────
# TEST 1 — Health checks de todos los servicios vía gateway
# ─────────────────────────────────────────────────────────────────────────────

def test_1_health_checks():
    """Verifica que el gateway y los servicios críticos respondan OK."""
    r = requests.get(f"{GW}/actuator/health", timeout=30)
    assert r.status_code == 200, f"Gateway no responde: {r.text}"

    data = r.json()
    status = data.get("status")
    assert status == "UP", f"Gateway no está UP: {data}"


# ─────────────────────────────────────────────────────────────────────────────
# TEST 2 — Flujo de autenticación: registro + login + JWT
# ─────────────────────────────────────────────────────────────────────────────

def test_2_auth_flow():
    """Registro de usuario → Login → JWT válido devuelto."""
    ts_local = int(time.time() * 1000)
    email = f"authtest_{ts_local}@test.com"

    # Registro
    r = requests.post(f"{GW}/api/v1/auth/register", json={"email": email, "password": PASSWORD}, timeout=30)
    assert r.status_code == 201, f"Registro falló ({r.status_code}): {r.text}"
    token = r.json().get("token")
    assert token and len(token) > 20, "No se devolvió JWT en el registro"

    # Login
    r = requests.post(f"{GW}/api/v1/auth/login", json={"email": email, "password": PASSWORD}, timeout=30)
    assert r.status_code == 200, f"Login falló ({r.status_code}): {r.text}"
    login_token = r.json().get("token")
    assert login_token and len(login_token) > 20, "No se devolvió JWT en el login"

    # Login con credenciales incorrectas debe rechazarse
    r = requests.post(f"{GW}/api/v1/auth/login", json={"email": email, "password": "WrongPass!"}, timeout=30)
    assert r.status_code in (401, 403, 400), f"Login inválido debería devolver 4xx, devolvió {r.status_code}"


# ─────────────────────────────────────────────────────────────────────────────
# TEST 3 — Ciclo de vida de un evento: crear → publicar → listar
# ─────────────────────────────────────────────────────────────────────────────

def test_3_event_lifecycle(published_event):
    """Evento creado y publicado aparece en el listado público."""
    event_id = published_event["event_id"]

    r = requests.get(f"{GW}/api/v1/events", timeout=30)
    assert r.status_code == 200, f"Listar eventos falló: {r.text}"

    data = r.json()
    ids = [e["id"] for e in data.get("content", [])]
    assert event_id in ids, f"Evento {event_id} no aparece en el listado público. IDs encontrados: {ids}"


# ─────────────────────────────────────────────────────────────────────────────
# TEST 4 — Compra de tickets (saga): orden → CONFIRMED → tickets generados
# ─────────────────────────────────────────────────────────────────────────────

def test_4_order_and_tickets_saga(buyer_token, published_event, payment_intent_id):
    """Crea una orden y espera que la saga asíncrona la confirme y genere tickets."""
    event_id = published_event["event_id"]
    ticket_type_id = published_event["ticket_type_id"]

    payload = {
        "items": [{"eventId": event_id, "ticketTypeId": ticket_type_id, "quantity": 1}],
        "paymentIntentId": payment_intent_id,
    }
    r = requests.post(
        f"{GW}/api/v1/orders",
        json=payload,
        headers={"Authorization": f"Bearer {buyer_token}"},
        timeout=60,
    )
    assert r.status_code == 201, f"Crear orden falló ({r.status_code}): {r.text}"
    order_id = r.json()["id"]

    # Esperar hasta 45s que la saga complete
    final_status = None
    for _ in range(15):
        time.sleep(3)
        r = requests.get(
            f"{GW}/api/v1/orders/{order_id}",
            headers={"Authorization": f"Bearer {buyer_token}"},
            timeout=30,
        )
        final_status = r.json().get("status")
        if final_status in ("CONFIRMED", "FAILED", "CANCELLED"):
            break

    assert final_status == "CONFIRMED", f"Orden no confirmada. Estado final: {final_status}"

    # Verificar que se generaron tickets
    r = requests.get(
        f"{GW}/api/v1/tickets/my?page=0&size=10",
        headers={"Authorization": f"Bearer {buyer_token}"},
        timeout=30,
    )
    assert r.status_code == 200, f"Obtener tickets falló: {r.text}"
    total = r.json().get("totalElements", 0)
    assert total >= 1, f"No se generaron tickets después de la orden confirmada (total={total})"


# ─────────────────────────────────────────────────────────────────────────────
# TEST 5 — Seguridad: endpoints protegidos rechazan acceso no autorizado
# ─────────────────────────────────────────────────────────────────────────────

def test_5_security_unauthorized():
    """Los endpoints protegidos devuelven 401 sin token y rechazan tokens inválidos."""
    protected_endpoints = [
        ("GET", f"{GW}/api/v1/orders/my"),
        ("GET", f"{GW}/api/v1/tickets/my"),
        ("POST", f"{GW}/api/v1/users"),
    ]

    for method, url in protected_endpoints:
        # Sin token → 401 o 403
        r = requests.request(method, url, timeout=30)
        assert r.status_code in (401, 403), (
            f"{method} {url} sin token devolvió {r.status_code}, se esperaba 401/403"
        )

        # Token inválido → 401 o 403
        r = requests.request(method, url, headers={"Authorization": "Bearer token.falso.aqui"}, timeout=30)
        assert r.status_code in (401, 403), (
            f"{method} {url} con token inválido devolvió {r.status_code}, se esperaba 401/403"
        )


# ─────────────────────────────────────────────────────────────────────────────
# TEST 6 — CRUD de perfil de usuario: crear y leer
# ─────────────────────────────────────────────────────────────────────────────

def test_6_user_profile_create_and_read(profile_creds):
    """Crea un perfil de usuario y lo recupera con GET /me."""
    token = profile_creds["token"]
    email = profile_creds["email"]

    # Crear perfil
    r = requests.post(
        f"{GW}/api/v1/users",
        json={"firstName": "Test", "lastName": "Usuario", "email": email},
        headers={"Authorization": f"Bearer {token}"},
        timeout=30,
    )
    assert r.status_code == 201, f"Crear perfil falló ({r.status_code}): {r.text}"
    data = r.json()
    assert data["firstName"] == "Test"
    assert data["email"] == email

    # Leer perfil propio
    r = requests.get(
        f"{GW}/api/v1/users/me",
        headers={"Authorization": f"Bearer {token}"},
        timeout=30,
    )
    assert r.status_code == 200, f"GET /users/me falló: {r.text}"
    assert r.json()["email"] == email


# ─────────────────────────────────────────────────────────────────────────────
# TEST 7 — CRUD de perfil de usuario: actualizar
# ─────────────────────────────────────────────────────────────────────────────

def test_7_user_profile_update(profile_creds):
    """Actualiza el perfil propio y verifica los nuevos valores."""
    token = profile_creds["token"]

    r = requests.put(
        f"{GW}/api/v1/users/me",
        json={"firstName": "Actualizado", "lastName": "Apellido", "phone": "1122334455"},
        headers={"Authorization": f"Bearer {token}"},
        timeout=30,
    )
    assert r.status_code == 200, f"PUT /users/me falló ({r.status_code}): {r.text}"
    data = r.json()
    assert data["firstName"] == "Actualizado", f"firstName no se actualizó: {data}"
    assert data["lastName"] == "Apellido", f"lastName no se actualizó: {data}"
