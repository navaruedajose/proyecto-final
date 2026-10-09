package co.edu.sistematickets.modulos.tickets.dto;

import co.edu.sistematickets.dominio.PrioridadTicket;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Datos para crear un ticket.
 *
 * <p>Los limites de longitud replican las restricciones {@code CHECK} del
 * esquema de PostgreSQL ({@code titulo >= 5}, {@code descripcion >= 10}). Se
 * validan en las dos capas a proposito: aqui para devolver un error legible
 * al usuario antes de tocar la base, y alli como ultima defensa por si alguien
 * escribe directamente en la base de datos.</p>
 *
 * <p>Campos que <strong>no</strong> aparecen y por que:</p>
 * <ul>
 *   <li>{@code codigo} — lo genera un trigger de base ({@code TK-AAAA-000000}).
 *       Si lo aceptaramos del cliente, cualquiera podria falsificar folios.</li>
 *   <li>{@code estado} — todo ticket nace en {@code NUEVO}. Permitir crearlo
 *       en {@code CERRADO} seria un agujero de trazabilidad.</li>
 *   <li>{@code fechaLimiteSla} — la calcula un trigger a partir del SLA de la
 *       categoria. Es un dato derivado, no una entrada.</li>
 * </ul>
 *
 * @param titulo        resumen breve del problema
 * @param descripcion   detalle de lo ocurrido
 * @param categoriaId   categoria que determina el SLA; puede ser {@code null}
 * @param prioridad     urgencia; si se omite, el servicio aplica {@code MEDIA}
 * @param solicitanteId solo un administrador puede crear a nombre de otro
 * @param agenteId      autoasignacion al crear; solo personal de soporte
 */
public record CrearTicketDto(

        @NotBlank(message = "El titulo es obligatorio.")
        @Size(min = 5, max = 200, message = "El titulo debe tener entre 5 y 200 caracteres.")
        String titulo,

        @NotBlank(message = "La descripcion es obligatoria.")
        @Size(min = 10, max = 5000, message = "La descripcion debe tener entre 10 y 5000 caracteres.")
        String descripcion,

        UUID categoriaId,

        PrioridadTicket prioridad,

        UUID solicitanteId,

        UUID agenteId
) {
}
