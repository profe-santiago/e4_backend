# API Gateway - E4 Backend

API Gateway centralizado para la plataforma de tickets E4, construido con **Spring Cloud Gateway**.

## Características

### 1. **Enrutamiento Centralizado**
Todos los servicios backend son accesibles a través del gateway en el puerto `8080`:

| Servicio | Ruta Original | Ruta a través del Gateway |
|----------|---------------|---------------------------|
| Auth Service | `localhost:8090/api/v1/auth/**` | `localhost:8080/api/v1/auth/**` |
| User Service | `localhost:8081/api/v1/users/**` | `localhost:8080/api/v1/users/**` |
| Event Service | `localhost:8082/api/v1/events/**` | `localhost:8080/api/v1/events/**` |
| Event Service | `localhost:8082/api/v1/categories/**` | `localhost:8080/api/v1/categories/**` |
| Ticket Service | `localhost:8083/api/v1/orders/**` | `localhost:8080/api/v1/orders/**` |
| Ticket Service | `localhost:8083/api/v1/tickets/**` | `localhost:8080/api/v1/tickets/**` |
| Payment Service | `localhost:8084/api/v1/payments/**` | `localhost:8080/api/v1/payments/**` |

### 2. **Rate Limiting Inteligente**

El gateway implementa rate limiting basado en **Token Bucket Algorithm** con Redis, diferenciando entre:

#### Límites por Tipo de Usuario

| Tipo | Identificador | Requests/min | Burst Capacity |
|------|--------------|--------------|----------------|
| **Anónimo (IP)** | Dirección IP | 100 | 150 |
| **Autenticado** | User ID (JWT) | 200 | 300 |
| **Order Creation** | User ID o IP | 10 | 15 |

#### Cabeceras de Respuesta
El gateway agrega headers informativos en cada respuesta:
```
X-RateLimit-Limit: 300
X-RateLimit-Remaining: 287
X-RateLimit-Reset: 1678901234
```

#### Respuesta cuando se excede el límite
```http
HTTP/1.1 429 Too Many Requests
X-RateLimit-Retry-After-Seconds: 60
Content-Type: application/json

{
  "error": "Too Many Requests",
  "message": "Rate limit exceeded. Please try again later."
}
```

### 3. **CORS Centralizado**

Configuración CORS unificada para todos los servicios:

- **Allowed Origins**: `localhost:*`, `127.0.0.1:*`, `*.yourdomain.com`
- **Allowed Methods**: GET, POST, PUT, PATCH, DELETE, OPTIONS
- **Allowed Headers**: Authorization, Content-Type, Accept, X-Requested-With, X-XSRF-TOKEN, X-Correlation-ID
- **Exposed Headers**: X-RateLimit-*, X-Correlation-ID
- **Credentials**: Permitidas
- **Max Age**: 3600 segundos

### 4. **Circuit Breakers con Resilience4j**

Cada servicio backend tiene su propio circuit breaker configurado:

#### Configuración
- **Sliding Window**: 10 requests
- **Minimum Calls**: 5 (antes de evaluar)
- **Failure Rate Threshold**: 50%
- **Slow Call Threshold**: 50% (>2 segundos)
- **Wait Duration (Open → Half-Open)**: 10 segundos
- **Permitted Calls in Half-Open**: 3

#### Estados del Circuit Breaker
1. **CLOSED**: Funcionamiento normal
2. **OPEN**: Servicio caído, retorna fallback inmediatamente
3. **HALF_OPEN**: Probando recuperación

#### Endpoints de Fallback
Cuando un servicio está caído, el gateway retorna:
```json
{
  "timestamp": "2024-03-22T10:30:00",
  "status": 503,
  "error": "Service Unavailable",
  "message": "Event Service is temporarily unavailable. Please try again later.",
  "suggestion": "Our team has been notified. Please try again in a few moments."
}
```

### 5. **Monitoreo con Actuator**

Endpoints expuestos para observabilidad:

```bash
# Health check
curl http://localhost:8080/actuator/health

# Estado de circuit breakers
curl http://localhost:8080/actuator/circuitbreakers

# Información general
curl http://localhost:8080/actuator/info
```

## Configuración

### Variables de Entorno

| Variable | Descripción | Default |
|----------|-------------|---------|
| `SERVER_PORT` | Puerto del gateway | 8080 |
| `JWT_SECRET` | Secret para validar JWT | (requerido) |
| `REDIS_HOST` | Host de Redis | localhost |
| `REDIS_PORT` | Puerto de Redis | 6379 |
| `REDIS_PASSWORD` | Password de Redis | (vacío) |
| `AUTH_SERVICE_HOST` | Host del auth-service | localhost |
| `AUTH_SERVICE_PORT` | Puerto del auth-service | 8090 |
| `USER_SERVICE_HOST` | Host del user-service | localhost |
| `USER_SERVICE_PORT` | Puerto del user-service | 8081 |
| `EVENT_SERVICE_HOST` | Host del event-service | localhost |
| `EVENT_SERVICE_PORT` | Puerto del event-service | 8082 |
| `TICKET_SERVICE_HOST` | Host del ticket-service | localhost |
| `TICKET_SERVICE_PORT` | Puerto del ticket-service | 8083 |
| `PAYMENT_SERVICE_HOST` | Host del payment-service | localhost |
| `PAYMENT_SERVICE_PORT` | Puerto del payment-service | 8084 |

### Rate Limiting Custom

Puedes ajustar los límites en `application.properties`:

```properties
# General (por IP)
rate.limit.default.replenishRate=100
rate.limit.default.burstCapacity=150

# Usuarios autenticados
rate.limit.authenticated.replenishRate=200
rate.limit.authenticated.burstCapacity=300

# Creación de órdenes
rate.limit.order.replenishRate=10
rate.limit.order.burstCapacity=15
```

## Arquitectura

```
┌─────────────────────────────────────────────────────────────────┐
│                         Client (Frontend)                        │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            │ HTTP Requests
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                       API Gateway (8080)                         │
│                                                                   │
│  ┌─────────────┐  ┌──────────────┐  ┌────────────────────────┐  │
│  │ CORS Filter │→ │ Rate Limiter │→ │ Circuit Breaker Filter │  │
│  └─────────────┘  └──────┬───────┘  └────────────────────────┘  │
│                           │                                       │
│                           ▼                                       │
│                      ┌─────────┐                                 │
│                      │  Redis  │                                 │
│                      └─────────┘                                 │
└───────────────────────────┬─────────────────────────────────────┘
                            │
         ┌──────────────────┼──────────────────┬──────────────────┐
         ▼                  ▼                  ▼                  ▼
   ┌──────────┐       ┌──────────┐      ┌──────────┐      ┌──────────┐
   │   Auth   │       │   User   │      │  Event   │      │  Ticket  │
   │ Service  │       │ Service  │      │ Service  │      │ Service  │
   │  (8090)  │       │  (8081)  │      │  (8082)  │      │  (8083)  │
   └──────────┘       └──────────┘      └──────────┘      └──────────┘
```

## Uso

### Desarrollo Local

```bash
# 1. Asegurar que Redis esté corriendo
docker run -d -p 6379:6379 redis:7-alpine

# 2. Configurar variables de entorno
export JWT_SECRET="your-secret-key-min-256-bits-for-hs256-algorithm-security"

# 3. Ejecutar el gateway
./gradlew bootRun
```

### Con Docker Compose

```bash
# Levantar todo el stack (incluye gateway, Redis y todos los servicios)
docker-compose up -d

# Ver logs del gateway
docker-compose logs -f api-gateway

# Verificar salud
curl http://localhost:8080/actuator/health
```

## Testing

### 1. Test de Rate Limiting

```bash
# Como usuario anónimo (límite: 100/min)
for i in {1..110}; do
  curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/api/v1/events
done
# Últimas 10 requests deberían retornar 429

# Como usuario autenticado (límite: 200/min)
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
for i in {1..210}; do
  curl -s -o /dev/null -w "%{http_code}\n" \
    -H "Authorization: Bearer $TOKEN" \
    http://localhost:8080/api/v1/events
done
```

### 2. Test de Circuit Breaker

```bash
# 1. Detener event-service
docker-compose stop event-service

# 2. Intentar acceder
curl http://localhost:8080/api/v1/events
# Retorna fallback después de algunos intentos

# 3. Reiniciar servicio
docker-compose start event-service

# 4. Esperar 10 segundos (wait duration)
# El circuit breaker se recuperará automáticamente
```

### 3. Test de CORS

```bash
curl -X OPTIONS http://localhost:8080/api/v1/events \
  -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: GET" \
  -v

# Debería retornar headers CORS apropiados
```

## Seguridad

### JWT Validation
El gateway **valida** JWTs solo para determinar rate limits (usuario autenticado vs anónimo). **No bloquea** requests con tokens inválidos - esa responsabilidad es de cada servicio backend.

### IP Extraction
Para rate limiting por IP, el gateway considera:
1. Header `X-Forwarded-For` (si existe)
2. `RemoteAddress` como fallback

**Importante**: En producción, configura tu load balancer/proxy para agregar `X-Forwarded-For` correctamente.

### Rate Limiting Anti-Bot
El límite estricto en `/orders` (10/min) previene:
- Ataques de bots comprando tickets masivamente
- Scalpers automatizados
- Abuso de la API

## Referencias

- [Spring Cloud Gateway Docs](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/)
- [Resilience4j Circuit Breaker](https://resilience4j.readme.io/docs/circuitbreaker)
- [Redis Rate Limiting](https://redis.io/docs/manual/patterns/rate-limiter/)
- [Token Bucket Algorithm](https://en.wikipedia.org/wiki/Token_bucket)
