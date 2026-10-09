package co.edu.sistematickets.modulos.tickets.dto;

import co.edu.sistematickets.dominio.PrioridadTicket;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Datos para modificar los campos descriptivos de un ticket.
 *
 * <p>Todos los campos son opcionales porque es una actualizacion parcial: el
 * cliente envia solo lo que cambia. El servicio rechaza la peticion si no
 * llega ningun campo, para que un PATCH vacio no se registre como si hubiera
 * ocurrido algo.</p>
 *
 * <p>Lo que <strong>no</strong> se puede cambiar por aqui, y por que:</p>
 * <ul>
 *   <li>{@code estado} tiene su propio endpoint, porque cambiar de estado
 *       dispara notificaciones e historial. Mezclarlo con la edicion de texto
 *       haria imposible auditar que fue un cambio de estado y que una
 *       correccion de redaccion.</li>
 *   <li>{@code agenteId} tambien tiene su endpoint, para dejar constancia de
 *       la asignacion en el historial.</li>
 *   <li>{@code codigo}, {@code solicitanteId} y las fechas no se tocan: son
 *       la identidad y la linea de tiempo del ticket.</li>
 * </ul>
 *
 * @param titulo      nuevo titulo, o {@code null} para dejarlo igual
 * @param descripcion nueva descripcion, o {@code null}
 * @param categoriaId nueva categoria; cambiarla recalcula el SLA
 * @param prioridad   nueva prioridad, o {@code null}
 */
public record ActualizarTicketDto(

        @Size(min = 5, max = 200, message = "El titulo debe tener entre 5 y 200 caracteres.")
        String titulo,

        @Size(min = 10, max = 5000, message = "La descripcion debe tener entre 10 y 5000 caracteres.")
        String descripcion,

        UUID categoriaId,

        PrioridadTicket prioridad
) {

    /**
     * Indica si la peticion no trae ningun campo que actualizar.
     *
     * @return {@code true} si todos los campos son {@code null}
     */
    public boolean estaVacio() {
        return titulo == null && descripcion == null && categoriaId == null && prioridad == null;
    }
}
