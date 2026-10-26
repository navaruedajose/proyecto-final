package co.edu.sistematickets.dominio;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Estados por los que pasa un ticket durante su ciclo de vida.
 *
 * <p>Espeja el tipo {@code estado_ticket} de PostgreSQL. Además de listar los
 * estados, esta clase <strong>es</strong> la máquina de estados: dice que
 * transiciones son legales. Por eso el mapa de transiciones vive aquí y no en
 * el servicio: la regla es del dominio, no un detalle de una capa.</p>
 *
 * <p>Que una transición no aparezca en el mapa significa que es ilegal. Un
 * intento de transición ilegal debe responder HTTP {@code 409 Conflict}, no
 * {@code 400}: la petición está bien formada, lo que no encaja es el estado
 * actual del recurso. Esa distinción es la que impide que un cliente deje el
 * sistema en un estado inconsistente.</p>
 */
public enum EstadoTicket {

    /** Recién creado, aún sin que nadie lo haya tomado. */
    NUEVO,

    /** Alguien lo tomo pero todavía no empezo a trabajarlo. */
    ABIERTO,

    /** En manos de un agente, con trabajo en curso. */
    EN_PROGRESO,

    /** Bloqueado a la espera de información del solicitante o de un tercero. */
    EN_ESPERA,

    /** Solucion aplicada. Es el estado que dispara la notificación al solicitante. */
    RESUELTO,

    /** Cerrado y contabilizado. Estado final: no admite más cambios. */
    CERRADO,

    /** Anulado antes de resolverse. Estado final: no admite más cambios. */
    CANCELADO;

    /** Transiciones legales desde cada estado. */
    private static final Map<EstadoTicket, Set<EstadoTicket>> TRANSICIONES;

    static {
        Map<EstadoTicket, Set<EstadoTicket>> mapa = new EnumMap<>(EstadoTicket.class);
        mapa.put(NUEVO, EnumSet.of(ABIERTO, EN_PROGRESO, CANCELADO));
        mapa.put(ABIERTO, EnumSet.of(EN_PROGRESO, EN_ESPERA, RESUELTO, CANCELADO));
        mapa.put(EN_PROGRESO, EnumSet.of(EN_ESPERA, RESUELTO, ABIERTO, CANCELADO));
        mapa.put(EN_ESPERA, EnumSet.of(EN_PROGRESO, RESUELTO, CANCELADO));
        // Un RESUELTO puede reabrirse si el solicitante no queda conforme.
        mapa.put(RESUELTO, EnumSet.of(CERRADO, EN_PROGRESO));
        mapa.put(CERRADO, EnumSet.noneOf(EstadoTicket.class));
        mapa.put(CANCELADO, EnumSet.noneOf(EstadoTicket.class));
        TRANSICIONES = Collections.unmodifiableMap(mapa);
    }

    /**
     * Indica si el ticket puede pasar de este estado al indicado.
     *
     * <p>Pasar al mismo estado siempre se considera válido: repetir el estado
     * actual no cambia nada y no tiene sentido rechazarlo.</p>
     *
     * @param estadoNuevo estado al que se quiere pasar
     * @return {@code true} si la transición es legal
     */
    public boolean puedeTransicionarA(EstadoTicket estadoNuevo) {
        if (estadoNuevo == null) {
            return false;
        }
        if (this == estadoNuevo) {
            return true;
        }
        return TRANSICIONES.getOrDefault(this, Collections.emptySet()).contains(estadoNuevo);
    }

    /**
     * @return {@code true} si es un estado terminal (no admite cambios)
     */
    public boolean esFinal() {
        return this == CERRADO || this == CANCELADO;
    }

    /**
     * @return {@code true} si el ticket sigue vivo y exige atención
     */
    public boolean estaPendiente() {
        return this == NUEVO || this == ABIERTO || this == EN_PROGRESO || this == EN_ESPERA;
    }
}
