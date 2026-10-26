package co.edu.sistematickets.modulos.notificaciones.dto;

import co.edu.sistematickets.dominio.CanalNotificacion;
import co.edu.sistematickets.dominio.EstadoNotificacion;

import java.time.Instant;
import java.util.UUID;

/**
 * Notificación enviada o pendiente de envío.
 *
 * <p>Modela un elemento de la cola de salida. El diseño merece una
 * explicación, porque es el que responde a la pregunta «¿y si el servicio de
 * correo está caído?».</p>
 *
 * <p>La notificación <strong>no se envía dentro de la petición HTTP</strong>
 * del usuario. Cuando un agente marca un ticket como resuelto, el cambio de
 * estado se confirma y se encola la notificación; el envío ocurre después. Si
 * el proveedor de correo tarda ocho segundos o está caído, el agente no se
 * queda esperando mirando un boton girando: su cambio ya está guardado.</p>
 *
 * <p>Los campos {@code estado} e {@code intentos} son los que hacen posible el
 * reintento. Una notificación en {@code FALLIDO} con intentos a medio gastar
 * se puede reintentar sin que el usuario tenga que volver a provocar el cambio
 * de estado.</p>
 *
 * @param id           identificador de la notificación
 * @param ticketId     ticket que la origino
 * @param usuarioId    destinatario
 * @param canal        vía de envío, correo o SMS
 * @param destinatario dirección de correo o número de teléfono
 * @param asunto       asunto del mensaje
 * @param mensaje      cuerpo del mensaje
 * @param estado       situación del envío
 * @param intentos     número de intentos realizados
 * @param errorDetalle motivo del último fallo, si lo hubo
 * @param enviadoEn    momento del envío correcto
 * @param creadoEn     momento de encolado
 */
public record NotificacionDto(
        UUID id,
        UUID ticketId,
        UUID usuarioId,
        CanalNotificacion canal,
        String destinatario,
        String asunto,
        String mensaje,
        EstadoNotificacion estado,
        Integer intentos,
        String errorDetalle,
        Instant enviadoEn,
        Instant creadoEn
) {
}
