package co.edu.sistematickets.modulos.tickets.mapper;

import co.edu.sistematickets.dominio.EstadoTicket;
import co.edu.sistematickets.modulos.tickets.dto.TicketDto;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * Traduce entre la entidad de persistencia y el DTO de salida.
 *
 * <p>Se aísla en su propia clase por una razón concreta: los campos derivados
 * del ticket ({@code slaVencido}, {@code horasRestantes}) son reglas de
 * negocio, no simples copias de columna. Si el mapeo viviera dentro del
 * servicio, esas reglas quedarian enterradas entre la lógica de casos de uso y
 * nadie las encontraria al buscar «donde se decide que un ticket esta
 * vencido».</p>
 *
 * <p>Al ser una clase sin estado, se registra como {@code @Component} y se
 * inyecta donde haga falta.</p>
 */
@Component
public class TicketMapper {

    /**
     * Calcula si el SLA está vencido.
     *
     * <p>Un ticket está vencido si tiene fecha límite, ya paso esa fecha y
     * todavía no fue resuelto. Los tres criterios importan:</p>
     * <ul>
     *   <li>Sin fecha límite no hay SLA que incumplir.</li>
     *   <li>Con fecha de resolución anterior al límite, el SLA se cumplio
     *       aunque hoy la fecha ya haya pasado. Comparar solo contra «ahora»
     *       marcaria como vencidos tickets que se resolvieron a tiempo.</li>
     * </ul>
     *
     * @param fechaLimite      fecha límite del SLA; puede ser {@code null}
     * @param fechaResolucion  fecha en que se resolvio; {@code null} si sigue abierto
     * @return {@code true} si el SLA está incumplido
     */
    public boolean calcularSlaVencido(Instant fechaLimite, Instant fechaResolucion) {
        if (fechaLimite == null) {
            return false;
        }
        Instant referencia = (fechaResolucion != null) ? fechaResolucion : Instant.now();
        return referencia.isAfter(fechaLimite);
    }

    /**
     * Calcula las horas que quedan de SLA, o las que se pasaron.
     *
     * <p>Si el ticket ya tiene fecha de resolución, el cálculo se hace contra
     * esa fecha para dar un número estable: el margen con el que se resolvio,
     * que no cambia con el paso del tiempo. Si sigue abierto, se calcula
     * contra el instante actual, de modo que el valor baja en tiempo real
     * mientras la interfaz se refresca.</p>
     *
     * <p>El signo es informativo: negativo significa que el SLA ya vencio.</p>
     *
     * @param fechaLimite     fecha límite del SLA; puede ser {@code null}
     * @param fechaResolucion fecha de resolución; {@code null} si sigue abierto
     * @return horas restantes (negativas si vencio), o {@code null} sin SLA
     */
    public Long calcularHorasRestantes(Instant fechaLimite, Instant fechaResolucion) {
        if (fechaLimite == null) {
            return null;
        }
        Instant referencia = (fechaResolucion != null) ? fechaResolucion : Instant.now();
        return Duration.between(referencia, fechaLimite).toHours();
    }

    /**
     * Determina si un ticket puede pasar al estado indicado.
     *
     * <p>Delega en el propio {@link EstadoTicket}, que es quien conoce el mapa
     * de transiciones. Este método existe solo para dar un punto de entrada
     * desde la capa de negocio sin que el servicio tenga que construir el
     * mensaje de error.</p>
     *
     * @param estadoActual estado en el que está el ticket
     * @param estadoNuevo  estado al que se quiere pasar
     * @return {@code true} si la transición es legal
     */
    public boolean esTransicionValida(EstadoTicket estadoActual, EstadoTicket estadoNuevo) {
        return estadoActual != null && estadoActual.puedeTransicionarA(estadoNuevo);
    }

    /**
     * Redacta el mensaje de error de una transición ilegal.
     *
     * <p>El mensaje enumera los estados a los que <em>si</em> se podría pasar.
     * Un error que solo dice «transición no permitida» obliga al usuario a
     * adivinar; decirle cuales son validas convierte el mensaje en ayuda.</p>
     *
     * @param estadoActual estado en el que está el ticket
     * @param estadoNuevo  estado rechazado
     * @return mensaje listo para devolver al cliente
     */
    public String describirTransicionIlegal(EstadoTicket estadoActual, EstadoTicket estadoNuevo) {
        if (estadoActual == null) {
            return "El ticket no tiene un estado válido del que partir.";
        }
        if (estadoActual.esFinal()) {
            return "El ticket está en estado %s, que es final: ya no admite más cambios."
                    .formatted(estadoActual);
        }
        return "No se puede pasar del estado %s al estado %s."
                .formatted(estadoActual, estadoNuevo);
    }

    /**
     * Proyecta una entidad de persistencia al DTO de salida.
     *
     * <p>Firma provisional: recibira la entidad {@code TicketEntidad} cuando
     * exista la capa de persistencia. Se deja documentada para fijar el
     * contrato y evitar que el mapeo acabe escribiendose a mano en el
     * controlador.</p>
     *
     * @param entidad entidad de persistencia
     * @return DTO listo para serializar
     */
    public TicketDto aDto(Object entidad) {
        throw new UnsupportedOperationException(
                "El mapeo desde la entidad se implementara junto con la capa de persistencia.");
    }
}
