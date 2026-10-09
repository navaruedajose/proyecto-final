package co.edu.sistematickets.dominio;

/**
 * Nivel de urgencia de un ticket.
 *
 * <p>Espeja el tipo {@code prioridad_ticket} de PostgreSQL. La prioridad no
 * altera el SLA por si sola: lo que manda es la categoria. Sirve para ordenar
 * la cola de trabajo y para que los informes muestren donde se concentra lo
 * urgente.</p>
 */
public enum PrioridadTicket {

    BAJA,
    MEDIA,
    ALTA,
    CRITICA;

    /**
     * Peso numerico de la prioridad, util para ordenar colecciones en memoria.
     *
     * <p>El orden natural del {@code enum} ya va de menor a mayor, pero exponer
     * el peso evita depender de {@code ordinal()} en el codigo de negocio
     * (que se rompe en silencio si alguien reordena las constantes).</p>
     *
     * @return valor entre 1 (BAJA) y 4 (CRITICA)
     */
    public int peso() {
        return ordinal() + 1;
    }
}
