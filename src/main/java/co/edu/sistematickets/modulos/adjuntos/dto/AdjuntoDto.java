package co.edu.sistematickets.modulos.adjuntos.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Evidencia adjunta a un ticket.
 *
 * <p>El DTO expone datos descriptivos del archivo, pero <strong>no la ruta
 * física</strong> donde está guardado. Publicar la ruta del sistema de
 * archivos es un problema de seguridad: revela la estructura del servidor y
 * permite deducir donde buscar si algún día hay una vulnerabilidad de lectura
 * de ficheros. El cliente solo necesita el identificador para pedir la
 * descarga; el servidor resuelve dónde está.</p>
 *
 * <p>{@code hashSha256} si se expone, y sirve para dos cosas: deduplicar (dos
 * subidas del mismo archivo no deberían ocupar el doble) y verificar
 * integridad en la descarga.</p>
 *
 * @param id             identificador del adjunto
 * @param ticketId       ticket al que pertenece
 * @param comentarioId   comentario asociado, si se subio junto a uno
 * @param nombreOriginal nombre del archivo tal como lo subio el usuario
 * @param mimeType       tipo MIME declarado
 * @param tamanoBytes    tamaño en bytes
 * @param hashSha256     hash del contenido, para deduplicar y verificar
 * @param subidoPor      identificador de quien lo subio
 * @param subidoPorNombre nombre de quien lo subio, ya resuelto
 * @param creadoEn       fecha de subida
 */
public record AdjuntoDto(
        UUID id,
        UUID ticketId,
        UUID comentarioId,
        String nombreOriginal,
        String mimeType,
        Long tamanoBytes,
        String hashSha256,
        UUID subidoPor,
        String subidoPorNombre,
        Instant creadoEn
) {
}
