package co.edu.sistematickets.modulos.comentarios.controller;

import co.edu.sistematickets.comun.RespuestaApi;
import co.edu.sistematickets.dominio.RolUsuario;
import co.edu.sistematickets.modulos.comentarios.dto.ComentarioDto;
import co.edu.sistematickets.modulos.comentarios.service.ComentarioServicio;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Endpoints REST de comentarios.
 *
 * <p>Las rutas cuelgan de {@code /tickets/{ticketId}/comentarios} y no de
 * {@code /comentarios}. Es una decisión de diseño de la API: refleja la
 * relación real entre los recursos. Un comentario no existe sin su ticket, y
 * la ruta lo deja claro; además hace imposible pedir un comentario sin decir
 * de que ticket es.</p>
 */
@RestController
@RequestMapping("/api/v1/tickets/{ticketId}/comentarios")
public class ComentarioControlador {

    private final ComentarioServicio comentarioServicio;

    /**
     * @param comentarioServicio servicio de comentarios
     */
    public ComentarioControlador(ComentarioServicio comentarioServicio) {
        this.comentarioServicio = comentarioServicio;
    }

    /**
     * Lista los comentarios de un ticket.
     *
     * @param ticketId ticket del que se quieren los comentarios
     * @return comentarios visibles para el usuario
     */
    @GetMapping
    public ResponseEntity<RespuestaApi<List<ComentarioDto>>> listar(@PathVariable UUID ticketId) {
        RolUsuario rol = RolUsuario.SOLICITANTE;
        return ResponseEntity.ok(RespuestaApi.ok(comentarioServicio.listar(ticketId, rol)));
    }

    /**
     * Publica un comentario en un ticket.
     *
     * @param ticketId ticket al que se comenta
     * @param datos    contenido del comentario
     * @return el comentario creado
     */
    @PostMapping
    public ResponseEntity<RespuestaApi<ComentarioDto>> crear(
            @PathVariable UUID ticketId,
            @Valid @RequestBody ComentarioDto datos) {

        UUID autorId = null;
        RolUsuario rol = RolUsuario.SOLICITANTE;

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RespuestaApi.creado(
                        comentarioServicio.crear(ticketId, datos, autorId, rol),
                        "Comentario publicado correctamente."));
    }

    /**
     * Elimina un comentario.
     *
     * @param ticketId     ticket al que pertenece
     * @param comentarioId comentario a borrar
     * @return respuesta sin cuerpo
     */
    @DeleteMapping("/{comentarioId}")
    public ResponseEntity<Void> eliminar(
            @PathVariable UUID ticketId,
            @PathVariable UUID comentarioId) {

        UUID usuarioId = null;
        RolUsuario rol = RolUsuario.SOLICITANTE;

        comentarioServicio.eliminar(ticketId, comentarioId, usuarioId, rol);
        return ResponseEntity.noContent().build();
    }
}
