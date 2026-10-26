package co.edu.sistematickets.modulos.adjuntos.controller;

import co.edu.sistematickets.comun.RespuestaApi;
import co.edu.sistematickets.modulos.adjuntos.dto.AdjuntoDto;
import co.edu.sistematickets.modulos.adjuntos.service.AdjuntoServicio;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Endpoints REST de adjuntos (evidencias).
 *
 * <p>Igual que los comentarios, las rutas cuelgan del ticket al que pertenecen:
 * {@code /tickets/{ticketId}/adjuntos}.</p>
 */
@RestController
@RequestMapping("/api/v1/tickets/{ticketId}/adjuntos")
public class AdjuntoControlador {

    private final AdjuntoServicio adjuntoServicio;

    /**
     * @param adjuntoServicio servicio de adjuntos
     */
    public AdjuntoControlador(AdjuntoServicio adjuntoServicio) {
        this.adjuntoServicio = adjuntoServicio;
    }

    /**
     * Lista los adjuntos de un ticket.
     *
     * @param ticketId ticket del que se quieren los adjuntos
     * @return lista de adjuntos
     */
    @GetMapping
    public ResponseEntity<RespuestaApi<List<AdjuntoDto>>> listar(@PathVariable UUID ticketId) {
        return ResponseEntity.ok(RespuestaApi.ok(adjuntoServicio.listar(ticketId)));
    }

    /**
     * Sube una o varias evidencias.
     *
     * <p>{@code @RequestParam("archivos")} acepta varios archivos con el mismo
     * nombre de campo, que es lo que permite subir una selección multiple en
     * una sola petición. El nombre del parámetro debe coincidir con el que usa
     * el cliente; el frontend envía {@code archivos}.</p>
     *
     * @param ticketId ticket destino
     * @param archivos archivos recibidos
     * @return los adjuntos creados
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RespuestaApi<List<AdjuntoDto>>> subir(
            @PathVariable UUID ticketId,
            @RequestParam("archivos") List<MultipartFile> archivos) {

        UUID usuarioId = null;
        return ResponseEntity.ok(RespuestaApi.ok(
                adjuntoServicio.subir(ticketId, archivos, usuarioId),
                "Evidencias subidas correctamente."));
    }

    /**
     * Descarga un adjunto.
     *
     * <p>Devuelve un recurso binario, no el contrato JSON habitual: el
     * contenido de la respuesta <em>es</em> el archivo.</p>
     *
     * <p>Dos cabeceras importan aquí:</p>
     * <ul>
     *   <li>{@code Content-Disposition: attachment} fuerza la descarga.
     *       Servir el archivo con {@code inline} permitiría que el navegador lo
     *       interpretara; con {@code attachment} se limita a guardarlo, que es
     *       la protección que sustituye al antivirus ausente.</li>
     *   <li>{@code Content-Type: application/octet-stream} declara el contenido
     *       como binario opaco, para que el navegador no intente adivinar el
     *       tipo y abrirlo.</li>
     * </ul>
     *
     * @param ticketId  ticket al que pertenece
     * @param adjuntoId adjunto a descargar
     * @return el archivo como recurso descargable
     */
    @GetMapping("/{adjuntoId}/descargar")
    public ResponseEntity<Resource> descargar(
            @PathVariable UUID ticketId,
            @PathVariable UUID adjuntoId) {

        throw new UnsupportedOperationException(
                "La descarga se implementara al conectar el almacenamiento de archivos. "
                        + "Recordar: Content-Disposition attachment y application/octet-stream.");
    }

    /**
     * Elimina un adjunto.
     *
     * @param ticketId  ticket al que pertenece
     * @param adjuntoId adjunto a eliminar
     * @return respuesta sin cuerpo
     */
    @DeleteMapping("/{adjuntoId}")
    public ResponseEntity<Void> eliminar(
            @PathVariable UUID ticketId,
            @PathVariable UUID adjuntoId) {

        UUID usuarioId = null;
        adjuntoServicio.eliminar(ticketId, adjuntoId, usuarioId);
        return ResponseEntity.noContent().build();
    }
}
