package co.edu.sistematickets.dominio;

/**
 * Estado de entrega de una notificación.
 *
 * <p>Espeja el tipo {@code estado_notificacion} de PostgreSQL. El paso por
 * {@link #FALLIDO} es el que habilita los reintentos sin que el usuario tenga
 * que volver a provocar el cambio de estado del ticket.</p>
 */
public enum EstadoNotificacion {

    PENDIENTE,
    ENVIADO,
    FALLIDO
}
