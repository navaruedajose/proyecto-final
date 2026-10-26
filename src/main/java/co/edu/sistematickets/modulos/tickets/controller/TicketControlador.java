package co.edu.sistematickets.modulos.tickets.controller;

import co.edu.sistematickets.comun.RespuestaApi;
import co.edu.sistematickets.dominio.RolUsuario;
import co.edu.sistematickets.modulos.tickets.dto.ActualizarTicketDto;
import co.edu.sistematickets.modulos.tickets.dto.AsignarAgenteDto;
import co.edu.sistematickets.modulos.tickets.dto.CambiarEstadoDto;
import co.edu.sistematickets.modulos.tickets.dto.CrearTicketDto;
import co.edu.sistematickets.modulos.tickets.dto.FiltrosTicketDto;
import co.edu.sistematickets.modulos.tickets.dto.TicketDto;
import co.edu.sistematickets.modulos.tickets.service.TicketServicio;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Endpoints REST del módulo de tickets.
 *
 * <p>Un controlador no toma decisiones de negocio: valida el formato de la
 * entrada (con {@code @Valid}), llama al servicio y envuelve el resultado en
 * el contrato de respuesta. Si en algún método aparece un {@code if} que no
 * sea comprobación de sintaxis, es que la lógica está en el sitio
 * equivocado.</p>
 *
 * <p><strong>Nota sobre la identidad del usuario.</strong> Los métodos
 * reciben {@code usuarioId} y {@code rol} como parámetros. En la versión
 * funcional no seran parámetros: se obtendran del token JWT con
 * {@code @AuthenticationPrincipal}, de modo que <em>no exista forma</em> de
 * que el cliente declare quien es. Aquí se dejan explicitos para que la firma
 * del servicio quede fijada y se vea con claridad que ambos datos son entradas
 * del caso de uso.</p>
 */
@RestController
@RequestMapping("/api/v1/tickets")
public class TicketControlador {

    private final TicketServicio ticketServicio;

    /**
     * @param ticketServicio servicio de tickets
     */
    public TicketControlador(TicketServicio ticketServicio) {
        this.ticketServicio = ticketServicio;
    }

    /**
     * Lista tickets con filtros y paginación.
     *
     * <p>{@code @ModelAttribute} enlaza los parámetros de consulta al DTO de
     * filtros. Con más de tres parámetros, un objeto es mucho mejor que una
     * lista de argumentos: añadir un filtro nuevo no cambia la firma.</p>
     *
     * @param filtros criterios de búsqueda, paginación y ordenación
     * @return página de tickets con sus metadatos
     */
    @GetMapping
    public ResponseEntity<RespuestaApi<List<TicketDto>>> listar(@ModelAttribute FiltrosTicketDto filtros) {
        // Pendiente: sustituir por la identidad extraída del token.
        UUID usuarioId = null;
        RolUsuario rol = RolUsuario.SOLICITANTE;

        return ResponseEntity.ok(ticketServicio.listar(filtros, usuarioId, rol));
    }

    /**
     * Devuelve los indicadores del panel de tickets.
     *
     * <p><strong>Esta ruta se declara antes que {@code /{id}}</strong> y el
     * orden es obligatorio: si {@code /{id}} fuera primero, Spring intentaría
     * interpretar la cadena {@code "estadisticas"} como un UUID y fallaría.
     * Es un error clásico y silencioso, así que conviene comentarlo.</p>
     *
     * @return indicadores agregados
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<RespuestaApi<Object>> estadisticas() {
        UUID usuarioId = null;
        RolUsuario rol = RolUsuario.SOLICITANTE;

        return ResponseEntity.ok(RespuestaApi.ok(ticketServicio.estadisticas(usuarioId, rol)));
    }

    /**
     * Devuelve un ticket por su identificador.
     *
     * @param id identificador del ticket
     * @return el ticket solicitado
     */
    @GetMapping("/{id}")
    public ResponseEntity<RespuestaApi<TicketDto>> obtener(@PathVariable UUID id) {
        UUID usuarioId = null;
        RolUsuario rol = RolUsuario.SOLICITANTE;

        return ResponseEntity.ok(RespuestaApi.ok(ticketServicio.obtener(id, usuarioId, rol)));
    }

    /**
     * Devuelve el historial de cambios de un ticket.
     *
     * @param id identificador del ticket
     * @return el ticket con su bitacora de cambios
     */
    @GetMapping("/{id}/historial")
    public ResponseEntity<RespuestaApi<TicketDto>> historial(@PathVariable UUID id) {
        UUID usuarioId = null;
        RolUsuario rol = RolUsuario.SOLICITANTE;

        return ResponseEntity.ok(RespuestaApi.ok(ticketServicio.obtenerHistorial(id, usuarioId, rol)));
    }

    /**
     * Crea un ticket.
     *
     * <p>Devuelve {@code 201 Created} y no {@code 200}. La distinción no es
     * cosmética: un cliente o un intermediario puede tratar una creación
     * exitosa de forma distinta a una lectura, y el código correcto evita
     * tener que inspeccionar el cuerpo para saber que paso.</p>
     *
     * @param datos datos del ticket
     * @return el ticket creado
     */
    @PostMapping
    public ResponseEntity<RespuestaApi<TicketDto>> crear(@Valid @RequestBody CrearTicketDto datos) {
        UUID usuarioId = null;
        RolUsuario rol = RolUsuario.SOLICITANTE;

        TicketDto creado = ticketServicio.crear(datos, usuarioId, rol);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RespuestaApi.creado(creado, "Ticket creado correctamente."));
    }

    /**
     * Actualiza los campos descriptivos de un ticket.
     *
     * <p>Se usa {@code PATCH} y no {@code PUT} porque la actualización es
     * parcial: el cliente envía solo lo que cambia. Con {@code PUT} la
     * semántica exigiria enviar el recurso completo, y un campo omitido
     * tendría que interpretarse como «borralo», que no es lo que se quiere.</p>
     *
     * @param id      identificador del ticket
     * @param cambios campos a modificar
     * @return el ticket actualizado
     */
    @PatchMapping("/{id}")
    public ResponseEntity<RespuestaApi<TicketDto>> actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody ActualizarTicketDto cambios) {

        UUID usuarioId = null;
        RolUsuario rol = RolUsuario.SOLICITANTE;

        return ResponseEntity.ok(
                RespuestaApi.ok(ticketServicio.actualizar(id, cambios, usuarioId, rol),
                        "Ticket actualizado correctamente."));
    }

    /**
     * Cambia el estado de un ticket.
     *
     * <p>Endpoint propio y separado de la edición normal. La razón es de
     * auditoría: cambiar de estado genera una entrada en el historial y puede
     * disparar notificaciones, así que merece su propia ruta. Si se mezclara
     * con el PATCH general, el historial no podría distinguir un cambio de
     * estado de una corrección de redacción.</p>
     *
     * @param id    identificador del ticket
     * @param datos estado destino, solución y observación
     * @return el ticket en su nuevo estado
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<RespuestaApi<TicketDto>> cambiarEstado(
            @PathVariable UUID id,
            @Valid @RequestBody CambiarEstadoDto datos) {

        UUID usuarioId = null;
        RolUsuario rol = RolUsuario.AGENTE;

        return ResponseEntity.ok(
                RespuestaApi.ok(ticketServicio.cambiarEstado(id, datos, usuarioId, rol),
                        "Estado del ticket actualizado."));
    }

    /**
     * Asigna o reasigna el agente de un ticket.
     *
     * @param id    identificador del ticket
     * @param datos agente destino y observación
     * @return el ticket con el nuevo responsable
     */
    @PatchMapping("/{id}/agente")
    public ResponseEntity<RespuestaApi<TicketDto>> asignarAgente(
            @PathVariable UUID id,
            @Valid @RequestBody AsignarAgenteDto datos) {

        UUID usuarioId = null;
        RolUsuario rol = RolUsuario.AGENTE;

        return ResponseEntity.ok(
                RespuestaApi.ok(ticketServicio.asignarAgente(id, datos, usuarioId, rol),
                        "Agente asignado correctamente."));
    }

    /**
     * Elimina un ticket (borrado lógico).
     *
     * <p>Devuelve {@code 204 No Content}: si la operación tuvo éxito no queda
     * nada que contar, y un cuerpo vacío lo comunica sin ambiguedad.</p>
     *
     * @param id identificador del ticket
     * @return respuesta sin cuerpo
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        UUID usuarioId = null;
        RolUsuario rol = RolUsuario.ADMINISTRADOR;

        ticketServicio.eliminar(id, usuarioId, rol);
        return ResponseEntity.noContent().build();
    }
}
