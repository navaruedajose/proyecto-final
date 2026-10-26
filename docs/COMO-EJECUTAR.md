# Como ejecutar el backend en Spring Boot

Guía para levantar este proyecto en Windows. Si algo falla, casi siempre es
por el JDK: **Spring Boot 3.x exige Java 17 como mínimo y en este equipo hay
Java 8 instalado**, que no sirve. Ese es el primer punto a revisar.

---

## 1. Requisitos

| Herramienta | Versión mínima | Para que se usa |
|---|---|---|
| **JDK** | **17** (probado con 23) | Compilar y ejecutar. Java 8 **no** funciona. |
| **Maven** | 3.6 | Compilar, descargar dependencias, empaquetar. |
| (opcional) IntelliJ IDEA | 2023+ | Trae un Maven integrado que sirve tal cual. |

Todo lo demas (Tomcat, Jackson, la validación) llega como dependencia de Maven
declarada en el `pom.xml`; no hay que instalar nada a mano.

---

## 2. Comprobar que el JDK sirve

```bat
java -version
```

Debe responder algo como `openjdk version "17.0.x"` o superior.
Si responde `1.8.0_431`, es Java 8 y **hay que instalar otro JDK** antes de
seguir. Descargas sugeridas:

- **Eclipse Temurin** — <https://adoptium.net/temurin/releases/?versión=17>
- **Oracle JDK** — <https://www.oracle.com/java/technologies/downloads/>

Al instalar, marcar la opción de configurar `JAVA_HOME` y el `PATH`.

### Si hay varios JDK instalados

Windows conserva la última versión del `PATH`, que puede ser la incorrecta.
Para forzar el JDK bueno **solo en la sesión actual**:

```bat
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.13.11-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"
java -version
```

Si el JDK está en otra ruta, ajustar la primera línea. Para que el cambio sea
permanente hay que editar las variables de entorno del sistema.

---

## 3. Comprobar Maven

```bat
mvn -v
```

Debe imprimir la versión de Maven **y** la de Java. **Verificar que la línea de
Java muestre 17 o superior**: si muestra 1.8, Maven está usando el JDK
equivocado y la compilación fallara.

### Si `mvn` no está en el PATH

IntelliJ trae un Maven integrado que funciona sin instalarlo. En un equipo con
IntelliJ IDEA 2025.2.3 la ruta es:

```
C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.3\plugins\maven\lib\maven3\bin\mvn.cmd
```

Se puede invocar con la ruta completa entre comillas:

```bat
"C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.3\plugins\maven\lib\maven3\bin\mvn.cmd" -v
```

---

## 4. Ejecutar el proyecto

Desde la raíz del proyecto (donde está el `pom.xml`):

```bat
mvn spring-boot:run
```

La primera vez Maven descarga Spring Boot y sus dependencias (~100 MB) y tarda
varios minutos. Las siguientes arranca en segundos.

Cuando el arranque termina, la consola muestra:

```
Tomcat started on port 4000 (http) with context path ''
Started SistemaTicketsApplication in 2.4 seconds
```

El servicio queda en **<http://localhost:4000>**. El puerto 4000 es deliberado:
es el mismo que usaba el backend original en Node.js, así que el frontend
sigue funcionando sin tocar su proxy de desarrollo.

Para detenerlo, `Ctrl + C` en la consola.

---

## 5. Verificar que responde

Los dos endpoints de `ControladorDeSistema` no necesitan autenticación y
sirven para comprobar que el proceso está vivo:

```bat
curl http://localhost:4000/vivo
curl http://localhost:4000/salud
```

- **`/vivo`** responde siempre que el proceso exista. Sirve para el *liveness
  probe* de un orquestador: si falla, hay que reiniciar el contenedor.
- **`/salud`** informa además el estado de las dependencias. Sirve para el
  *readiness probe*: si falla, el contenedor está vivo pero no puede atender
  peticiones todavía.

`/` devuelve un resumen del servicio con la lista de módulos.

---

## 6. Compilar y empaquetar

```bat
mvn clean package
```

Genera `target/sistema-tickets-spring-1.0.0.jar`, un **fat jar** ejecutable que
incluye Tomcat y todas las dependencias. Se ejecuta sin Maven ni el `pom.xml`:

```bat
java -jar target\sistema-tickets-spring-1.0.0.jar
```

Para saltar las pruebas al empaquetar:

```bat
mvn clean package -DskipTests
```

Solo comprobar que el código compila, sin empaquetar (es lo más rápido cuando
se está programando):

```bat
mvn -q compile
```

Con `-q` Maven solo imprime errores y advertencias. **Si no imprime nada, la
compilación fue correcta.** Es el comando usado para verificar este proyecto.

---

## 7. Problemas frecuentes

| Sintoma | Causa y solución |
|---|---|
| `package org.springframework.boot does not exist` | Las dependencias no están descargadas. No hay nada mal en el código: ejecutar `mvn compile` y dejar que Maven baje los jars. |
| `invalid target release: 17` | Maven está usando Java 8. Revisar `mvn -v` y corregir `JAVA_HOME`. |
| `Error: A JNI error has occurred` al ejecutar el jar | El jar se compilo con una versión de Java más nueva que la que intenta ejecutarlo. Usar el mismo JDK para compilar y para ejecutar. |
| `Port 4000 was already in use` | Ya hay algo escuchando en ese puerto (normalmente el backend de Node.js). Cerrarlo, o cambiar `server.port` en `application.properties`. |
| `Table ... doesn't exist` o error de datasource | Normal **mientras la persistencia este sin conectar**: el bloque de datasource está comentado a propósito. Ver el punto siguiente. |
| La consola se queda en `Starting SistemaTicketsApplication` | Está descargando dependencias. Esperar; solo pasa la primera vez. |

---

## 8. Sobre la persistencia (aún no conectada)

Este proyecto se entrego como **estructura**: DTOs, servicios y controladores
completos, pero sin acceso a datos. Los repositorios son **interfaces sin
implementación** y los servicios lanzan `UnsupportedOperationException` en los
puntos donde haría falta escribir en la base.

Consecuencia práctica: **la aplicación arranca y responde en `/vivo`, `/salud`
y `/`, pero las rutas de negocio fallan** porque no hay nada detras.

Para conectarla a PostgreSQL hay que:

1. **Descomentar en `pom.xml`** el bloque `3. PENDIENTES PARA LA VERSIÓN
   FUNCIONAL` (`data-jpa`, `security`, `postgresql`, `jjwt-api`).
2. **Descomentar el datasource** en `application.properties` y darle valores a
   `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER` y `DB_PASSWORD` por variables de
   entorno. Las credenciales no se escriben en el archivo.
3. **Implementar los repositorios**: crear una clase por cada interfaz de
   `repository/` (con JPA o con `JdbcTemplate`).
4. **Sustituir los `UnsupportedOperationException`** de los servicios por las
   llamadas correspondientes al repositorio.

El esquema SQL de PostgreSQL ya existe en el proyecto original, en
`database/init/01-schema.sql`, y es la referencia para el punto 3: los enums
`RolUsuario`, `EstadoTicket`, `PrioridadTicket`, `CanalNotificacion` y
`EstadoNotificacion` de este proyecto son la copia exacta de los tipos `ENUM`
de PostgreSQL.

---

## 9. Verificación realizada

Este proyecto se compilo con un JDK 23 y **Maven 3.9**, con el resultado
`BUILD SUCCESS`. Nota: el `pom.xml` declara `java.version` **17** mientras que
el JDK que compilo es el **23**, así que la compilación valida que el código
sea compatible hacia adelante, pero no ejercicio el objetivo 17 exacto. Para
reproducir la comprobación con la versión objetivo hay que usar un JDK 17:

```bat
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.13.11-hotspot"
mvn -q -B clean compile
```

---

## 10. Scripts de verificación oficiales

El proyecto original incluye sus propios verificadores (`verificar-api.js`,
`verificar-cabeceras.bat`, `verificar-rendimiento.bat`). Este proyecto Spring
Boot **no los reutiliza**: apuntan a los contratos del backend Node.js. Si se
quiere comparar ambas implementaciones, hay que arrancarlas en puertos
distintos y ajustar la URL base de los scripts.
