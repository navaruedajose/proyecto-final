package co.edu.sistematickets.modulos.reportes.dto;

import co.edu.sistematickets.dominio.EstadoTicket;
import co.edu.sistematickets.dominio.PrioridadTicket;

/**
 * Indicadores del panel de control.
 *
 * <p>Es un DTO de solo lectura que se alimenta de dos vistas de PostgreSQL:</p>
 * <ul>
 *   <li>{@code vista_resumen_tickets} — totales por estado, prioridad y
 *       categoría.</li>
 *   <li>{@code vista_desempeno_agentes} — carga y cumplimiento por agente.</li>
 * </ul>
 *
 * <p>La agregación se hace en la base y no en la aplicación. Sumar en memoria
 * obligaría a traer todas las filas para contarlas, que es exactamente lo que
 * no se puede hacer cuando la tabla tiene millones de registros. Es también la
 * respuesta a la pregunta clásica «¿esto escala?»: el trabajo pesado ocurre
 * donde están los datos.</p>
 *
 * @param totalTickets              total de tickets vigentes
 * @param totalPendientes           tickets sin resolver ni cerrar
 * @param totalResueltos            tickets resueltos
 * @param totalCerrados             tickets cerrados
 * @param totalVencidos             tickets con el SLA incumplido
 * @param porcentajeCumplimientoSla porcentaje de tickets resueltos dentro de plazo
 * @param promedioHorasResolucion   media de horas hasta la resolución
 * @param porEstado                 desglose de totales por estado
 * @param porPrioridad              desglose de totales por prioridad
 * @param porCategoria              desglose de totales por categoría
 */
public record PanelReporteDto(
        long totalTickets,
        long totalPendientes,
        long totalResueltos,
        long totalCerrados,
        long totalVencidos,
        Double porcentajeCumplimientoSla,
        Double promedioHorasResolucion,
        java.util.Map<EstadoTicket, Long> porEstado,
        java.util.Map<PrioridadTicket, Long> porPrioridad,
        java.util.Map<String, Long> porCategoria
) {
}
