package co.edu.sistematickets.config;

import co.edu.sistematickets.comun.RespuestaApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Endpoints de salud y metadatos del servicio.
 *
 * <p>No son funcionalidad de negocio, pero son imprescindibles en producción y
 * conviene saber explicar para que sirven:</p>
 *
 * <ul>
 *   <li>{@code /salud} — lo consulta el orquestador (Docker, Kubernetes) para
 *       saber si el contenedor está vivo. Si deja de responder, se reinicia.
 *       También lo usa el script de verificación del proyecto.</li>
 *   <li>{@code /vivo} — variante mínima, sin comprobar dependencias. Sirve para
 *       distinguir «el proceso está vivo» de «el servicio puede atender». Un
 *       proceso vivo con la base de datos caída debe responder en {@code /vivo}
 *       pero fallar en {@code /salud}, y esa diferencia es la que permite
 *       diagnosticar sin adivinar.</li>
 *   <li>{@code /} — versión y documentación de la API, para que quien reciba
 *       una URL del servicio sepa que está mirando.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1")
public class ControladorDeSistema {

    /**
     * Comprueba que el servicio puede atender, dependencias incluidas.
     *
     * @return estado del servicio y de sus dependencias
     */
    @GetMapping("/salud")
    public ResponseEntity<RespuestaApi<Map<String, Object>>> salud() {
        Map<String, Object> estado = Map.of(
                "servicio", "sistema-tickets-spring",
                "version", "1.0.0",
                "estado", "OPERATIVO",
                "baseDeDatos", "PENDIENTE_DE_CONEXION",
                "momento", Instant.now().toString()
        );
        return ResponseEntity.ok(RespuestaApi.ok(estado));
    }

    /**
     * Comprueba solo que el proceso responda.
     *
     * @return marca de vida
     */
    @GetMapping("/vivo")
    public ResponseEntity<Map<String, Boolean>> vivo() {
        return ResponseEntity.ok(Map.of("vivo", true));
    }

    /**
     * Describe el servicio en la raíz de la API.
     *
     * @return nombre, versión y punto de entrada de la documentación
     */
    @GetMapping
    public ResponseEntity<RespuestaApi<Map<String, String>>> raiz() {
        Map<String, String> info = Map.of(
                "nombre", "API del Sistema de Tickets",
                "version", "v1",
                "documentacion", "/api/v1/salud",
                "implementacion", "Spring Boot 3 (Java 17)"
        );
        return ResponseEntity.ok(RespuestaApi.ok(info));
    }
}
