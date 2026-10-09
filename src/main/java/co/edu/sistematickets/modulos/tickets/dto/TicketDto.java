package co.edu.sistematickets.modulos.tickets.dto;

import co.edu.sistematickets.dominio.EstadoTicket;
import co.edu.sistematickets.dominio.PrioridadTicket;

import java.time.Instant;
import java.util.UUID;

/**
 * Ticket tal como sale hacia el cliente.
 *
 * <p>Es la representacion de lectura del recurso. No se devuelve nunca la
 * entidad de base de datos directamente, y la razon es de seguridad: la
 * entidad contiene columnas que no deben salir jamas por la red (el hash de la
 * contrasena en el caso de usuario, o {@code eliminado_en} en el caso del
 * ticket). Al proyectar sobre un DTO, ese peligro desaparece por construccion:
 * lo que no esta en el DTO no se puede filtrar por descuido.</p>
 *
 * <p>Incluye campos <strong>derivados</strong> que no existen como columna:</p>
 * <ul>
 *   <li>{@code slaVencido} — se calcula comparando la fecha limite con ahora.</li>
 *   <li>{@code horasRestantes} — negativo si ya vencio; util para pintar la
 *       cuenta atras en la interfaz sin que el cliente tenga que hacer la
 *       resta de fechas.</li>
 * </ul>
 * <p>Se calculan en el servicio y viajan ya resueltos. El cliente no deberia
 * tener que replicar la regla de negocio de que significa «vencido».</p>
 *
 * @param id                identificador interno
 * @param codigo            folio legible, generado por trigger
 * @param titulo            resumen del problema
 * @param descripcion       detalle del problema
 * @param estado            estado actual en el ciclo de vida
 * @param prioridad         urgencia
 * @param categoriaId       categoria asociada; {@code null} si no tiene
 * @param categoriaNombre   nombre de la categoria, ya resuelto para la vista
 * @param solicitanteId     quien reporto el ticket
 * @param solicitanteNombre nombre del solicitante, ya resuelto
 * @param agenteId          agente asignado; {@code null} si sigue sin dueño
 * @param agenteNombre      nombre del agente, ya resuelto
 * @param solucion          texto de la solucion, una vez resuelto
 * @param fechaLimiteSla    momento en que vence el SLA
 * @param fechaResolucion   momento en que se resolvio
 * @param fechaCierre       momento en que se cerro
 * @param slaVencido        {@code true} si ya paso la fecha limite sin resolverse
 * @param horasRestantes    horas que quedan de SLA; negativo si vencio
 * @param creadoEn          fecha de alta
 * @param actualizadoEn     ultima modificacion
 */
public record TicketDto(
        UUID id,
        String codigo,
        String titulo,
        String descripcion,
        EstadoTicket estado,
        PrioridadTicket prioridad,
        UUID categoriaId,
        String categoriaNombre,
        UUID solicitanteId,
        String solicitanteNombre,
        UUID agenteId,
        String agenteNombre,
        String solucion,
        Instant fechaLimiteSla,
        Instant fechaResolucion,
        Instant fechaCierre,
        boolean slaVencido,
        Long horasRestantes,
        Instant creadoEn,
        Instant actualizadoEn
) {
}
