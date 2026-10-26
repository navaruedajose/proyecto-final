package co.edu.sistematickets.modulos.tickets.dto;

import co.edu.sistematickets.dominio.PrioridadTicket;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Datos para modificar los campos descriptivos de un ticket.
 *
 * <p>Todos los campos son opcionales porque es una actualización parcial: el
 * cliente envía solo lo que cambia. El servicio rechaza la petición si no
 * llega ningún campo, para que un PATCH vacío no se registre como si hubiera
 * ocurrido algo.</p>
 *
 * <p>Lo que <strong>no</strong> se puede cambiar por aquí, y por que:</p>
 * <ul>
 *   <li>{@code estado} tiene su propio endpoint, porque cambiar de estado
 *       dispara notificaciones e historial. Mezclarlo con la edición de texto
 *       haría imposible auditar que fue un cambio de estado y que una
 *       corrección de redacción.</li>
 *   <li>{@code agenteId} también tiene su endpoint, para dejar constancia de
 *       la asignación en el historial.</li>
 *   <li>{@code código}, {@code solicitanteId} y las fechas no se tocan: son
 *       la identidad y la línea de tiempo del ticket.</li>
 * </ul>
 *
 * @param título      nuevo título, o {@code null} para dejarlo igual
 * @param descripción nueva descripción, o {@code null}
 * @param categoriaId nueva categoría; cambiarla recalcula el SLA
 * @param prioridad   nueva prioridad, o {@code null}
 */
public record ActualizarTicketDto(

        @Size(min = 5, max = 200, message = "El título debe tener entre 5 y 200 caracteres.")
        String titulo,

        @Size(min = 10, max = 5000, message = "La descripción debe tener entre 10 y 5000 caracteres.")
        String descripcion,

        UUID categoriaId,

        PrioridadTicket prioridad
) {

    /**
     * Indica si la petición no trae ningún campo que actualizar.
     *
     * @return {@code true} si todos los campos son {@code null}
     */
    public boolean estaVacio() {
        return titulo == null && descripcion == null && categoriaId == null && prioridad == null;
    }
}
