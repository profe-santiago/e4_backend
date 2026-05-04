# EventFlow — Plataforma de Venta de Tickets

Sistema completo de venta de entradas para eventos, construido con microservicios en **Java 21 + Spring Boot** en el backend y **React + TypeScript** en el frontend.

---

## Arquitectura General

```
┌─────────────────────────────────────────────────────────────┐
│                        Frontend (React)                      │
│          Vite · React Query · Zustand · Stripe Elements      │
└─────────────────────┬───────────────────────────────────────┘
                      │ HTTP
┌─────────────────────▼───────────────────────────────────────┐
│                    API Gateway :8080                         │
│            Rate Limiting · CORS · Circuit Breakers           │
└──┬──────────┬──────────┬──────────┬──────────┬──────────────┘
   │          │          │          │          │
:8090      :8081      :8082      :8083      :8084   :8085
auth     user      event    ticket   payment  notif
service  service   service  service  service  service
   │          │          │          │          │
 auth_db  user_db  event_db  ticket_db payment_db notif_db
(5442)   (5443)   (5444)    (5447)    (5445)   (5446)

              RabbitMQ :5672 / UI :15672
              Redis     :6379
```

---

## Microservicios

| Servicio | Puerto | Responsabilidad |
|----------|--------|-----------------|
| **api-gateway** | 8080 | Gateway centralizado con rate limiting, CORS y circuit breakers |
| **auth-service** | 8090 | Autenticación y emisión de JWT |
| **user-service** | 8081 | Gestión de perfiles de usuario |
| **event-service** | 8082 | Catálogo de eventos, tipos de ticket y gestión de stock |
| **ticket-service** | 8083 | Órdenes de compra y tickets físicos (QR) |
| **payment-service** | 8084 | Procesamiento de pagos con Stripe |
| **notification-service** | 8085 | Notificaciones in-app y email |

## Infraestructura

| Componente | Uso |
|------------|-----|
| **PostgreSQL 16** | Base de datos por servicio (Database-per-Service) |
| **RabbitMQ 3** | Mensajería asíncrona — patrón Saga coreografiado |
| **Redis 7** | Cache para rate limiting en el API Gateway |
| **Cloudinary** | Almacenamiento de imágenes de eventos |
| **Stripe** | Procesamiento de pagos con tarjeta (USD) |
| **Docker Compose** | Orquestación completa del stack |

## Patrones de Diseño

- **Clean Architecture / Hexagonal**: capas `domain` → `application` → `infrastructure` en cada servicio
- **Domain-Driven Design (DDD)**: Aggregate Roots, Value Objects, Rich Domain Models
- **Saga Coreografiada**: flujo de compra distribuido con eventos en RabbitMQ
- **CQRS Light**: separación de comandos y queries en la capa de aplicación
- **Database-per-Service**: cada microservicio tiene su propia base de datos PostgreSQL

---

## Flujo de Compra (Saga)

```
Cliente → POST /api/v1/orders  { items, paymentMethodId }
    │
    ▼
ticket-service  →  [stock.reserve]  →  event-service
                                            │ reserva stock
                                       [stock.reserved]
                                            │
ticket-service ←──────────────────────────┘
    │ confirma orden + genera tickets
    │
    ├──→  [order.confirmed]  →  payment-service
    │                               │ cobra con Stripe (USD)
    │                          [payment.completed]
    │                               │
    │         notification-service ←┘
    │              │ email + notif in-app
    │
    └──→  [order.confirmed]  →  notification-service
                                    │ email "orden confirmada"
```

**Compensaciones (rollback):**
- Si el pago falla → `payment.failed` → ticket-service libera stock → notifica al usuario
- Si el stock no está disponible → `stock.reservation.failed` → orden cancelada

---

## Flujo de Registro / Login

```
1. POST /api/v1/auth/register   → crea credenciales en auth_db
2. POST /api/v1/users           → crea perfil en user_db (requiere JWT del paso 1)
3. POST /api/v1/auth/login      → devuelve JWT
```

> **Nota**: el registro es un proceso de dos pasos no atómico. Si el perfil de usuario no existe (404), la UI muestra un formulario para completarlo.

---

## Frontend

**Stack**: React 19 · TypeScript · Vite · React Router v7 · TanStack Query · Zustand · React Hook Form · Zod · Stripe Elements

### Arquitectura Frontend

```
src/
├── core/
│   ├── di/               # Contextos de inyección de dependencias
│   └── http/             # Axios instance + interceptors JWT
├── features/             # Módulos por dominio
│   ├── auth/
│   │   ├── domain/       # Entidades, puertos
│   │   ├── application/  # Use Cases
│   │   └── ui/           # Páginas, hooks
│   ├── events/
│   ├── orders/
│   ├── tickets/
│   ├── payments/
│   ├── profile/
│   └── notifications/
├── router/               # AppRouter, PrivateRoute, RoleRoute
├── shared/
│   ├── components/       # Layout, Sidebar, GuestLayout
│   └── config/           # theme, navigation, formOptions
└── store/                # auth.store (Zustand)
```

### Roles y rutas

| Rol | Acceso |
|-----|--------|
| **Guest** (no autenticado) | `/` (listado de eventos), `/events/:id` (detalle) |
| **BUYER** | + `/events/:id/checkout`, `/orders`, `/tickets`, `/profile`, `/notifications` |
| **ADMIN** | + `/my-events`, `/events/new`, `/events/:id/edit`, `/events/:id/overview`, `/admin/*` |

### Integración de pagos (Stripe Elements)

El checkout usa `CardNumberElement`, `CardExpiryElement` y `CardCvcElement` de `@stripe/react-stripe-js`. Los datos de tarjeta nunca tocan el servidor propio — Stripe los tokeniza directamente desde el navegador y devuelve un `paymentMethodId` que se envía al backend.

**Moneda estándar: USD** para todos los eventos y tickets.

---

## Inicio Rápido

### Prerrequisitos

- Docker Desktop o Docker Engine + Docker Compose
- Node.js 20+ (para el frontend)
- Java 21 JDK (para desarrollo local del backend)

### 1. Clonar el Repositorio

```bash
git clone <repository-url>
cd e4_backend
```

### 2. Configurar Variables de Entorno

```bash
cp .env.example .env
# Editar .env con tus valores reales
```

**Variables requeridas en `backend/.env`:**

```env
# JWT
JWT_SECRET=clave-minimo-256-bits-para-hs256

# Stripe
STRIPE_SECRET_KEY=sk_test_...

# Cloudinary (imágenes de eventos)
CLOUDINARY_CLOUD_NAME=...
CLOUDINARY_API_KEY=...
CLOUDINARY_API_SECRET=...

# Email SMTP (para notificaciones por email)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=tu-email@gmail.com
MAIL_PASSWORD=app-password-de-16-caracteres
MAIL_FROM=tu-email@gmail.com
```

**Variables requeridas en `frontend/eventFlow/.env`:**

```env
VITE_API_URL=http://localhost:8080
VITE_STRIPE_PUBLISHABLE_KEY=pk_test_...
```

### 3. Levantar el Backend

```bash
cd backend
docker compose up -d

# Ver logs
docker compose logs -f

# Logs de un servicio específico
docker compose logs -f event-service
```

### 4. Levantar el Frontend

```bash
cd frontend/eventFlow
npm install
npm run dev
# Accesible en http://localhost:5173
```

### 5. Verificar Salud del Sistema

```bash
# API Gateway health
curl http://localhost:8080/actuator/health

# RabbitMQ Management UI
open http://localhost:15672
# Usuario: guest / Password: guest
```

---

## API — Ejemplos de Uso

Todos los endpoints pasan por el gateway en `http://localhost:8080`.

### Registro + Login

```bash
# 1. Registrar credenciales
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"secret123","firstName":"Ana","lastName":"López"}'

# Respuesta incluye token JWT para crear el perfil
# { "userId":"...", "email":"...", "token":"eyJ...", "role":"BUYER" }

# 2. Crear perfil de usuario (con el token del paso 1)
curl -X POST http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Ana","lastName":"López"}'

# 3. Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"secret123"}'

# Respuesta:
# { "userId":"...", "email":"...", "token":"eyJ...", "role":"BUYER" }
```

> Roles posibles: `BUYER` (comprador) y `ADMIN` (organizador).

### Listar Eventos

```bash
# Públicos — sin autenticación
curl "http://localhost:8080/api/v1/events?status=PUBLISHED&page=0&size=20"

# Con filtros
curl "http://localhost:8080/api/v1/events?status=PUBLISHED&categoryId=1&search=rock&city=Buenos+Aires"
```

### Crear Orden de Compra

```bash
TOKEN="<tu-jwt-token>"

curl -X POST http://localhost:8080/api/v1/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "items": [{ "eventId": "uuid-del-evento", "ticketTypeId": 1, "quantity": 2 }],
    "paymentMethodId": "pm_..."
  }'

# Respuesta: { "id":"...", "status":"PENDING", "totalAmount":50.00, ... }
# El procesamiento es asíncrono — consultar estado con GET /api/v1/orders/{id}
```

> El `paymentMethodId` lo genera Stripe en el frontend a partir de los datos de tarjeta. En pruebas usar la tarjeta `4242 4242 4242 4242`.

---

## Base de Datos

Cada servicio tiene su propia base de datos PostgreSQL:

| Servicio | Base de datos | Puerto host |
|----------|--------------|-------------|
| auth-service | auth_db | 5442 |
| user-service | user_db | 5443 |
| event-service | event_db | 5444 |
| payment-service | payment_db | 5445 |
| notification-service | notification_db | 5446 |
| ticket-service | ticket_db | 5447 |

```bash
# Conectar a una base de datos
psql -h localhost -p 5444 -U testuser -d event_db
# Password: testuser
```

Las migraciones se aplican automáticamente con **Flyway** al iniciar cada servicio.

---

## Rate Limiting

| Tipo de usuario | Requests/min | Endpoint |
|-----------------|-------------|---------|
| Anónimo (por IP) | 100 | General |
| Autenticado | 200 | General |
| Creación de orden | 10 | POST /orders |

Headers de respuesta:
```
X-RateLimit-Limit: 200
X-RateLimit-Remaining: 187
```

Si excedés el límite: **HTTP 429 Too Many Requests**

---

## Circuit Breakers

Cada servicio tiene un circuit breaker (Resilience4j) configurado en el gateway:

- Después de **5 llamadas fallidas** (>50%), el circuito se abre
- Requests retornan fallback inmediatamente mientras el circuito está abierto
- Después de **10 segundos**, intenta recuperación (HALF_OPEN)

```bash
curl http://localhost:8080/actuator/circuitbreakers
```

---

## Documentación API (Swagger)

Cada servicio expone documentación OpenAPI:

| Servicio | URL |
|---------|-----|
| Auth | http://localhost:8090/api/docs |
| User | http://localhost:8081/api/docs |
| Event | http://localhost:8082/api/docs |
| Ticket | http://localhost:8083/api/docs |
| Payment | http://localhost:8084/api/docs |
| Notification | http://localhost:8085/api/docs |

---

## Estructura de un Servicio (Backend)

```
service-name/
├── src/main/java/com/tickets/service_name/
│   ├── [bounded-context]/
│   │   ├── domain/               # Capa de dominio (sin dependencias externas)
│   │   │   ├── Entity.java       # Aggregate Root
│   │   │   ├── ValueObject.java
│   │   │   └── Repository.java   # Puerto (interfaz)
│   │   ├── application/          # Casos de uso
│   │   │   ├── CreateXUseCase.java
│   │   │   └── dto/
│   │   └── infrastructure/       # Adaptadores
│   │       ├── persistence/      # JPA entities + repositorios Spring Data
│   │       ├── rest/             # Controllers + DTOs REST
│   │       └── messaging/        # Producers/Consumers RabbitMQ
│   └── config/                   # Beans de configuración Spring
└── src/main/resources/
    ├── application.properties
    └── db/migration/             # Migraciones Flyway (V1__, V2__...)
```

---

## Comandos Útiles

```bash
# Reconstruir un servicio específico
docker compose up -d --build event-service

# Detener todo
docker compose down

# Detener y eliminar volúmenes (borra todos los datos)
docker compose down -v

# Ver logs en tiempo real
docker compose logs -f

# RabbitMQ Management
open http://localhost:15672  # guest / guest
```

---

## Estado del Proyecto

### Implementado y funcional

- [x] API Gateway con rate limiting y circuit breakers
- [x] Autenticación JWT (registro, login, roles BUYER/ADMIN)
- [x] Gestión de eventos (CRUD, categorías, imágenes vía Cloudinary)
- [x] Tipos de ticket con períodos de venta configurables
- [x] Flujo de compra completo (Saga coreografiada con RabbitMQ)
- [x] Pagos con Stripe (integración real, moneda USD)
- [x] Tickets con QR para validación en puerta
- [x] Notificaciones in-app persistentes (backend completo)
- [x] Reembolsos (implementado en backend)
- [x] Frontend completo con React + TypeScript

### Pendiente / En progreso

- [ ] **Notificaciones por email**: el backend está implementado pero requiere configurar credenciales SMTP reales (`MAIL_PASSWORD` en `.env`). Ver sección de configuración.
- [ ] **Revisión del flujo de reembolsos**: implementado en backend, falta UI en el frontend y pruebas end-to-end del flujo completo.
- [ ] **Badge de notificaciones no leídas** en el sidebar del frontend.
- [ ] Notificaciones en tiempo real (WebSockets / Server-Sent Events).
- [ ] Reservas con TTL (timeout automático de órdenes PENDING).
- [ ] Mapa interactivo de asientos.
- [ ] Observabilidad completa (Prometheus + Grafana + Zipkin).
- [ ] Tests de carga con Gatling.

---

## Licencia

Proyecto privado.
