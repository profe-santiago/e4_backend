#!/bin/bash
# ============================================================
# Prueba de flujo completo: registro → compra → reembolso
# ============================================================

GW="http://localhost:8080"
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

PASS=0
FAIL=0

ok()   { echo -e "${GREEN}  ✓ $1${NC}"; ((PASS++)); }
fail() { echo -e "${RED}  ✗ $1${NC}"; ((FAIL++)); }
info() { echo -e "${CYAN}  → $1${NC}"; }
step() { echo -e "\n${YELLOW}[$1]${NC} $2"; }

assert_code() {
  local label=$1 expected=$2 actual=$3 body=$4
  if [ "$actual" -eq "$expected" ]; then
    ok "$label (HTTP $actual)"
  else
    fail "$label — esperado HTTP $expected, recibido HTTP $actual"
    echo "     Body: $body"
  fi
}

http() {
  # Devuelve "BODY\nHTTP_CODE"
  local method=$1 url=$2; shift 2
  curl -s -w "\n%{http_code}" -X "$method" "$url" \
    -H "Content-Type: application/json" "$@"
}

body_of()      { echo "$1" | head -n -1; }
code_of()      { echo "$1" | tail -n1; }
extract()      { echo "$1" | head -n -1 | jq -r "$2" 2>/dev/null; }

# ──────────────────────────────────────────────────────────────
# 0. HEALTH CHECK
# ──────────────────────────────────────────────────────────────
step "0" "Health check del gateway"
R=$(http GET "$GW/actuator/health")
CODE=$(code_of "$R")
assert_code "Gateway health" 200 "$CODE" "$(body_of "$R")"
if [ "$CODE" -ne 200 ]; then
  echo -e "${RED}El gateway no responde. ¿Están los contenedores corriendo?${NC}"
  exit 1
fi

# ──────────────────────────────────────────────────────────────
# 1. REGISTRO DE USUARIOS
# ──────────────────────────────────────────────────────────────
step "1" "Registro de usuarios"

TS=$(date +%s)
ORGANIZER_EMAIL="organizer_${TS}@test.com"
BUYER_EMAIL="buyer_${TS}@test.com"
PASSWORD="Password123"

# Organizador
R=$(http POST "$GW/api/v1/auth/register" \
  -d "{\"email\":\"$ORGANIZER_EMAIL\",\"password\":\"$PASSWORD\"}")
CODE=$(code_of "$R"); BODY=$(body_of "$R")
assert_code "Registro organizador" 201 "$CODE" "$BODY"
ORGANIZER_TOKEN=$(extract "$R" ".token")
info "Organizador: $ORGANIZER_EMAIL | Token: ${ORGANIZER_TOKEN:0:30}..."

# Comprador
R=$(http POST "$GW/api/v1/auth/register" \
  -d "{\"email\":\"$BUYER_EMAIL\",\"password\":\"$PASSWORD\"}")
CODE=$(code_of "$R"); BODY=$(body_of "$R")
assert_code "Registro comprador" 201 "$CODE" "$BODY"
BUYER_TOKEN=$(extract "$R" ".token")
info "Comprador: $BUYER_EMAIL | Token: ${BUYER_TOKEN:0:30}..."

# ──────────────────────────────────────────────────────────────
# 2. LOGIN
# ──────────────────────────────────────────────────────────────
step "2" "Login"

R=$(http POST "$GW/api/v1/auth/login" \
  -d "{\"email\":\"$ORGANIZER_EMAIL\",\"password\":\"$PASSWORD\"}")
CODE=$(code_of "$R"); BODY=$(body_of "$R")
assert_code "Login organizador" 200 "$CODE" "$BODY"
ORGANIZER_TOKEN=$(extract "$R" ".token")

R=$(http POST "$GW/api/v1/auth/login" \
  -d "{\"email\":\"$BUYER_EMAIL\",\"password\":\"$PASSWORD\"}")
CODE=$(code_of "$R"); BODY=$(body_of "$R")
assert_code "Login comprador" 200 "$CODE" "$BODY"
BUYER_TOKEN=$(extract "$R" ".token")

# ──────────────────────────────────────────────────────────────
# 3. CREACIÓN DE PERFILES EN USER-SERVICE
# ──────────────────────────────────────────────────────────────
step "3" "Creación de perfiles de usuario"

R=$(http POST "$GW/api/v1/users" \
  -H "Authorization: Bearer $ORGANIZER_TOKEN" \
  -d "{\"firstName\":\"Org\",\"lastName\":\"Test\",\"email\":\"$ORGANIZER_EMAIL\",\"phone\":\"1111111111\"}")
CODE=$(code_of "$R"); BODY=$(body_of "$R")
assert_code "Perfil organizador" 201 "$CODE" "$BODY"

R=$(http POST "$GW/api/v1/users" \
  -H "Authorization: Bearer $BUYER_TOKEN" \
  -d "{\"firstName\":\"Buyer\",\"lastName\":\"Test\",\"email\":\"$BUYER_EMAIL\",\"phone\":\"2222222222\"}")
CODE=$(code_of "$R"); BODY=$(body_of "$R")
assert_code "Perfil comprador" 201 "$CODE" "$BODY"

# ──────────────────────────────────────────────────────────────
# 4. CREACIÓN Y PUBLICACIÓN DE EVENTO
# ──────────────────────────────────────────────────────────────
step "4" "Crear evento"

START_DATE=$(date -d "+30 days" '+%Y-%m-%dT20:00:00' 2>/dev/null \
  || date -v+30d '+%Y-%m-%dT20:00:00')  # fallback macOS

R=$(http POST "$GW/api/v1/events" \
  -H "Authorization: Bearer $ORGANIZER_TOKEN" \
  -d "{
    \"title\": \"Festival de Prueba $TS\",
    \"description\": \"Evento creado por script de testing\",
    \"venue\": \"Luna Park\",
    \"city\": \"Buenos Aires\",
    \"country\": \"Argentina\",
    \"startDate\": \"$START_DATE\"
  }")
CODE=$(code_of "$R"); BODY=$(body_of "$R")
assert_code "Crear evento" 201 "$CODE" "$BODY"
EVENT_ID=$(extract "$R" ".id")
info "Event ID: $EVENT_ID"

# ──────────────────────────────────────────────────────────────
# 5. CREAR TIPO DE TICKET
# ──────────────────────────────────────────────────────────────
step "5" "Crear tipo de ticket"

R=$(http POST "$GW/api/v1/events/$EVENT_ID/ticket-types" \
  -H "Authorization: Bearer $ORGANIZER_TOKEN" \
  -d "{
    \"name\": \"General\",
    \"description\": \"Entrada general\",
    \"price\": 50.00,
    \"currency\": \"USD\",
    \"totalQuantity\": 100
  }")
CODE=$(code_of "$R"); BODY=$(body_of "$R")
assert_code "Crear ticket type" 201 "$CODE" "$BODY"
TICKET_TYPE_ID=$(extract "$R" ".id")
info "Ticket Type ID: $TICKET_TYPE_ID"

# ──────────────────────────────────────────────────────────────
# 6. PUBLICAR EVENTO
# ──────────────────────────────────────────────────────────────
step "6" "Publicar evento"

R=$(http PATCH "$GW/api/v1/events/$EVENT_ID/status?status=PUBLISHED" \
  -H "Authorization: Bearer $ORGANIZER_TOKEN")
CODE=$(code_of "$R"); BODY=$(body_of "$R")
assert_code "Publicar evento" 200 "$CODE" "$BODY"
EVENT_STATUS=$(extract "$R" ".status")
info "Estado del evento: $EVENT_STATUS"

# ──────────────────────────────────────────────────────────────
# 7. LISTAR EVENTOS PÚBLICOS
# ──────────────────────────────────────────────────────────────
step "7" "Listar eventos publicados (endpoint público)"

R=$(http GET "$GW/api/v1/events")
CODE=$(code_of "$R"); BODY=$(body_of "$R")
assert_code "Listar eventos" 200 "$CODE" "$BODY"
TOTAL=$(extract "$R" ".totalElements")
info "Eventos publicados: $TOTAL"

# ──────────────────────────────────────────────────────────────
# 8. CREAR ORDEN (inicia saga asíncrona)
# ──────────────────────────────────────────────────────────────
step "8" "Crear orden de compra"

# pm_card_visa es el método de pago de test de Stripe
R=$(http POST "$GW/api/v1/orders" \
  -H "Authorization: Bearer $BUYER_TOKEN" \
  -d "{
    \"items\": [{
      \"eventId\": \"$EVENT_ID\",
      \"ticketTypeId\": $TICKET_TYPE_ID,
      \"quantity\": 2
    }],
    \"paymentMethodId\": \"pm_card_visa\"
  }")
CODE=$(code_of "$R"); BODY=$(body_of "$R")
assert_code "Crear orden" 201 "$CODE" "$BODY"
ORDER_ID=$(extract "$R" ".id")
ORDER_STATUS=$(extract "$R" ".status")
info "Order ID: $ORDER_ID | Estado inicial: $ORDER_STATUS"

# ──────────────────────────────────────────────────────────────
# 9. ESPERAR CONFIRMACIÓN DE ORDEN (saga asíncrona)
# ──────────────────────────────────────────────────────────────
step "9" "Esperando confirmación de orden (saga async)"

MAX_WAIT=30
INTERVAL=3
ELAPSED=0
ORDER_STATUS=""

while [ "$ELAPSED" -lt "$MAX_WAIT" ]; do
  sleep "$INTERVAL"
  ELAPSED=$((ELAPSED + INTERVAL))

  R=$(http GET "$GW/api/v1/orders/$ORDER_ID" \
    -H "Authorization: Bearer $BUYER_TOKEN")
  ORDER_STATUS=$(extract "$R" ".status")
  info "[$ELAPSED s] Estado de la orden: $ORDER_STATUS"

  if [ "$ORDER_STATUS" = "CONFIRMED" ] || [ "$ORDER_STATUS" = "FAILED" ] || [ "$ORDER_STATUS" = "CANCELLED" ]; then
    break
  fi
done

if [ "$ORDER_STATUS" = "CONFIRMED" ]; then
  ok "Orden confirmada (CONFIRMED)"
elif [ "$ORDER_STATUS" = "FAILED" ]; then
  fail "Orden fallida (FAILED) — revisar logs de payment-service"
else
  fail "Orden no confirmada después de ${MAX_WAIT}s — estado: $ORDER_STATUS"
fi

# ──────────────────────────────────────────────────────────────
# 10. CONSULTAR PAGO POR ORDEN
# ──────────────────────────────────────────────────────────────
step "10" "Consultar pago asociado a la orden"

R=$(http GET "$GW/api/v1/payments/order/$ORDER_ID" \
  -H "Authorization: Bearer $BUYER_TOKEN")
CODE=$(code_of "$R"); BODY=$(body_of "$R")
assert_code "Obtener pago por orden" 200 "$CODE" "$BODY"
PAYMENT_STATUS=$(extract "$R" ".status")
PAYMENT_ID=$(extract "$R" ".id")
info "Payment ID: $PAYMENT_ID | Estado: $PAYMENT_STATUS"

# ──────────────────────────────────────────────────────────────
# 11. OBTENER MIS TICKETS
# ──────────────────────────────────────────────────────────────
step "11" "Obtener mis tickets"

R=$(http GET "$GW/api/v1/tickets/my?page=0&size=10" \
  -H "Authorization: Bearer $BUYER_TOKEN")
CODE=$(code_of "$R"); BODY=$(body_of "$R")
assert_code "Mis tickets" 200 "$CODE" "$BODY"
TICKET_COUNT=$(extract "$R" ".totalElements")
TICKET_ID=$(extract "$R" ".content[0].id")
info "Tickets generados: $TICKET_COUNT | Primer ticket ID: $TICKET_ID"

# ──────────────────────────────────────────────────────────────
# 12. SOLICITAR REEMBOLSO (solo si la orden está CONFIRMED)
# ──────────────────────────────────────────────────────────────
step "12" "Solicitar reembolso"

if [ "$ORDER_STATUS" = "CONFIRMED" ]; then
  R=$(http PATCH "$GW/api/v1/orders/$ORDER_ID/refund" \
    -H "Authorization: Bearer $BUYER_TOKEN")
  CODE=$(code_of "$R"); BODY=$(body_of "$R")
  assert_code "Solicitar reembolso" 202 "$CODE" "$BODY"
  REFUND_STATUS=$(extract "$R" ".status")
  info "Estado tras solicitar reembolso: $REFUND_STATUS"

  # Esperar REFUNDED
  step "12b" "Esperando REFUNDED (saga async)"
  ELAPSED=0
  while [ "$ELAPSED" -lt "$MAX_WAIT" ]; do
    sleep "$INTERVAL"
    ELAPSED=$((ELAPSED + INTERVAL))

    R=$(http GET "$GW/api/v1/orders/$ORDER_ID" \
      -H "Authorization: Bearer $BUYER_TOKEN")
    ORDER_STATUS=$(extract "$R" ".status")
    info "[$ELAPSED s] Estado de la orden: $ORDER_STATUS"

    if [ "$ORDER_STATUS" = "REFUNDED" ] || [ "$ORDER_STATUS" = "REFUND_FAILED" ]; then
      break
    fi
  done

  if [ "$ORDER_STATUS" = "REFUNDED" ]; then
    ok "Reembolso completado (REFUNDED)"
  elif [ "$ORDER_STATUS" = "REFUND_FAILED" ]; then
    fail "Reembolso fallido (REFUND_FAILED)"
  else
    fail "Reembolso no completado después de ${MAX_WAIT}s — estado: $ORDER_STATUS"
  fi
else
  info "Saltando reembolso porque la orden no está CONFIRMED"
fi

# ──────────────────────────────────────────────────────────────
# 13. MIS ÓRDENES (paginado)
# ──────────────────────────────────────────────────────────────
step "13" "Mis órdenes (paginado)"

R=$(http GET "$GW/api/v1/orders/my?page=0&size=10" \
  -H "Authorization: Bearer $BUYER_TOKEN")
CODE=$(code_of "$R"); BODY=$(body_of "$R")
assert_code "Mis órdenes" 200 "$CODE" "$BODY"
info "Total órdenes: $(extract "$R" ".totalElements")"

# ──────────────────────────────────────────────────────────────
# RESUMEN
# ──────────────────────────────────────────────────────────────
echo ""
echo "========================================="
echo " RESULTADO FINAL"
echo "========================================="
echo -e " ${GREEN}Pasaron: $PASS${NC}"
echo -e " ${RED}Fallaron: $FAIL${NC}"
echo "========================================="

[ "$FAIL" -eq 0 ] && exit 0 || exit 1
