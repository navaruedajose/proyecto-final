package co.edu.sistematickets.modulos.notificaciones.service;

import co.edu.sistematickets.comun.RespuestaApi;
import co.edu.sistematickets.modulos.notificaciones.dto.NotificacionDto;

import java.util.List;
import java.util.UUID;

/**
 * Reglas de negocio de notificaciones.
 *
 * <p>El módulo que entrega los avisos por correo y SMS, sobre todo cuando un
 * ticket pasa a {@code RESUELTO}. Las decisiones de diseño son las que hay que
 * poder defender:</p>
 *
 * <ol>
 *   <li><strong>El envío no bloquea la petición del usuario.</strong> Se encola
 *       y se envía después. Si el proveedor tarda o está caído, el cambio de
 *       estado ya está confirmado y el usuario no se queda esperando.</li>
 *   <li><strong>Un fallo de envío no revierte el cambio de estado.</strong> El
 *       ticket está resuelto de verdad; lo que falla es el aviso. Mezclar
 *       ambas cosas haría que un problema de correo impidiera cerrar tickets,
 *       que es peor que no avisar.</li>
 *   <li><strong>Se reintenta con límite.</strong> Reintentar sin tope ante un
 *       fallo permanente (un número de teléfono inválido) sería un bucle
 *       infinito. Tras agotar los intentos, la notificación queda en
 *       {@code FALLIDO} y visible para que un administrador decida.</li>
 *   <li><strong>El envío fallido es información, no ruido.</strong> El estado
 *       {@code FALLIDO} existe para que el fallo se pueda consultar; una
 *       notificación que simplemente desaparece deja al usuario creyendo que
 *       se envío.</li>
 * </ol>
 */
public class NotificacionServicio {

    /**
     * Encola la notificación de ticket resuelto por los dos canales.
     *
     * <p>Se encolan las dos notificaciones por separado, cada una con su
     * estado. Así, si el correo sale bien y el SMS falla, queda constancia de
     * cada uno y se reintenta solo el que fallo. Tratarlas como una sola
     * operación haría que un fallo en el SMS marcara también el correo como no
     * enviado.</p>
     *
     * @param ticketId identificador del ticket resuelto
     * @param usuarioId destinatario
     */
    public void encolarTicketResuelto(UUID ticketId, UUID usuarioId) {
        throw new UnsupportedOperationException(
                "El encolado se implementara al conectar la persistencia. "
                        + "Se encola una notificación por canal, cada una con su propio estado.");
    }

    /**
     * Lista las notificaciones de un ticket.
     *
     * @param ticketId identificador del ticket
     * @return notificaciones asociadas
     */
    public List<NotificacionDto> listarPorTicket(UUID ticketId) {
        throw new UnsupportedOperationException(
                "La consulta se implementara al conectar la persistencia.");
    }

    /**
     * Devuelve un resumen de la cola de notificaciones.
     *
     * @return conteos por estado
     */
    public RespuestaApi<Object> resumen() {
        throw new UnsupportedOperationException(
                "El resumen se implementara al conectar la persistencia.");
    }

    /**
     * Reintenta las notificaciones fallidas que aún tienen intentos disponibles.
     *
     * <p>Es la operación de rescate: si el proveedor de correo estuvo caído
     * media hora, este método pone al día todo lo que quedo pendiente sin que
     * nadie tenga que tocar los tickets uno por uno.</p>
     *
     * @return número de notificaciones reintentadas
     */
    public int reintentarFallidas() {
        throw new UnsupportedOperationException(
                "El reintento se implementara al conectar la persistencia.");
    }
}
