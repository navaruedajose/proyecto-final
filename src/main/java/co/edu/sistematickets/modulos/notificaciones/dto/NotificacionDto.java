package co.edu.sistematickets.modulos.notificaciones.dto;

import co.edu.sistematickets.dominio.CanalNotificacion;
import co.edu.sistematickets.dominio.EstadoNotificacion;

import java.time.Instant;
import java.util.UUID;

/**
 * Notificacion enviada o pendiente de envio.
 *
 * <p>Modela un elemento de la cola de salida. El diseno merece una
 * explicacion, porque es el que responde a la pregunta «¿y si el servicio de
 * correo esta caido?».</p>
 *
 * <p>La notificacion <strong>no se envia dentro de la peticion HTTP</strong>
 * del usuario. Cuando un agente marca un ticket como resuelto, el cambio de
 * estado se confirma y se encola la notificacion; el envio ocurre despues. Si
 * el proveedor de correo tarda ocho segundos o esta caido, el agente no se
 * queda esperando mirando un boton girando: su cambio ya esta guardado.</p>
 *
 * <p>Los campos {@code estado} e {@code intentos} son los que hacen posible el
 * reintento. Una notificacion en {@code FALLIDO} con intentos a medio gastar
 * se puede reintentar sin que el usuario tenga que volver a provocar el cambio
 * de estado.</p>
 *
 * @param id           identificador de la notificacion
 * @param ticketId     ticket que la origino
 * @param usuarioId    destinatario
 * @param canal        via de envio, correo o SMS
 * @param destinatario direccion de correo o numero de telefono
 * @param asunto       asunto del mensaje
 * @param mensaje      cuerpo del mensaje
 * @param estado       situacion del envio
 * @param intentos     numero de intentos realizados
 * @param errorDetalle motivo del ultimo fallo, si lo hubo
 * @param enviadoEn    momento del envio correcto
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
