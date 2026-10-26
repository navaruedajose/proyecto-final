package co.edu.sistematickets.modulos.comentarios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

/**
 * Comentario de un ticket.
 *
 * <p>Se usa el mismo DTO para crear y para leer, porque los campos coinciden.
 * Si en el futuro divergieran (por ejemplo, si al crear se admitiera un
 * identificador de comentario padre), lo correcto sería separarlos en lugar de
 * dejar campos opcionales que solo aplican en un sentido.</p>
 *
 * <p><strong>Sobre {@code esInterno}.</strong> Marca la nota como privada: la
 * ven los agentes, no el solicitante. Es la razón de que el servicio tenga que
 * filtrar los comentarios según quien pregunta, y no limitarse a devolver
 * todos los del ticket. Un filtro que se olvide aquí no rompe nada visible,
 * pero filtra conversaciones internas al cliente, así que conviene probarlo
 * explicitamente.</p>
 *
 * @param id          identificador del comentario
 * @param ticketId    ticket al que pertenece
 * @param autorId     identificador del autor
 * @param autorNombre nombre del autor, ya resuelto para la vista
 * @param contenido   texto del comentario
 * @param esInterno   {@code true} si es una nota privada entre agentes
 * @param creadoEn    fecha de publicación
 */
public record ComentarioDto(

        UUID id,

        UUID ticketId,

        UUID autorId,

        String autorNombre,

        @NotBlank(message = "El contenido del comentario es obligatorio.")
        @Size(max = 5000, message = "El comentario no puede exceder 5000 caracteres.")
        String contenido,

        Boolean esInterno,

        Instant creadoEn
) {
}
