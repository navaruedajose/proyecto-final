package co.edu.sistematickets.dominio;

/**
 * Canal por el que se entrega una notificación.
 *
 * <p>Espeja el tipo {@code canal_notificacion} de PostgreSQL. Ambos canales se
 * disparan cuando un ticket pasa a {@link EstadoTicket#RESUELTO}.</p>
 */
public enum CanalNotificacion {

    EMAIL,
    SMS
}
