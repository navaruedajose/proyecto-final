package co.edu.sistematickets.modulos.tickets.dto;

import co.edu.sistematickets.dominio.PrioridadTicket;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Datos para crear un ticket.
 *
 * <p>Los límites de longitud replican las restricciones {@code CHECK} del
 * esquema de PostgreSQL ({@code título >= 5}, {@code descripción >= 10}). Se
 * validan en las dos capas a propósito: aquí para devolver un error legible
 * al usuario antes de tocar la base, y alli como última defensa por si alguien
 * escribe directamente en la base de datos.</p>
 *
 * <p>Campos que <strong>no</strong> aparecen y por que:</p>
 * <ul>
 *   <li>{@code código} — lo genera un trigger de base ({@code TK-AAAA-000000}).
 *       Si lo aceptaramos del cliente, cualquiera podría falsificar folios.</li>
 *   <li>{@code estado} — todo ticket nace en {@code NUEVO}. Permitir crearlo
 *       en {@code CERRADO} sería un agujero de trazabilidad.</li>
 *   <li>{@code fechaLimiteSla} — la calcula un trigger a partir del SLA de la
 *       categoría. Es un dato derivado, no una entrada.</li>
 * </ul>
 *
 * @param título        resumen breve del problema
 * @param descripción   detalle de lo ocurrido
 * @param categoriaId   categoría que determina el SLA; puede ser {@code null}
 * @param prioridad     urgencia; si se omite, el servicio aplica {@code MEDIA}
 * @param solicitanteId solo un administrador puede crear a nombre de otro
 * @param agenteId      autoasignación al crear; solo personal de soporte
 */
public record CrearTicketDto(

        @NotBlank(message = "El título es obligatorio.")
        @Size(min = 5, max = 200, message = "El título debe tener entre 5 y 200 caracteres.")
        String titulo,

        @NotBlank(message = "La descripción es obligatoria.")
        @Size(min = 10, max = 5000, message = "La descripción debe tener entre 10 y 5000 caracteres.")
        String descripcion,

        UUID categoriaId,

        PrioridadTicket prioridad,

        UUID solicitanteId,

        UUID agenteId
) {
}
