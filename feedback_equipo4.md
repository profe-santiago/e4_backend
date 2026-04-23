# Feedback – Equipo 4: Aplicación Web de Boletos en Línea
**Stack:** Spring Boot (Java 21) · React · PostgreSQL  
**Fecha de revisión:** 22 de abril de 2025

---

## Resumen general

Este es el proyecto más ambicioso conceptualmente del grupo y el que demuestra mayor dominio de los patrones de diseño y arquitectura vistos en el curso. La implementación de microservicios reales con Clean Architecture, el patrón Saga coreografiado con RabbitMQ, Circuit Breakers, Rate Limiting y una base de datos por servicio son conceptos de nivel profesional bien aplicados. El equipo merece un reconocimiento especial por la profundidad técnica del trabajo.

---

## Lo que están haciendo bien ✅

### Arquitectura multicapas dentro de cada microservicio

Cada microservicio implementa Clean Architecture con 3 capas claramente separadas (tomando `event-service` como ejemplo):

- **`domain/`** — entidades puras, interfaces de repositorio y lógica de negocio. Esta capa no depende de ningún framework. `Event`, `TicketType`, `EventRepository` (interfaz) viven aquí.
- **`application/`** — casos de uso que orquestan el dominio: `CreateEventUseCase`, `ReserveStockUseCase`, `ReleaseStockUseCase`. Cada operación de negocio es una clase con una sola responsabilidad.
- **`infrastructure/`** — adaptadores que conectan el dominio con el mundo exterior: `persistence/` (JPA), `rest/` (controllers HTTP), `messaging/` (RabbitMQ).

El dominio no depende de la infraestructura — la infraestructura depende del dominio. Esta inversión de dependencias es el corazón de Clean Architecture y está correctamente aplicada.

### Patrón DTO en cada capa de comunicación

Tienen DTOs separados para cada frontera:

- `CategoryRequest` / `CategoryResponse` en la capa REST (lo que entra y sale por HTTP).
- `CreateEventCommand` / `UpdateEventCommand` en la capa de aplicación (lo que reciben los use cases).
- DTOs de mensajería (`StockReserveCommand`, `StockReservedEvent`, etc.) para la comunicación entre servicios por RabbitMQ.

Esta separación de DTOs por capa es una práctica avanzada que evita que los cambios en una frontera propaguen cambios a las otras.

### Patrón Use Case con responsabilidad única

Cada operación de negocio es una clase separada: `CreateEventUseCase`, `GetEventByIdUseCase`, `ChangeEventStatusUseCase`, `ReserveStockUseCase`, `ReleaseStockUseCase`. Esto aplica el Principio de Responsabilidad Única de forma muy disciplinada: cada clase tiene exactamente una razón para cambiar.

### Patrón Saga Coreografiado (comunicación asíncrona)

El flujo de compra distribuido usa eventos en RabbitMQ para coordinar los servicios sin un orquestador central. `ticket-service` publica `stock.reserve`, `event-service` escucha y responde con `stock.reserved`, y el proceso continúa de forma reactiva. Esto es exactamente el patrón de comunicación asíncrona entre microservicios: cada servicio reacciona a eventos sin depender directamente de los otros.

### Value Object: clase `Money`

La clase `Money` en el dominio de `ticket-type` encapsula la representación de dinero: cantidad y moneda en un solo objeto con sus propias reglas. Este es un Value Object de DDD — un concepto avanzado bien aplicado para evitar errores de representación monetaria.

### Database per Service Pattern

Cada microservicio tiene su propia base de datos PostgreSQL independiente con su propio esquema y sus propias migraciones Flyway. Esto garantiza que los servicios estén desacoplados también a nivel de datos: un cambio en el esquema de `event-service` no afecta a `ticket-service`.

### API Gateway con Circuit Breaker y Rate Limiting

El `api-gateway` protege el sistema de fallos en cascada: si un servicio backend falla repetidamente, el circuit breaker se abre y las peticiones retornan un fallback inmediatamente en lugar de bloquear. El rate limiting diferenciado (100 req/min anónimo, 200 autenticado, 10 para crear órdenes) protege contra abuso.

### Swagger / OpenAPI por servicio

Cada microservicio expone su propia documentación OpenAPI en `/api/docs`. Esto es coherente con la arquitectura: cada servicio es autónomo y documenta su propia API.

### Tests unitarios

Tienen `CategoryServiceImplTest`, `EventServiceImplTest`, `StockReservationServiceImplTest` y `TicketTypeServiceImplTest`. El testing no es el foco principal del curso, pero su presencia demuestra madurez técnica.

### README excepcional

El README incluye: diagrama de secuencia del flujo de compra completo, tabla de microservicios con puertos, instrucciones de Docker, ejemplos de curl con request/response, documentación de rate limiting y circuit breakers, y guía de desarrollo para agregar nuevos endpoints. Es documentación de nivel profesional.

---

## Áreas de mejora 🔧

### Sin frontend entregado

El README menciona React como frontend, pero solo hay código de backend en el repositorio. Para la actividad 10 y la presentación final se requiere el frontend conectado a la API. No necesita ser complejo — con un flujo básico de registro, lista de eventos y compra de un boleto es suficiente para demostrar que el sistema funciona end-to-end.

### Verificar que todos los servicios estén al mismo nivel de implementación

Se revisó `event-service` en detalle y está muy bien implementado. Asegúrense de que los demás servicios (`ticket-service`, `payment-service`, `notification-service`) estén igualmente completos y que el `docker-compose up` levante todo el stack sin errores. Para la presentación es imprescindible poder hacer un flujo de compra completo en vivo.

### La ambición arquitectónica no debe sacrificar la demo

La arquitectura es muy avanzada, pero el evaluador también necesita ver el sistema funcionando en pantalla. Prioricen tener un flujo end-to-end demo-able sobre implementar todos los casos edge de cada servicio.

---

## Calificación conceptual

| Criterio | Evaluación |
|---|---|
| Arquitectura multicapas (Clean Architecture) | ✅ Excelente — implementación real |
| DTOs por capa (REST, application, messaging) | ✅ Excelente |
| Patrón Use Case (SRP) | ✅ Excelente |
| Arquitectura de microservicios | ✅ Implementación real — no solo de nombre |
| Patrón Saga (comunicación asíncrona) | ✅ Excelente |
| Database per Service | ✅ Correcto |
| Value Objects (DDD) | ✅ Presente |
| API Gateway + Circuit Breaker | ✅ Implementado |
| Swagger / OpenAPI | ✅ Por servicio |
| Diseño RESTful | ✅ Sigue convenciones |
| README / Documentación | ✅ Excepcional |
| Frontend conectado | ❌ No entregado |

---

## Recomendación final

El proyecto con mayor dominio técnico del grupo. Para la presentación final el foco debe ser: (1) tener el frontend conectado aunque sea básico, (2) poder hacer un demo completo end-to-end del flujo de compra, y (3) explicar con sus propias palabras qué problema resuelve cada patrón que eligieron. Si logran eso, tienen la mejor presentación del semestre.

---

## Sugerencias adicionales de buenas prácticas

Estas son mejoras aplicables en el tiempo que queda, sin afectar la arquitectura:

**1. Agregar validación con Bean Validation en los DTOs de request**
Algunos DTOs de request pueden no tener anotaciones de validación. Agregar `@NotNull`, `@NotBlank`, `@Min`, `@Size` según corresponda, junto con `@Valid` en los controllers, da un primer nivel de validación antes de que el request llegue al use case:
```java
public class CreateEventRequest {
    @NotBlank(message = "El nombre es requerido")
    private String name;

    @NotNull(message = "La fecha es requerida")
    @Future(message = "La fecha debe ser futura")
    private LocalDateTime date;
}
```

**2. Usar constantes para los nombres de exchanges y queues de RabbitMQ**
Los nombres de queues y exchanges como `"stock.reserve"` o `"order.confirmed"` probablemente aparecen en múltiples clases. Centralizar esos strings en una clase de constantes evita errores por typos:
```java
public final class RabbitMQConstants {
    public static final String STOCK_RESERVE    = "stock.reserve";
    public static final String STOCK_RESERVED   = "stock.reserved";
    public static final String ORDER_CONFIRMED  = "order.confirmed";
    // ...
}
```

**3. Documentar las variables de entorno requeridas por servicio**
Cada servicio probablemente requiere variables como `JWT_SECRET`, `DB_URL`, `RABBITMQ_HOST`. Asegúrense de que el `.env.example` en la raíz cubra todas las variables requeridas con comentarios que expliquen para qué sirve cada una.

**4. Agregar comentarios explicativos en el flujo Saga**
Los consumidores de RabbitMQ (`StockReserveConsumer`, `OrderCancelledConsumer`) son el corazón de la Saga y son los más difíciles de entender para alguien que lee el código por primera vez. Agregar un comentario breve al inicio de cada consumer explicando qué evento escucha, qué hace y qué evento publica como resultado facilita mucho la revisión y la presentación.

**5. Crear un script de prueba del flujo completo**
Ya tienen `test-gateway.sh` en el repositorio, lo que es excelente. Asegúrense de que ese script cubra el flujo completo (registro → login → listar eventos → crear orden → consultar estado de la orden) y que funcione sin errores. Es la herramienta perfecta para demostrar el sistema en la presentación si el frontend no está listo a tiempo.
