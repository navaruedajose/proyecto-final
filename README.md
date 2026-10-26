# Sistema de Tickets — Backend en Spring Boot

Versión **Spring Boot (Java 17)** del backend del Sistema de Tickets (helpdesk).
Es el mismo dominio del proyecto original en Node.js + Express, reescrito con
la arquitectura por capas que se espera de una aplicación Spring.

---

## Contenido

- [Qué es](#qué-es)
- [Arrancar en 3 pasos](#arrancar-en-3-pasos)
- [Arquitectura](#arquitectura)
- [Estructura de paquetes](#estructura-de-paquetes)
- [Endpoints](#endpoints)
- [Decisiones de diseño](#decisiones-de-diseño)
- [Enumeraciones del dominio](#enumeraciones-del-dominio)
- [Manejo de errores](#manejo-de-errores)
- [Formato de respuesta](#formato-de-respuesta)
- [Qué falta para que sea funcional](#qué-falta-para-que-sea-funcional)
- [Verificación](#verificación)

---

## Qué es

Un sistema de gestion de incidentes (tickets) para una mesa de ayuda. Cubre el
ciclo completo:

```
Solicitante abre un ticket
        │
        ▼
   NUEVO ──▶ ABIERTO ──▶ EN_PROGRESO ──▶ RESUELTO ──▶ CERRADO
               │              │              │
               ▼              ▼              │
           CANCELADO      EN_ESPERA ────────┘
                             │
                             └──▶ EN_PROGRESO  (vuelve al trabajo)
```

**8 módulos funcionales:** autenticación, usuarios, categorías, tickets,
comentarios, adjuntos, reportes y notificaciones.

**3 roles:** `SOLICITANTE` (abre y sigue sus tickets), `AGENTE` (atiende y
resuelve) y `ADMINISTRADOR` (todo, más gestion de usuarios y categorías).

---

## Arrancar en 3 pasos

### 1. Tener un JDK 17 o superior

```bat
java -version
```

Si responde `1.8.0_431`, es Java 8 y **no sirve**: Spring Boot 3.x exige Java
17 como mínimo. Descargar un JDK 17 de
[Adoptium](https://adoptium.net/temurin/releases/?versión=17) o de Oracle.

### 2. Arrancar

```bat
mvn spring-boot:run
```

La primera vez tarda varios minutos (descarga las dependencias). El servicio
queda en **<http://localhost:4000>**.

### 3. Comprobar

```bat
curl http://localhost:4000/vivo
curl http://localhost:4000/salud
```

También hay lanzadores para no escribir las órdenes largas:

| Script | Que hace |
|---|---|
| `ejecutar.bat` | Arranca la aplicación. |
| `compilar.bat` | Empaqueta el fat jar en `target\`. |
| `verificar.bat` | Compila y consulta los endpoints de diagnóstico. |

**Guía detallada, problemas frecuentes y notas sobre el JDK:
[`docs/COMO-EJECUTAR.md`](docs/COMO-EJECUTAR.md).**

---

## Arquitectura

Cuatro capas por módulo, con dependencias en **un solo sentido**:

```
    HTTP
     │
     ▼
┌──────────────┐
│  controller  │  Traduce HTTP ↔ servicio. No tiene reglas de negocio.
└──────┬───────┘
       │
       ▼
┌──────────────┐
│   service    │  Reglas de negocio. No sabe que existe HTTP ni SQL.
└──────┬───────┘
       │
       ▼
┌──────────────┐
│  repository  │  Acceso a datos. INTERFAZ: sin implementacion aun.
└──────────────┘
       │
       ▼
   PostgreSQL

   dto/   Atraviesa todas las capas: es lo unico que cruza el borde HTTP.
```

**La regla que hace que esto sirva de algo:** ninguna capa conoce a la que
tiene por encima. El repositorio no sabe que existe el servicio; el servicio no
sabe que existe el controlador. Eso permite probar las reglas de negocio sin
levantar un servidor ni una base de datos, y cambiar de PostgreSQL a otra cosa
tocando una sola capa.

**Por que DTOs y no entidades.** Lo que viaja por HTTP nunca es la fila de la
base de datos. Un DTO es una decisión explícita sobre que se expone. Si el DTO
no tiene el campo `contrasenaHash`, no hay forma de filtrarlo por descuido: el
campo no existe en el objeto que se serializa.

---

## Estructura de paquetes

```
src/main/java/co/edu/sistematickets/
│
├── SistemaTicketsApplication.java      Punto de entrada (@SpringBootApplication)
│
├── config/                             Configuracion transversal
│   ├── ManejadorGlobalDeErrores.java   @RestControllerAdvice: unifica los errores
│   ├── ConfiguracionCors.java          Origenes permitidos, cabeceras expuestas
│   ├── ControladorDeSistema.java       /salud, /vivo, /
│   └── (OpenAPI / propiedades)         Pendiente
│
├── comun/                              Piezas compartidas por todos los módulos
│   ├── RespuestaApi.java               Envoltorio unico de todas las respuestas
│   ├── DetalleError.java               Estructura de un error
│   ├── PaginacionDto.java              Base de los filtros paginados
│   ├── ExcepcionAplicacion.java        Base: sabe su propio código HTTP
│   ├── NoEncontradoExcepcion.java      404
│   ├── SolicitudInvalidaExcepcion.java 400
│   ├── SinPermisoExcepcion.java        403
│   ├── ConflictoEstadoExcepcion.java   409 (transicion de estado ilegal)
│   └── RecursoDuplicadoExcepcion.java  409 (unicidad)
│
├── dominio/                            Enumeraciones = tipos ENUM de PostgreSQL
│   ├── RolUsuario.java                 SOLICITANTE | AGENTE | ADMINISTRADOR
│   ├── EstadoTicket.java               La máquina de estados vive aquí
│   ├── PrioridadTicket.java            BAJA | MEDIA | ALTA | CRITICA
│   ├── CanalNotificacion.java          EMAIL | SMS | AMBOS
│   └── EstadoNotificacion.java         PENDIENTE | ENVIADA | FALLIDA
│
└── modulos/                            Un paquete por dominio funcional
    ├── autenticacion/  dto · repository · service · controller
    ├── usuarios/       dto · repository · service · controller
    ├── categorias/     dto · repository · service · controller
    ├── tickets/        dto · repository · service · controller · mapper
    ├── comentarios/    dto · repository · service · controller
    ├── adjuntos/       dto · repository · service · controller
    ├── reportes/       dto · service · controller
    └── notificaciones/ dto · service · controller
```

**62 archivos Java · ~4.980 líneas.** Todos los módulos repiten la misma
distribución, así que saber donde está algo en `tickets` es saber donde está en
`adjuntos`.

---

## Endpoints

**Base:** `/api/v1`

### Diagnóstico

| Método | Ruta | Que hace |
|---|---|---|
| `GET` | `/vivo` | El proceso existe. Para el *liveness probe*. |
| `GET` | `/salud` | Estado de las dependencias. Para el *readiness probe*. |
| `GET` | `/` | Resumen del servicio y lista de módulos. |

Los tres están **fuera** de `/api/v1` a propósito: no son parte de la API de
negocio. Un orquestador tiene que poder comprobarlos sin conocer la versión de
la API.

### Autenticación — `/api/v1/auth`

| Método | Ruta | Que hace |
|---|---|---|
| `POST` | `/registro` | Crea una cuenta de solicitante. |
| `POST` | `/iniciar-sesion` | Devuelve el par de tokens. |
| `POST` | `/usuarios` | Alta de agente o administrador (solo `ADMINISTRADOR`). |
| `POST` | `/refrescar` | Cambia un token de refresco por uno de acceso nuevo. |
| `POST` | `/cerrar-sesion` | Invalida el token de refresco actual. |
| `POST` | `/cerrar-todas-las-sesiones` | Invalida todos los del usuario. |
| `GET` | `/perfil` | Datos del usuario autenticado. |

`/registro` **no acepta** el campo `rol`. Se crea siempre `SOLICITANTE`: si el
cliente pudiera elegirlo, cualquiera se haría administrador enviando un JSON
distinto. Promover a `AGENTE` es una acción de administrador por
`PATCH /api/v1/usuarios/{id}`.

### Usuarios — `/api/v1/usuarios`

| Método | Ruta | Que hace |
|---|---|---|
| `GET` | `/` | Lista paginada, con filtros. |
| `GET` | `/agentes` | Solo el personal de soporte (para el desplegable de asignación). |
| `GET` | `/{id}` | Detalle. |
| `PATCH` | `/{id}` | Actualiza campos sueltos. |
| `PATCH` | `/contrasena` | Cambia la contraseña propia. |
| `DELETE` | `/{id}` | Baja lógica. |

`/agentes` está declarado **antes** que `/{id}`. Si fuera al revés, Spring
intentaría convertir la cadena `"agentes"` en un UUID y devolvería 400. Es un
error que solo aparece en tiempo de ejecución.

La baja es **lógica** (`eliminadoEn`): borrar un usuario rompería la trazabilidad
de los tickets que atendio.

### Categorías — `/api/v1/categorias`

| Método | Ruta | Que hace |
|---|---|---|
| `GET` | `/` | Lista. |
| `GET` | `/{id}` | Detalle. |
| `POST` | `/` | Crea. |
| `PATCH` | `/{id}` | Actualiza. |
| `DELETE` | `/{id}` | Elimina, **solo si no tiene tickets**. |

El `DELETE` devuelve en `meta.ticketsAfectados` cuantos tickets quedarian sin
categoría. Si hay alguno, no borra: los tickets existentes apuntarian a una
categoría inexistente.

### Tickets — `/api/v1/tickets`

| Método | Ruta | Que hace |
|---|---|---|
| `GET` | `/` | Lista paginada, filtrada por rol. |
| `GET` | `/estadisticas` | Contadores agregados. |
| `GET` | `/{id}` | Detalle. |
| `GET` | `/{id}/historial` | Cambios de estado. |
| `POST` | `/` | Crea. |
| `PATCH` | `/{id}` | Actualiza campos sueltos. |
| `PATCH` | `/{id}/estado` | Cambia de estado. Valida la transición. |
| `PATCH` | `/{id}/agente` | Asigna o desasigna agente. |
| `DELETE` | `/{id}` | Cancela. |

`/estadisticas` también va antes que `/{id}`, por el mismo motivo que
`/agentes`.

`PATCH /{id}/agente` acepta `agenteId: null` para **desasignar**. Es un caso
que se pierde si se trata `null` como "campo omitido": hay que distinguir
"no lo toques" de "quitale el agente".

### Comentarios — `/api/v1/tickets/{ticketId}/comentarios`

| Método | Ruta | Que hace |
|---|---|---|
| `GET` | `/` | Conversacion, sin las notas internas si el rol es solicitante. |
| `POST` | `/` | Publica. Solo el personal puede marcar `esInterno`. |
| `DELETE` | `/{comentarioId}` | Solo el autor o un administrador. |

La ruta es **anidada** y no `/comentarios`: un comentario no existe sin su
ticket, y la ruta lo refleja. Además impide pedir un comentario sin decir de
que ticket es.

### Adjuntos — `/api/v1/tickets/{ticketId}/adjuntos`

| Método | Ruta | Que hace |
|---|---|---|
| `GET` | `/` | Lista los adjuntos del ticket. |
| `POST` | `/` | Sube una evidencia (`multipart/form-data`). |
| `GET` | `/{adjuntoId}/descargar` | Descarga con `Content-Disposition: attachment`. |
| `DELETE` | `/{adjuntoId}` | Borra. |

### Reportes — `/api/v1/reportes`

| Método | Ruta | Que hace |
|---|---|---|
| `GET` | `/dashboard` | Panel con los indicadores principales. |
| `GET` | `/tickets` | Genera el reporte. `?dias=30&formato=pdf\|xlsx` |
| `POST` | `/limpiar` | Borra los reportes temporales antiguos. |

**La ruta correcta es `/api/v1/reportes/tickets?formato=pdf`.** No existen
`/reportes/pdf` ni `/reportes/xlsx`: el formato es un parámetro, no un
recurso. Un reporte no es una entidad distinta por estar en PDF.

### Notificaciones — `/api/v1/notificaciones`

| Método | Ruta | Que hace |
|---|---|---|
| `GET` | `/ticket/{ticketId}` | Historial de envios de un ticket. |
| `GET` | `/resumen` | Contadores por estado y canal. |
| `POST` | `/reintentar` | Reencola las fallidas. |

---

## Decisiones de diseño

Estas son las decisiones que un revisor va a preguntar. Cada una tiene una
razón concreta.

### 1. La máquina de estados vive en el enum, no en el servicio

`EstadoTicket` es dueño de su propia tabla de transiciones:

```java
private static final Map<EstadoTicket, Set<EstadoTicket>> TRANSICIONES;
static {
    Map<EstadoTicket, Set<EstadoTicket>> mapa = new EnumMap<>(EstadoTicket.class);
    mapa.put(NUEVO,       EnumSet.of(ABIERTO, EN_PROGRESO, CANCELADO));
    mapa.put(ABIERTO,     EnumSet.of(EN_PROGRESO, EN_ESPERA, RESUELTO, CANCELADO));
    mapa.put(EN_PROGRESO, EnumSet.of(EN_ESPERA, RESUELTO, ABIERTO, CANCELADO));
    mapa.put(EN_ESPERA,   EnumSet.of(EN_PROGRESO, RESUELTO, CANCELADO));
    mapa.put(RESUELTO,    EnumSet.of(CERRADO, EN_PROGRESO));  // reapertura
    mapa.put(CERRADO,     EnumSet.noneOf(EstadoTicket.class));
    mapa.put(CANCELADO,   EnumSet.noneOf(EstadoTicket.class));
    TRANSICIONES = Collections.unmodifiableMap(mapa);
}
```

**Por que no en el servicio:** si la tabla estuviera en `TicketServicio`, cada
sitio que necesitara validar una transición tendría que pasar por el servicio, y
un cambio en las reglas obligaría a tocar código de orquestación. Poniendola en
el enum, la regla viaja con el concepto y se puede probar sola: `EstadoTicket`
no depende de Spring, ni de la base de datos, ni de nada.

**Por que `unmodifiableMap`:** el mapa se construye una vez. Si alguien pudiera
modificarlo en tiempo de ejecución, las reglas del negocio dejarian de ser
deterministas.

**Detalle de la reapertura:** un ticket `RESUELTO` puede volver a `EN_PROGRESO`.
Un solicitante que no queda conforme no debería tener que abrir un ticket nuevo
y perder el historial. Es una decisión de negocio, no un descuido: por eso
`CERRADO` y `CANCELADO` si son terminales.

### 2. Las excepciones saben su propio código HTTP

`ExcepcionAplicacion` es abstracta y guarda un `HttpStatus` y un `codigo` de
negocio. Cada subclase nace sabiendo que le corresponde:

| Excepción | HTTP | Cuando |
|---|---|---|
| `NoEncontradoExcepcion` | 404 | El recurso no existe. |
| `SolicitudInvalidaExcepcion` | 400 | Los datos no cumplen una regla. |
| `SinPermisoExcepcion` | 403 | Autenticado, pero sin derechos. |
| `ConflictoEstadoExcepcion` | 409 | La transición no aplica al estado actual. |
| `RecursoDuplicadoExcepcion` | 409 | Ya existe algo único (correo, código). |

Esto hace que el manejador global sea **abierto/cerrado**: un solo método

```java
@ExceptionHandler(ExcepcionAplicacion.class)
public ResponseEntity<RespuestaApi<Void>> manejar(ExcepcionAplicacion ex) { ... }
```

cubre las cinco, sin una cadena de `if`. Añadir un sexto tipo de error no
obliga a tocar el manejador: solo a crear la clase.

### 3. Por qué 409 y no 400 al cambiar de estado

`PATCH /tickets/{id}/estado` con `{"estado": "CERRADO"}` sobre un ticket
`NUEVO` devuelve **409 Conflict**, no 400.

La petición esta **bien formada**: `CERRADO` es un estado válido del
enumerado. Lo que no encaja es el **estado actual del recurso**, que no admite
esa transición. Un 400 diría "tu petición está mal", y la petición está bien; el
problema es del recurso. El 409 además le dice al cliente que puede reintentar
cuando el ticket avance, cosa que un 400 no sugiere.

### 4. DTOs sin los campos sensibles, por construcción

| Campo | Por que no esta |
|---|---|
| `contrasenaHash` | No está en `UsuarioDto`. Lo que no existe en la clase no se puede filtrar. |
| `rutaRelativa` | La ruta física del adjunto en disco no le sirve al cliente; solo le da un mapa del servidor. |
| `eliminadoEn` | Detalle interno de la baja lógica. |
| `rol` en `RegistroDto` | Evita la escalada de privilegios (ver arriba). |

**La única excepción deliberada** es `AutenticacionRepositorio`, el único
punto que devuelve el hash: hace falta para comparar en el inicio de sesión.
Está documentado en el propio archivo para que nadie lo "arregle".

### 5. El orden de las comprobaciones importa

En `TicketServicio.obtener` primero se comprueba la **existencia** y después el
**permiso**. Si fuera al revés, un usuario sin acceso recibiría 403 para un
ticket inexistente y 403 para uno ajeno: la diferencia le diría cuales existen.

Solo quien tiene derecho a ver el recurso llega a la comprobación de permisos.

### 6. El aislamiento por rol **sobrescribe**, nunca suma

```java
private void aplicarAislamientoPorRol(FiltrosTicketDto filtros, UUID usuarioId, RolUsuario rol) {
    switch (rol) {
        case SOLICITANTE -> { filtros.setSolicitanteId(usuarioId); filtros.setAgenteId(null); }
        case AGENTE      -> { filtros.setSolicitanteId(null); }
        case ADMINISTRADOR -> { }
    }
}
```

Un solicitante pregunta `?solicitanteId=<el de otro>`. Si el filtro del cliente
se **sumara** al del rol, se aplicarian los dos y ganaría el del cliente. Al
**sobrescribir**, el valor del cliente se descarta y siempre ve lo suyo. El
parámetro deja de ser una vía de fuga.

### 7. Las notificaciones son una cola, no una llamada síncrona

Al resolver un ticket, la petición confirma el cambio y **encola** la
notificación. El envío pasa después.

Si se enviara en la misma petición, un servidor de correo lento o caído
bloquearia al agente: el ticket no se resolveria hasta que el proveedor
respondiera. Con la cola, el cambio de estado está hecho y el correo sale
cuando el proveedor pueda.

**Un fallo de envío no es una excepción, es un resultado.** Por eso
`ResultadoEnvio` tiene factorias `ok(...)` y `fallo(...)`: si un correo
rebotado lanzara una excepción, pareceria que el ticket no se resolvio.

### 8. Inversión de dependencias en el proveedor de notificaciones

`ProveedorNotificacion` es una interfaz; el servicio depende de ella, no de
SendGrid ni de Twilio. En desarrollo se inyecta una implementación simulada:
**no se gasta credito real en pruebas** y se puede verificar el flujo completo
sin red. Cambiar de proveedor es añadir una clase, no tocar el servicio.

### 9. Las transiciones se validan antes de pedir la solución

En `cambiarEstado`, el orden es: permiso → transición valida → exigir
`solucion` si el destino es `RESUELTO`. Si se pidiera la solución primero, un
intento de transición imposible devolvería "falta la solución" en lugar de "esa
transición no es valida", que es el error real.

### 10. El SLA se calcula contra la fecha de resolución

```java
public boolean calcularSlaVencido(Instant fechaLimite, Instant fechaResolucion) {
    if (fechaLimite == null) return false;
    Instant referencia = (fechaResolucion != null) ? fechaResolucion : Instant.now();
    return referencia.isAfter(fechaLimite);
}
```

Un ticket resuelto **a tiempo** pero consultado hoy no puede aparecer como
vencido. Si se comparara siempre contra "ahora", todo lo antiguo saldria en
rojo. La referencia es la fecha en que se resolvio; solo si sigue abierto se
usa el momento actual.

---

## Enumeraciones del dominio

Son la copia exacta de los tipos `ENUM` de PostgreSQL en
`database/init/01-schema.sql`. Están en Java, no como `String`, para que
**el compilador detecte los valores invalidos** en lugar de descubrirlos en
producción.

| Enum | Valores | Extra |
|---|---|---|
| `RolUsuario` | `SOLICITANTE`, `AGENTE`, `ADMINISTRADOR` | `esPersonalDeSoporte()` |
| `EstadoTicket` | `NUEVO`, `ABIERTO`, `EN_PROGRESO`, `EN_ESPERA`, `RESUELTO`, `CERRADO`, `CANCELADO` | mapa de transiciones, `puedeTransicionarA()`, `esFinal()`, `estaPendiente()` |
| `PrioridadTicket` | `BAJA`, `MEDIA`, `ALTA`, `CRITICA` | `peso()` para ordenar |
| `CanalNotificacion` | `EMAIL`, `SMS`, `AMBOS` | — |
| `EstadoNotificacion` | `PENDIENTE`, `ENVIADA`, `FALLIDA` | — |

`esPersonalDeSoporte()` evita repetir `rol == AGENTE || rol == ADMINISTRADOR`
en cada sitio; si mañana se añade un rol de soporte, se cambia en un lugar.

---

## Manejo de errores

`ManejadorGlobalDeErrores` centraliza **todas** las excepciones. Sin el, cada
controlador tendría su propio `try/catch` y el formato de error dependeria de
quien escribio cada endpoint.

| Excepción capturada | HTTP | Que significa |
|---|---|---|
| `ExcepcionAplicacion` | según la subclase | Errores de negocio. |
| `MethodArgumentNotValidException` | 400 | Un DTO no pasa la validación. |
| `HttpMessageNotReadableException` | 400 | El JSON está mal formado. |
| `MethodArgumentTypeMismatchException` | 400 | Un parámetro tiene el tipo equivocado (p. ej. un UUID inválido). |
| `Exception` | 500 | Cualquier otra cosa. |

**El 500 nunca devuelve el mensaje interno.** Un `NullPointerException` real
suele traer nombres de clases, tablas y a veces fragmentos de datos. El
manejador devuelve un texto neutro y el detalle va al registro del servidor,
que es donde debe estar.

Se refuerza desde `application.properties`:

```properties
server.error.include-message=never
server.error.include-stacktrace=never
```

---

## Formato de respuesta

**Todas** las respuestas usan el mismo envoltorio (`RespuestaApi<T>`). Un
cliente que sabe leer un error sabe leer todos, y no hay que adivinar la forma
de la respuesta por endpoint.

**Correcta**

```json
{
  "exito": true,
  "datos": { "...": "..." },
  "mensaje": "Ticket creado correctamente.",
  "meta": null,
  "errores": null,
  "marcaTiempo": "2026-10-09T17:38:00Z"
}
```

**Paginada** — el detalle va en `meta`, no mezclado con los datos:

```json
{
  "exito": true,
  "datos": [ "..." ],
  "meta": {
    "paginacion": {
      "pagina": 1, "limite": 20, "total": 47,
      "totalPaginas": 3, "tieneSiguiente": true, "tieneAnterior": false
    }
  },
  "marcaTiempo": "2026-10-09T17:38:00Z"
}
```

**Con error**

```json
{
  "exito": false,
  "datos": null,
  "mensaje": "No se puede pasar de NUEVO a CERRADO.",
  "errores": [
    { "codigo": "CONFLICTO_ESTADO", "campo": "estado", "mensaje": "Transición no permitida." }
  ],
  "marcaTiempo": "2026-10-09T17:38:00Z"
}
```

**Los campos nulos se omiten** (`@JsonInclude(NON_NULL)`): no se serializa
`"errores": null`, así que la respuesta de éxito es más corta y no lleva claves
vacias que el cliente tenga que ignorar.

### Paginación

`PaginacionDto` acepta `pagina` y `limite`, con topes:

```java
public static final int LIMITE_POR_DEFECTO = 20;
public static final int LIMITE_MAXIMO = 100;
```

**El tope de 100 es una defensa, no una preferencia.** Sin el, un cliente pide
`?limite=10000000` y el servidor intenta montar la lista en memoria: es una
denegación de servicio con una URL. Cualquier página fuera de rango se
normaliza en lugar de rechazarse: un cliente que pide la página 0 recibe la 1,
no un error.

---

## Qué falta para que sea funcional

Los cuatro pasos, en orden, están en
[`docs/COMO-EJECUTAR.md`](docs/COMO-EJECUTAR.md), sección 9. En resumen:

1. **`pom.xml`** — descomentar el bloque `3. PENDIENTES PARA LA VERSIÓN
   FUNCIONAL` (`data-jpa`, `security`, `postgresql`, `jjwt-api`).
2. **`application.properties`** — descomentar el datasource; las credenciales
   van por variables de entorno (hay plantilla en `.env.example`).
3. **`repository/`** — implementar cada interfaz (JPA o `JdbcTemplate`).
4. **`service/`** — sustituir los `UnsupportedOperationException` por las
   llamadas al repositorio.

El esquema SQL ya existe en el proyecto original
(`database/init/01-schema.sql`) y es la referencia para el paso 3.

**También pendiente:**

- `config/ConfiguracionSeguridad.java` + filtro JWT. Los controladores hoy
  reciben `rol` y `usuarioId` como valores fijos, con un comentario que marca
  donde debe leerlos del token.
- `config/ConfiguracionOpenApi.java` para la documentación Swagger.
- Pruebas: `src/test/` existe pero está vacío.

---

## Verificación

| Comprobación | Resultado |
|---|---|
| `mvn clean compile` (JDK 23, Maven 3.9) | **BUILD SUCCESS** |
| Archivos Java | 62 |
| Líneas de código | ~4.980 |
| Módulos funcionales | 8 |
| Endpoints REST | 43 |

**Nota sobre el JDK.** El `pom.xml` declara `java.version` **17** mientras que
la compilación se hizo con el JDK **23** disponible en el equipo. Eso valida
que el código sea compatible hacia adelante, pero **no ejercita el objetivo 17
exacto**. Para reproducir la comprobación con la versión objetivo hay que usar
un JDK 17; el comando está en `docs/COMO-EJECUTAR.md`, sección 10.

**Nota sobre el entorno.** En la máquina de desarrollo hay **Java 8**
instalado (`1.8.0_431`), que no sirve para Spring Boot 3, y `mvn` / `gradle` no
están en el `PATH`. La compilación se hizo con un JDK 23 ubicado en
`C:/Users/5700X/.jdks/openjdk-23.0.1/` y con el Maven integrado de IntelliJ, en
`C:/Program Files/JetBrains/IntelliJ IDEA 2025.2.3/plugins/maven/lib/maven3/`.
