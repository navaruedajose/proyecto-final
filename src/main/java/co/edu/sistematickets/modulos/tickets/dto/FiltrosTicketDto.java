package co.edu.sistematickets.modulos.tickets.dto;

import co.edu.sistematickets.comun.PaginacionDto;
import co.edu.sistematickets.dominio.EstadoTicket;
import co.edu.sistematickets.dominio.PrioridadTicket;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Filtros del listado de tickets.
 *
 * <p>Hereda de {@link PaginacionDto}, así que ya trae página, límite,
 * ordenación y dirección. Aquí solo se añaden los criterios propios del
 * listado de tickets.</p>
 *
 * <p>Todos los campos son opcionales: sin filtros, el servicio aplica
 * aislamiento por rol. Es decir, un solicitante nunca ve los tickets de otro
 * aunque no envíe ningún filtro, porque el filtro de propiedad no viene del
 * cliente sino de la sesión.</p>
 *
 * @see PaginacionDto
 */
public class FiltrosTicketDto extends PaginacionDto {

    /** Filtra por estado del ciclo de vida. */
    private EstadoTicket estado;

    /** Filtra por urgencia. */
    private PrioridadTicket prioridad;

    /** Filtra por categoría. */
    private UUID categoriaId;

    /** Filtra por agente asignado. */
    private UUID agenteId;

    /** Filtra por quien reporto el ticket. */
    private UUID solicitanteId;

    /** Búsqueda de texto libre sobre título y descripción. */
    private String busqueda;

    /** Fecha inicial (inclusive) en formato {@code AAAA-MM-DD}. */
    private LocalDate desde;

    /** Fecha final (inclusive) en formato {@code AAAA-MM-DD}. */
    private LocalDate hasta;

    /** Si es {@code true}, solo devuelve tickets con el SLA vencido. */
    private Boolean soloVencidos;

    public EstadoTicket getEstado() {
        return estado;
    }

    public void setEstado(EstadoTicket estado) {
        this.estado = estado;
    }

    public PrioridadTicket getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(PrioridadTicket prioridad) {
        this.prioridad = prioridad;
    }

    public UUID getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(UUID categoriaId) {
        this.categoriaId = categoriaId;
    }

    public UUID getAgenteId() {
        return agenteId;
    }

    public void setAgenteId(UUID agenteId) {
        this.agenteId = agenteId;
    }

    public UUID getSolicitanteId() {
        return solicitanteId;
    }

    public void setSolicitanteId(UUID solicitanteId) {
        this.solicitanteId = solicitanteId;
    }

    public String getBusqueda() {
        return busqueda;
    }

    public void setBusqueda(String busqueda) {
        this.busqueda = busqueda;
    }

    public LocalDate getDesde() {
        return desde;
    }

    public void setDesde(LocalDate desde) {
        this.desde = desde;
    }

    public LocalDate getHasta() {
        return hasta;
    }

    public void setHasta(LocalDate hasta) {
        this.hasta = hasta;
    }

    public Boolean getSoloVencidos() {
        return soloVencidos;
    }

    public void setSoloVencidos(Boolean soloVencidos) {
        this.soloVencidos = soloVencidos;
    }
}
