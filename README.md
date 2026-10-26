# Sistema de Tickets — Backend en Spring Boot

Version **Spring Boot (Java 17)** del backend del Sistema de Tickets (helpdesk).
Es el mismo dominio del proyecto original en Node.js + Express, reescrito con
la arquitectura por capas que se espera de una aplicacion Spring.

> **Estado de la entrega:** este repositorio contiene la **estructura
> completa** — DTOs con validacion, servicios con las reglas de negocio y
> controladores REST — pero **sin acceso a datos**. Los repositorios son
> interfaces sin implementacion. La aplicacion arranca y responde en los
> endpoints de diagnostico; las rutas de negocio fallan de forma explicita con
> `UnsupportedOperationException` hasta que se conecte la persistencia. Ver
> [`docs/COMO-EJECUTAR.md`](docs/COMO-EJECUTAR.md), seccion 9.

---

## Contenido

- [Que es](#que-es)
- [Arrancar en 3 pasos](#arrancar-en-3-pasos)
- [Arquitectura](#arquitectura)
- [Estructura de paquetes](#estructura-de-paquetes)
- [Endpoints](#endpoints)
- [Decisiones de diseno](#decisiones-de-diseno)
- [Enumeraciones del dominio](#enumeraciones-del-dominio)
- [Manejo de errores](#manejo-de-errores)
- [Formato de respuesta](#formato-de-respuesta)
- [Que falta para que sea funcional](#que-falta-para-que-sea-funcional)
- [Verificacion](#verificacion)

---

## Que es

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

**8 modulos funcionales:** autenticacion, usuarios, categorias, tickets,
comentarios, adjuntos, reportes y notificaciones.

**3 roles:** `SOLICITANTE` (abre y sigue sus tickets), `AGENTE` (atiende y
resuelve) y `ADMINISTRADOR` (todo, mas gestion de usuarios y categorias).

---

## Arrancar en 3 pasos

### 1. Tener un JDK 17 o superior

```bat
java -version
```

Si responde `1.8.0_431`, es Java 8 y **no sirve**: Spring Boot 3.x exige Java
17 como minimo. Descargar un JDK 17 de
[Adoptium](https://adoptium.net/temurin/releases/?version=17) o de Oracle.

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

Tambien hay lanzadores para no escribir las ordenes largas:

| Script | Que hace |
|---|---|
| `ejecutar.bat` | Arranca la aplicacion. |
| `compilar.bat` | Empaqueta el fat jar en `target\`. |
| `verificar.bat` | Compila y consulta los endpoints de diagnostico. |

**Guia detallada, problemas frecuentes y notas sobre el JDK:
[`docs/COMO-EJECUTAR.md`](docs/COMO-EJECUTAR.md).**

---

## Arquitectura

Cuatro capas por modulo, con dependencias en **un solo sentido**:

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
base de datos. Un DTO es una decision explicita sobre que se expone. Si el DTO
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
├── comun/                              Piezas compartidas por todos los modulos
│   ├── RespuestaApi.java               Envoltorio unico de todas las respuestas
│   ├── DetalleError.java               Estructura de un error
│   ├── PaginacionDto.java              Base de los filtros paginados
│   ├── ExcepcionAplicacion.java        Base: sabe su propio codigo HTTP
│   ├── NoEncontradoExcepcion.java      404
│   ├── SolicitudInvalidaExcepcion.java 400
│   ├── SinPermisoExcepcion.java        403
│   ├── ConflictoEstadoExcepcion.java   409 (transicion de estado ilegal)
│   └── RecursoDuplicadoExcepcion.java  409 (unicidad)
│
├── dominio/                            Enumeraciones = tipos ENUM de PostgreSQL
│   ├── RolUsuario.java                 SOLICITANTE | AGENTE | ADMINISTRADOR
│   ├── EstadoTicket.java               La maquina de estados vive aqui
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

**62 archivos Java · ~4.980 lineas.** Todos los modulos repiten la misma
distribucion, asi que saber donde esta algo en `tickets` es saber donde esta en
`adjuntos`.

---

## Endpoints

**Base:** `/api/v1`

### Diagnostico

| Metodo | Ruta | Que hace |
|---|---|---|
| `GET` | `/vivo` | El proceso existe. Para el *liveness probe*. |
| `GET` | `/salud` | Estado de las dependencias. Para el *readiness probe*. |
| `GET` | `/` | Resumen del servicio y lista de modulos. |

Los tres estan **fuera** de `/api/v1` a proposito: no son parte de la API de
negocio. Un orquestador tiene que poder comprobarlos sin conocer la version de
la API.

### Autenticacion — `/api/v1/auth`

| Metodo | Ruta | Que hace |
|---|---|---|
| `POST` | `/registro` | Crea una cuenta de solicitante. |
| `POST` | `/iniciar-sesion` | Devuelve el par de tokens. |
| `POST` | `/usuarios` | Alta de agente o administrador (solo `ADMINISTRADOR`). |
| `POST` | `/refrescar` | Cambia un token de refresco por uno de acceso nuevo. |
| `POST` | `/cerrar-sesion` | Invalida el token de refresco actual. |
| `POST` | `/cerrar-todas-las-sesiones` | Invalida todos los del usuario. |
| `GET` | `/perfil` | Datos del usuario autenticado. |

`/registro` **no acepta** el campo `rol`. Se crea siempre `SOLICITANTE`: si el
cliente pudiera elegirlo, cualquiera se haria administrador enviando un JSON
distinto. Promover a `AGENTE` es una accion de administrador por
`PATCH /api/v1/usuarios/{id}`.

### Usuarios — `/api/v1/usuarios`

| Metodo | Ruta | Que hace |
|---|---|---|
| `GET` | `/` | Lista paginada, con filtros. |
| `GET` | `/agentes` | Solo el personal de soporte (para el desplegable de asignacion). |
| `GET` | `/{id}` | Detalle. |
| `PATCH` | `/{id}` | Actualiza campos sueltos. |
| `PATCH` | `/contrasena` | Cambia la contrasena propia. |
| `DELETE` | `/{id}` | Baja logica. |

`/agentes` esta declarado **antes** que `/{id}`. Si fuera al reves, Spring
intentaria convertir la cadena `"agentes"` en un UUID y devolveria 400. Es un
error que solo aparece en tiempo de ejecucion.

La baja es **logica** (`eliminadoEn`): borrar un usuario romperia la trazabilidad
de los tickets que atendio.

### Categorias — `/api/v1/categorias`

| Metodo | Ruta | Que hace |
|---|---|---|
| `GET` | `/` | Lista. |
| `GET` | `/{id}` | Detalle. |
| `POST` | `/` | Crea. |
| `PATCH` | `/{id}` | Actualiza. |
| `DELETE` | `/{id}` | Elimina, **solo si no tiene tickets**. |

El `DELETE` devuelve en `meta.ticketsAfectados` cuantos tickets quedarian sin
categoria. Si hay alguno, no borra: los tickets existentes apuntarian a una
categoria inexistente.

### Tickets — `/api/v1/tickets`

| Metodo | Ruta | Que hace |
|---|---|---|
| `GET` | `/` | Lista paginada, filtrada por rol. |
| `GET` | `/estadisticas` | Contadores agregados. |
| `GET` | `/{id}` | Detalle. |
| `GET` | `/{id}/historial` | Cambios de estado. |
| `POST` | `/` | Crea. |
| `PATCH` | `/{id}` | Actualiza campos sueltos. |
| `PATCH` | `/{id}/estado` | Cambia de estado. Valida la transicion. |
| `PATCH` | `/{id}/agente` | Asigna o desasigna agente. |
| `DELETE` | `/{id}` | Cancela. |

`/estadisticas` tambien va antes que `/{id}`, por el mismo motivo que
`/agentes`.

`PATCH /{id}/agente` acepta `agenteId: null` para **desasignar**. Es un caso
que se pierde si se trata `null` como "campo omitido": hay que distinguir
"no lo toques" de "quitale el agente".

### Comentarios — `/api/v1/tickets/{ticketId}/comentarios`

| Metodo | Ruta | Que hace |
|---|---|---|
| `GET` | `/` | Conversacion, sin las notas internas si el rol es solicitante. |
| `POST` | `/` | Publica. Solo el personal puede marcar `esInterno`. |
| `DELETE` | `/{comentarioId}` | Solo el autor o un administrador. |

La ruta es **anidada** y no `/comentarios`: un comentario no existe sin su
ticket, y la ruta lo refleja. Ademas impide pedir un comentario sin decir de
que ticket es.

### Adjuntos — `/api/v1/tickets/{ticketId}/adjuntos`

| Metodo | Ruta | Que hace |
|---|---|---|
| `GET` | `/` | Lista los adjuntos del ticket. |
| `POST` | `/` | Sube una evidencia (`multipart/form-data`). |
| `GET` | `/{adjuntoId}/descargar` | Descarga con `Content-Disposition: attachment`. |
| `DELETE` | `/{adjuntoId}` | Borra. |

### Reportes — `/api/v1/reportes`

| Metodo | Ruta | Que hace |
|---|---|---|
| `GET` | `/dashboard` | Panel con los indicadores principales. |
| `GET` | `/tickets` | Genera el reporte. `?dias=30&formato=pdf\|xlsx` |
| `POST` | `/limpiar` | Borra los reportes temporales antiguos. |

**La ruta correcta es `/api/v1/reportes/tickets?formato=pdf`.** No existen
`/reportes/pdf` ni `/reportes/xlsx`: el formato es un parametro, no un
recurso. Un reporte no es una entidad distinta por estar en PDF.

### Notificaciones — `/api/v1/notificaciones`

| Metodo | Ruta | Que hace |
|---|---|---|
| `GET` | `/ticket/{ticketId}` | Historial de envios de un ticket. |
| `GET` | `/resumen` | Contadores por estado y canal. |
| `POST` | `/reintentar` | Reencola las fallidas. |

---

## Decisiones de diseno

Estas son las decisiones que un revisor va a preguntar. Cada una tiene una
razon concreta.

### 1. La maquina de estados vive en el enum, no en el servicio

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
sitio que necesitara validar una transicion tendria que pasar por el servicio, y
un cambio en las reglas obligaria a tocar codigo de orquestacion. Poniendola en
el enum, la regla viaja con el concepto y se puede probar sola: `EstadoTicket`
no depende de Spring, ni de la base de datos, ni de nada.

**Por que `unmodifiableMap`:** el mapa se construye una vez. Si alguien pudiera
modificarlo en tiempo de ejecucion, las reglas del negocio dejarian de ser
deterministas.

**Detalle de la reapertura:** un ticket `RESUELTO` puede volver a `EN_PROGRESO`.
Un solicitante que no queda conforme no deberia tener que abrir un ticket nuevo
y perder el historial. Es una decision de negocio, no un descuido: por eso
`CERRADO` y `CANCELADO` si son terminales.

### 2. Las excepciones saben su propio codigo HTTP

`ExcepcionAplicacion` es abstracta y guarda un `HttpStatus` y un `codigo` de
negocio. Cada subclase nace sabiendo que le corresponde:

| Excepcion | HTTP | Cuando |
|---|---|---|
| `NoEncontradoExcepcion` | 404 | El recurso no existe. |
| `SolicitudInvalidaExcepcion` | 400 | Los datos no cumplen una regla. |
| `SinPermisoExcepcion` | 403 | Autenticado, pero sin derechos. |
| `ConflictoEstadoExcepcion` | 409 | La transicion no aplica al estado actual. |
| `RecursoDuplicadoExcepcion` | 409 | Ya existe algo unico (correo, codigo). |

Esto hace que el manejador global sea **abierto/cerrado**: un solo metodo

```java
@ExceptionHandler(ExcepcionAplicacion.class)
public ResponseEntity<RespuestaApi<Void>> manejar(ExcepcionAplicacion ex) { ... }
```

cubre las cinco, sin una cadena de `if`. Anadir un sexto tipo de error no
obliga a tocar el manejador: solo a crear la clase.

### 3. Por que 409 y no 400 al cambiar de estado

`PATCH /tickets/{id}/estado` con `{"estado": "CERRADO"}` sobre un ticket
`NUEVO` devuelve **409 Conflict**, no 400.

La peticion esta **bien formada**: `CERRADO` es un estado valido del
enumerado. Lo que no encaja es el **estado actual del recurso**, que no admite
esa transicion. Un 400 diria "tu peticion esta mal", y la peticion esta bien; el
problema es del recurso. El 409 ademas le dice al cliente que puede reintentar
cuando el ticket avance, cosa que un 400 no sugiere.

### 4. DTOs sin los campos sensibles, por construccion

| Campo | Por que no esta |
|---|---|
| `contrasenaHash` | No esta en `UsuarioDto`. Lo que no existe en la clase no se puede filtrar. |
| `rutaRelativa` | La ruta fisica del adjunto en disco no le sirve al cliente; solo le da un mapa del servidor. |
| `eliminadoEn` | Detalle interno de la baja logica. |
| `rol` en `RegistroDto` | Evita la escalada de privilegios (ver arriba). |

**La unica excepcion deliberada** es `AutenticacionRepositorio`, el unico
punto que devuelve el hash: hace falta para comparar en el inicio de sesion.
Esta documentado en el propio archivo para que nadie lo "arregle".

### 5. El orden de las comprobaciones importa

En `TicketServicio.obtener` primero se comprueba la **existencia** y despues el
**permiso**. Si fuera al reves, un usuario sin acceso recibiria 403 para un
ticket inexistente y 403 para uno ajeno: la diferencia le diria cuales existen.

Solo quien tiene derecho a ver el recurso llega a la comprobacion de permisos.

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
se **sumara** al del rol, se aplicarian los dos y ganaria el del cliente. Al
**sobrescribir**, el valor del cliente se descarta y siempre ve lo suyo. El
parametro deja de ser una via de fuga.

### 7. Las notificaciones son una cola, no una llamada sincrona

Al resolver un ticket, la peticion confirma el cambio y **encola** la
notificacion. El envio pasa despues.

Si se enviara en la misma peticion, un servidor de correo lento o caido
bloquearia al agente: el ticket no se resolveria hasta que el proveedor
respondiera. Con la cola, el cambio de estado esta hecho y el correo sale
cuando el proveedor pueda.

**Un fallo de envio no es una excepcion, es un resultado.** Por eso
`ResultadoEnvio` tiene factorias `ok(...)` y `fallo(...)`: si un correo
rebotado lanzara una excepcion, pareceria que el ticket no se resolvio.

### 8. Inversion de dependencias en el proveedor de notificaciones

`ProveedorNotificacion` es una interfaz; el servicio depende de ella, no de
SendGrid ni de Twilio. En desarrollo se inyecta una implementacion simulada:
**no se gasta credito real en pruebas** y se puede verificar el flujo completo
sin red. Cambiar de proveedor es anadir una clase, no tocar el servicio.

### 9. Las transiciones se validan antes de pedir la solucion

En `cambiarEstado`, el orden es: permiso → transicion valida → exigir
`solucion` si el destino es `RESUELTO`. Si se pidiera la solucion primero, un
intento de transicion imposible devolveria "falta la solucion" en lugar de "esa
transicion no es valida", que es el error real.

### 10. El SLA se calcula contra la fecha de resolucion

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
`database/init/01-schema.sql`. Estan en Java, no como `String`, para que
**el compilador detecte los valores invalidos** en lugar de descubrirlos en
produccion.

| Enum | Valores | Extra |
|---|---|---|
| `RolUsuario` | `SOLICITANTE`, `AGENTE`, `ADMINISTRADOR` | `esPersonalDeSoporte()` |
| `EstadoTicket` | `NUEVO`, `ABIERTO`, `EN_PROGRESO`, `EN_ESPERA`, `RESUELTO`, `CERRADO`, `CANCELADO` | mapa de transiciones, `puedeTransicionarA()`, `esFinal()`, `estaPendiente()` |
| `PrioridadTicket` | `BAJA`, `MEDIA`, `ALTA`, `CRITICA` | `peso()` para ordenar |
| `CanalNotificacion` | `EMAIL`, `SMS`, `AMBOS` | — |
| `EstadoNotificacion` | `PENDIENTE`, `ENVIADA`, `FALLIDA` | — |

`esPersonalDeSoporte()` evita repetir `rol == AGENTE || rol == ADMINISTRADOR`
en cada sitio; si manana se anade un rol de soporte, se cambia en un lugar.

---

## Manejo de errores

`ManejadorGlobalDeErrores` centraliza **todas** las excepciones. Sin el, cada
controlador tendria su propio `try/catch` y el formato de error dependeria de
quien escribio cada endpoint.

| Excepcion capturada | HTTP | Que significa |
|---|---|---|
| `ExcepcionAplicacion` | segun la subclase | Errores de negocio. |
| `MethodArgumentNotValidException` | 400 | Un DTO no pasa la validacion. |
| `HttpMessageNotReadableException` | 400 | El JSON esta mal formado. |
| `MethodArgumentTypeMismatchException` | 400 | Un parametro tiene el tipo equivocado (p. ej. un UUID invalido). |
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
    { "codigo": "CONFLICTO_ESTADO", "campo": "estado", "mensaje": "Transicion no permitida." }
  ],
  "marcaTiempo": "2026-10-09T17:38:00Z"
}
```

**Los campos nulos se omiten** (`@JsonInclude(NON_NULL)`): no se serializa
`"errores": null`, asi que la respuesta de exito es mas corta y no lleva claves
vacias que el cliente tenga que ignorar.

### Paginacion

`PaginacionDto` acepta `pagina` y `limite`, con topes:

```java
public static final int LIMITE_POR_DEFECTO = 20;
public static final int LIMITE_MAXIMO = 100;
```

**El tope de 100 es una defensa, no una preferencia.** Sin el, un cliente pide
`?limite=10000000` y el servidor intenta montar la lista en memoria: es una
denegacion de servicio con una URL. Cualquier pagina fuera de rango se
normaliza en lugar de rechazarse: un cliente que pide la pagina 0 recibe la 1,
no un error.

---

## Que falta para que sea funcional

Los cuatro pasos, en orden, estan en
[`docs/COMO-EJECUTAR.md`](docs/COMO-EJECUTAR.md), seccion 9. En resumen:

1. **`pom.xml`** — descomentar el bloque `3. PENDIENTES PARA LA VERSION
   FUNCIONAL` (`data-jpa`, `security`, `postgresql`, `jjwt-api`).
2. **`application.properties`** — descomentar el datasource; las credenciales
   van por variables de entorno (hay plantilla en `.env.example`).
3. **`repository/`** — implementar cada interfaz (JPA o `JdbcTemplate`).
4. **`service/`** — sustituir los `UnsupportedOperationException` por las
   llamadas al repositorio.

El esquema SQL ya existe en el proyecto original
(`database/init/01-schema.sql`) y es la referencia para el paso 3.

**Tambien pendiente:**

- `config/ConfiguracionSeguridad.java` + filtro JWT. Los controladores hoy
  reciben `rol` y `usuarioId` como valores fijos, con un comentario que marca
  donde debe leerlos del token.
- `config/ConfiguracionOpenApi.java` para la documentacion Swagger.
- Pruebas: `src/test/` existe pero esta vacio.

---

## Verificacion

| Comprobacion | Resultado |
|---|---|
| `mvn clean compile` (JDK 23, Maven 3.9) | **BUILD SUCCESS** |
| Archivos Java | 62 |
| Lineas de codigo | ~4.980 |
| Modulos funcionales | 8 |
| Endpoints REST | 43 |

**Nota sobre el JDK.** El `pom.xml` declara `java.version` **17** mientras que
la compilacion se hizo con el JDK **23** disponible en el equipo. Eso valida
que el codigo sea compatible hacia adelante, pero **no ejercita el objetivo 17
exacto**. Para reproducir la comprobacion con la version objetivo hay que usar
un JDK 17; el comando esta en `docs/COMO-EJECUTAR.md`, seccion 10.

**Nota sobre el entorno.** En la maquina de desarrollo hay **Java 8**
instalado (`1.8.0_431`), que no sirve para Spring Boot 3, y `mvn` / `gradle` no
estan en el `PATH`. La compilacion se hizo con un JDK 23 ubicado en
`C:/Users/5700X/.jdks/openjdk-23.0.1/` y con el Maven integrado de IntelliJ, en
`C:/Program Files/JetBrains/IntelliJ IDEA 2025.2.3/plugins/maven/lib/maven3/`.
