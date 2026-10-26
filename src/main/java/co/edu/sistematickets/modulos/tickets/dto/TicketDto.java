package co.edu.sistematickets.modulos.tickets.dto;

import co.edu.sistematickets.dominio.EstadoTicket;
import co.edu.sistematickets.dominio.PrioridadTicket;

import java.time.Instant;
import java.util.UUID;

/**
 * Ticket tal como sale hacia el cliente.
 *
 * <p>Es la representación de lectura del recurso. No se devuelve nunca la
 * entidad de base de datos directamente, y la razón es de seguridad: la
 * entidad contiene columnas que no deben salir jamas por la red (el hash de la
 * contraseña en el caso de usuario, o {@code eliminado_en} en el caso del
 * ticket). Al proyectar sobre un DTO, ese peligro desaparece por construcción:
 * lo que no está en el DTO no se puede filtrar por descuido.</p>
 *
 * <p>Incluye campos <strong>derivados</strong> que no existen como columna:</p>
 * <ul>
 *   <li>{@code slaVencido} — se calcula comparando la fecha límite con ahora.</li>
 *   <li>{@code horasRestantes} — negativo si ya vencio; útil para pintar la
 *       cuenta atrás en la interfaz sin que el cliente tenga que hacer la
 *       resta de fechas.</li>
 * </ul>
 * <p>Se calculan en el servicio y viajan ya resueltos. El cliente no debería
 * tener que replicar la regla de negocio de que significa «vencido».</p>
 *
 * @param id                identificador interno
 * @param código            folio legible, generado por trigger
 * @param título            resumen del problema
 * @param descripción       detalle del problema
 * @param estado            estado actual en el ciclo de vida
 * @param prioridad         urgencia
 * @param categoriaId       categoría asociada; {@code null} si no tiene
 * @param categoriaNombre   nombre de la categoría, ya resuelto para la vista
 * @param solicitanteId     quien reporto el ticket
 * @param solicitanteNombre nombre del solicitante, ya resuelto
 * @param agenteId          agente asignado; {@code null} si sigue sin dueño
 * @param agenteNombre      nombre del agente, ya resuelto
 * @param solución          texto de la solución, una vez resuelto
 * @param fechaLimiteSla    momento en que vence el SLA
 * @param fechaResolucion   momento en que se resolvio
 * @param fechaCierre       momento en que se cerro
 * @param slaVencido        {@code true} si ya paso la fecha límite sin resolverse
 * @param horasRestantes    horas que quedan de SLA; negativo si vencio
 * @param creadoEn          fecha de alta
 * @param actualizadoEn     última modificación
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
