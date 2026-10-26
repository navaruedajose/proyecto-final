package co.edu.sistematickets.modulos.tickets.dto;

import co.edu.sistematickets.dominio.EstadoTicket;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Datos para cambiar el estado de un ticket.
 *
 * <p>Este endpoint es el que ejecuta la máquina de estados. El servicio
 * comprueba con {@link EstadoTicket#puedeTransicionarA(EstadoTicket)} si el
 * salto es legal y lanza {@code ConflictoEstadoExcepcion} (HTTP 409) si no lo
 * es. La validación de Bean Validation solo garantiza que el estado destino
 * <em>existe</em>; que sea <em>alcanzable</em> es regla de negocio.</p>
 *
 * @param estado      estado al que se quiere pasar
 * @param solución     texto de la solución aplicada; obligatorio para resolver
 * @param observación  nota que se guarda en el historial del cambio
 */
public record CambiarEstadoDto(

        @NotNull(message = "El estado es obligatorio.")
        EstadoTicket estado,

        @Size(max = 5000, message = "La solución no puede exceder 5000 caracteres.")
        String solucion,

        @Size(max = 500, message = "La observación no puede exceder 500 caracteres.")
        String observacion
) {
}
