package co.edu.sistematickets.modulos.tickets.dto;

import co.edu.sistematickets.dominio.EstadoTicket;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Datos para cambiar el estado de un ticket.
 *
 * <p>Este endpoint es el que ejecuta la maquina de estados. El servicio
 * comprueba con {@link EstadoTicket#puedeTransicionarA(EstadoTicket)} si el
 * salto es legal y lanza {@code ConflictoEstadoExcepcion} (HTTP 409) si no lo
 * es. La validacion de Bean Validation solo garantiza que el estado destino
 * <em>existe</em>; que sea <em>alcanzable</em> es regla de negocio.</p>
 *
 * @param estado      estado al que se quiere pasar
 * @param solucion     texto de la solucion aplicada; obligatorio para resolver
 * @param observacion  nota que se guarda en el historial del cambio
 */
public record CambiarEstadoDto(

        @NotNull(message = "El estado es obligatorio.")
        EstadoTicket estado,

        @Size(max = 5000, message = "La solucion no puede exceder 5000 caracteres.")
        String solucion,

        @Size(max = 500, message = "La observacion no puede exceder 500 caracteres.")
        String observacion
) {
}
