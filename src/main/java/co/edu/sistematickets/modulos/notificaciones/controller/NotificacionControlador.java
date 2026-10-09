package co.edu.sistematickets.modulos.notificaciones.controller;

import co.edu.sistematickets.comun.RespuestaApi;
import co.edu.sistematickets.modulos.notificaciones.dto.NotificacionDto;
import co.edu.sistematickets.modulos.notificaciones.service.NotificacionServicio;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Endpoints REST de notificaciones.
 *
 * <p>Solo para consulta y mantenimiento. No existe un endpoint para «enviar
 * una notificacion»: los envios son consecuencia de cambios de estado, los
 * dispara el sistema, no el usuario. Exponer un endpoint de envio manual
 * abriria la puerta a usar el sistema como plataforma de mensajeria.</p>
 */
@RestController
@RequestMapping("/api/v1/notificaciones")
public class NotificacionControlador {

    private final NotificacionServicio notificacionServicio;

    /**
     * @param notificacionServicio servicio de notificaciones
     */
    public NotificacionControlador(NotificacionServicio notificacionServicio) {
        this.notificacionServicio = notificacionServicio;
    }

    /**
     * Lista las notificaciones de un ticket.
     *
     * @param ticketId identificador del ticket
     * @return notificaciones asociadas
     */
    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<RespuestaApi<List<NotificacionDto>>> porTicket(@PathVariable UUID ticketId) {
        return ResponseEntity.ok(RespuestaApi.ok(notificacionServicio.listarPorTicket(ticketId)));
    }

    /**
     * Devuelve un resumen de la cola de notificaciones.
     *
     * @return conteos por estado de envio
     */
    @GetMapping("/resumen")
    public ResponseEntity<RespuestaApi<Object>> resumen() {
        return ResponseEntity.ok(notificacionServicio.resumen());
    }

    /**
     * Reintenta las notificaciones fallidas.
     *
     * <p>Devuelve el numero de reintentos lanzados. Es la operacion que se usa
     * despues de un corte del proveedor de correo.</p>
     *
     * @return numero de notificaciones reintentadas
     */
    @PostMapping("/reintentar")
    public ResponseEntity<RespuestaApi<Integer>> reintentar() {
        int reintentadas = notificacionServicio.reintentarFallidas();
        return ResponseEntity.ok(RespuestaApi.ok(reintentadas,
                "Notificaciones reintentadas: " + reintentadas + "."));
    }
}
