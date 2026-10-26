package co.edu.sistematickets.modulos.tickets.dto;

import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Datos para asignar o reasignar el agente de un ticket.
 *
 * <p>{@code agenteId} admite {@code null} a propósito: desasignar un ticket
 * (devolverlo a la cola sin responsable) es una operación legitima y frecuente
 * cuando un agente se ausenta. Por eso el campo no lleva {@code @NotNull},
 * aunque la anotación por defecto del proyecto sea exigir los campos
 * obligatorios.</p>
 *
 * @param agenteId    agente que tomara el ticket; {@code null} para desasignar
 * @param observación motivo del cambio, que queda en el historial
 */
public record AsignarAgenteDto(

        UUID agenteId,

        @Size(max = 500, message = "La observación no puede exceder 500 caracteres.")
        String observacion
) {
}
